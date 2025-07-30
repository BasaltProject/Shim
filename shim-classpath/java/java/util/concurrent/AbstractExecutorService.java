/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.util.concurrent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class AbstractExecutorService implements ExecutorService {
  protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
    return new FutureTask<T>(runnable, value);
  }

  protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
    return new FutureTask<T>(callable);
  }

  @Override
  public Future<?> submit(Runnable task) {
    if (task == null) throw new NullPointerException();
    RunnableFuture<Void> ftask = newTaskFor(task, null);
    execute(ftask);
    return ftask;
  }

  @Override
  public <T> Future<T> submit(Runnable task, T result) {
    if (task == null) throw new NullPointerException();
    RunnableFuture<T> ftask = newTaskFor(task, result);
    execute(ftask);
    return ftask;
  }

  @Override
  public <T> Future<T> submit(Callable<T> task) {
    if (task == null) throw new NullPointerException();
    RunnableFuture<T> ftask = newTaskFor(task);
    execute(ftask);
    return ftask;
  }

  @Override
  public <T> T invokeAny(Collection<? extends Callable<T>> tasks)
      throws InterruptedException, ExecutionException {
    if (tasks == null || tasks.isEmpty()) throw new IllegalArgumentException();
    ExecutionException lastEx = null;
    for (Callable<T> task : tasks) {
      try {
        return task.call();
      } catch (Exception ex) {
        lastEx = new ExecutionException(ex);
      }
    }
    throw (lastEx != null) ? lastEx : new ExecutionException("No task succeeded", null);
  }

  @Override
  public <T> T invokeAny(
      Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
      throws InterruptedException, ExecutionException, TimeoutException {
    return invokeAny(tasks);
  }

  @Override
  public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks)
      throws InterruptedException {
    if (tasks == null) throw new NullPointerException();
    List<Future<T>> futures = new ArrayList<Future<T>>(tasks.size());
    for (Callable<T> t : tasks) {
      RunnableFuture<T> f = newTaskFor(t);
      futures.add(f);
      execute(f);
    }
    for (Future<T> f : futures) {
      if (!f.isDone()) {
        try {
          f.get();
        } catch (CancellationException | ExecutionException ignore) {
        }
      }
    }
    return futures;
  }

  @Override
  public <T> List<Future<T>> invokeAll(
      Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
      throws InterruptedException {
    return invokeAll(tasks);
  }
}
