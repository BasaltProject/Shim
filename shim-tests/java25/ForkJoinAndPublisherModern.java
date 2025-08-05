/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

// Minimum release: 17

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ForkJoinAndPublisherModern {
  public static void main(String[] args) throws Exception {
    testRecursiveTask();
    testRecursiveAction();
    testForkJoinTaskAdapt();
    testForkJoinTaskCompletions();
    testSubmissionPublisher();
    System.out.println("ForkJoinAndPublisherModern OK");
  }

  static class FibonacciTask extends RecursiveTask<Integer> {
    final int n;

    FibonacciTask(int n) {
      this.n = n;
    }

    @Override
    protected Integer compute() {
      if (n <= 1) return n;
      FibonacciTask f1 = new FibonacciTask(n - 1);
      f1.fork();
      FibonacciTask f2 = new FibonacciTask(n - 2);
      return f2.compute() + f1.join();
    }
  }

  private static void testRecursiveTask() {
    ForkJoinPool pool = ForkJoinPool.commonPool();
    if (pool == null) throw new AssertionError("commonPool is null");
    if (ForkJoinPool.getCommonPoolParallelism() <= 0) {
      throw new AssertionError("commonPoolParallelism <= 0");
    }

    int fib8 = pool.invoke(new FibonacciTask(8));
    if (fib8 != 21) {
      throw new AssertionError("fib(8) mismatch: " + fib8);
    }

    ForkJoinTask<Integer> submitTask = pool.submit(new FibonacciTask(7));
    if (submitTask.join() != 13) {
      throw new AssertionError("submit fib(7) mismatch: " + submitTask.join());
    }
  }

  static class ArrayIncrementAction extends RecursiveAction {
    final int[] array;
    final int start;
    final int end;

    ArrayIncrementAction(int[] array, int start, int end) {
      this.array = array;
      this.start = start;
      this.end = end;
    }

    @Override
    protected void compute() {
      if (end - start <= 2) {
        for (int i = start; i < end; i++) {
          array[i] += 10;
        }
      } else {
        int mid = (start + end) >>> 1;
        ArrayIncrementAction left = new ArrayIncrementAction(array, start, mid);
        ArrayIncrementAction right = new ArrayIncrementAction(array, mid, end);
        invokeAll(left, right);
      }
    }
  }

  private static void testRecursiveAction() {
    int[] array = new int[]{1, 2, 3, 4, 5, 6, 7, 8};
    ArrayIncrementAction action = new ArrayIncrementAction(array, 0, array.length);
    ForkJoinPool.commonPool().invoke(action);
    if (!action.isDone() || action.isCompletedAbnormally()) {
      throw new AssertionError("action did not complete normally");
    }
    for (int i = 0; i < array.length; i++) {
      if (array[i] != (i + 1) + 10) {
        throw new AssertionError("array[" + i + "] mismatch: " + array[i]);
      }
    }
  }

  private static void testForkJoinTaskAdapt() throws Exception {
    Callable<String> callable = () -> "adapted-callable";
    ForkJoinTask<String> taskFromCallable = ForkJoinTask.adapt(callable);
    String res = ForkJoinPool.commonPool().invoke(taskFromCallable);
    if (!"adapted-callable".equals(res)) {
      throw new AssertionError("adapted callable mismatch: " + res);
    }

    AtomicBoolean ran = new AtomicBoolean(false);
    Runnable runnable = () -> ran.set(true);
    ForkJoinTask<Integer> taskFromRunnable = ForkJoinTask.adapt(runnable, 42);
    Integer runnableRes = ForkJoinPool.commonPool().invoke(taskFromRunnable);
    if (!ran.get() || runnableRes != 42) {
      throw new AssertionError("adapted runnable mismatch: " + runnableRes);
    }
  }

  private static void testForkJoinTaskCompletions() throws Exception {
    ForkJoinTask<String> task = new RecursiveTask<String>() {
      @Override
      protected String compute() {
        return "original";
      }
    };
    task.complete("forced");
    if (!task.isDone() || !"forced".equals(task.join()) || !"forced".equals(task.get())) {
      throw new AssertionError("complete value mismatch: " + task.join());
    }

    ForkJoinTask<String> exceptionalTask = new RecursiveTask<String>() {
      @Override
      protected String compute() {
        return "original";
      }
    };
    exceptionalTask.completeExceptionally(new IllegalStateException("boom"));
    if (!exceptionalTask.isCompletedAbnormally()) {
      throw new AssertionError("exceptional task not completed abnormally");
    }
    if (!(exceptionalTask.getException() instanceof IllegalStateException)) {
      throw new AssertionError("unexpected exception: " + exceptionalTask.getException());
    }
  }

  private static void testSubmissionPublisher() throws Exception {
    try (SubmissionPublisher<String> publisher = new SubmissionPublisher<String>()) {
      if (publisher.isClosed()) throw new AssertionError("publisher should be open");
      if (publisher.hasSubscribers()) throw new AssertionError("publisher should have no subscribers");

      List<String> collected = Collections.synchronizedList(new ArrayList<String>());
      CompletableFuture<Void> future = publisher.consume(collected::add);
      if (!publisher.hasSubscribers()) throw new AssertionError("subscriber not registered");
      if (publisher.getNumberOfSubscribers() != 1) {
        throw new AssertionError("subscribers count mismatch: " + publisher.getNumberOfSubscribers());
      }

      publisher.submit("msg-1");
      publisher.submit("msg-2");
      publisher.submit("msg-3");
      publisher.close();

      future.get(5, TimeUnit.SECONDS);

      if (collected.size() != 3 || !"msg-1".equals(collected.get(0)) ||
          !"msg-2".equals(collected.get(1)) || !"msg-3".equals(collected.get(2))) {
        throw new AssertionError("collected items mismatch: " + collected);
      }
      if (!publisher.isClosed()) {
        throw new AssertionError("publisher should be closed");
      }
    }
  }
}
