/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.util.concurrent;

import java.util.ArrayList;
import java.util.List;

public class ForkJoinPool extends AbstractExecutorService {
  private static final ForkJoinPool common = new ForkJoinPool();

  private final int parallelism;
  private volatile boolean shutdown = false;
  private final BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<Runnable>();
  private final List<Thread> workers = new ArrayList<Thread>();

  public static ForkJoinPool commonPool() {
    return common;
  }

  public static int getCommonPoolParallelism() {
    return Math.max(1, Runtime.getRuntime().availableProcessors() - 1);
  }

  public ForkJoinPool() {
    this(Math.max(1, Runtime.getRuntime().availableProcessors()));
  }

  public ForkJoinPool(int parallelism) {
    if (parallelism <= 0) throw new IllegalArgumentException();
    this.parallelism = parallelism;
    for (int i = 0; i < parallelism; ++i) {
      ForkJoinWorkerThread worker = new ForkJoinWorkerThread(this);
      worker.setName("ForkJoinPool-worker-" + i);
      workers.add(worker);
      worker.start();
    }
  }

  void runWorker() {
    while (!shutdown || !workQueue.isEmpty()) {
      try {
        Runnable task = workQueue.poll(100, TimeUnit.MILLISECONDS);
        if (task != null) {
          task.run();
        }
      } catch (InterruptedException e) {
        if (shutdown) break;
      } catch (Throwable ignore) {
      }
    }
  }

  public int getParallelism() {
    return parallelism;
  }

  public <T> T invoke(ForkJoinTask<T> task) {
    if (task == null) throw new NullPointerException();
    execute(task);
    return task.join();
  }

  public void execute(ForkJoinTask<?> task) {
    if (task == null) throw new NullPointerException();
    if (shutdown) throw new RejectedExecutionException();
    workQueue.offer(new Runnable() {
      @Override
      public void run() {
        task.doExec();
      }
    });
  }

  public <T> ForkJoinTask<T> submit(ForkJoinTask<T> task) {
    if (task == null) throw new NullPointerException();
    execute(task);
    return task;
  }

  @Override
  public <T> ForkJoinTask<T> submit(Callable<T> task) {
    ForkJoinTask<T> fjt = ForkJoinTask.adapt(task);
    execute(fjt);
    return fjt;
  }

  @Override
  public <T> ForkJoinTask<T> submit(Runnable task, T result) {
    ForkJoinTask<T> fjt = ForkJoinTask.adapt(task, result);
    execute(fjt);
    return fjt;
  }

  @Override
  public ForkJoinTask<?> submit(Runnable task) {
    ForkJoinTask<?> fjt = ForkJoinTask.adapt(task);
    execute(fjt);
    return fjt;
  }

  @Override
  public void execute(Runnable task) {
    if (task == null) throw new NullPointerException();
    if (shutdown) throw new RejectedExecutionException();
    workQueue.offer(task);
  }

  @Override
  public void shutdown() {
    shutdown = true;
  }

  @Override
  public List<Runnable> shutdownNow() {
    shutdown = true;
    List<Runnable> remaining = new ArrayList<Runnable>();
    workQueue.drainTo(remaining);
    for (Thread worker : workers) {
      worker.interrupt();
    }
    return remaining;
  }

  @Override
  public boolean isShutdown() {
    return shutdown;
  }

  @Override
  public boolean isTerminated() {
    return shutdown && workQueue.isEmpty();
  }

  @Override
  public boolean awaitTermination(long timeout, TimeUnit unit)
      throws InterruptedException {
    long deadline = System.currentTimeMillis() + unit.toMillis(timeout);
    while (!isTerminated()) {
      long remaining = deadline - System.currentTimeMillis();
      if (remaining <= 0) return false;
      Thread.sleep(Math.min(remaining, 50));
    }
    return true;
  }
}
