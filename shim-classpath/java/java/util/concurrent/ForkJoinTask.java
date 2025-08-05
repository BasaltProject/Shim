/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.util.concurrent;

import java.io.Serializable;
import java.util.Collection;
import java.util.Objects;

public abstract class ForkJoinTask<V> implements Future<V>, Serializable {
  private volatile int status;
  private static final int NORMAL = 2;
  private static final int CANCELLED = 3;
  private static final int EXCEPTIONAL = 4;

  private Throwable exception;

  public abstract V getRawResult();

  protected abstract void setRawResult(V value);

  protected abstract boolean exec();

  public final ForkJoinTask<V> fork() {
    Thread t = Thread.currentThread();
    if (t instanceof ForkJoinWorkerThread) {
      ((ForkJoinWorkerThread) t).pool.execute(this);
    } else {
      ForkJoinPool.commonPool().execute(this);
    }
    return this;
  }

  public final V join() {
    if (status < NORMAL) {
      doExec();
    }
    if (status == CANCELLED) {
      throw new CancellationException();
    }
    if (status == EXCEPTIONAL) {
      if (exception instanceof RuntimeException) {
        throw (RuntimeException) exception;
      }
      if (exception instanceof Error) {
        throw (Error) exception;
      }
      throw new RuntimeException(exception);
    }
    return getRawResult();
  }

  public final V invoke() {
    doExec();
    return join();
  }

  void doExec() {
    if (status < NORMAL) {
      synchronized (this) {
        if (status < NORMAL) {
          status = 1;
          try {
            if (exec()) {
              status = NORMAL;
            }
          } catch (Throwable t) {
            this.exception = t;
            status = EXCEPTIONAL;
          }
          notifyAll();
        }
      }
    }
  }

  public static void invokeAll(ForkJoinTask<?> t1, ForkJoinTask<?> t2) {
    Objects.requireNonNull(t1, "t1");
    Objects.requireNonNull(t2, "t2");
    t2.fork();
    t1.invoke();
    t2.join();
  }

  public static void invokeAll(ForkJoinTask<?>... tasks) {
    Objects.requireNonNull(tasks, "tasks");
    if (tasks.length == 0) return;
    for (int i = 1; i < tasks.length; i++) {
      if (tasks[i] != null) tasks[i].fork();
    }
    if (tasks[0] != null) tasks[0].invoke();
    for (int i = 1; i < tasks.length; i++) {
      if (tasks[i] != null) tasks[i].join();
    }
  }

  public static <T extends ForkJoinTask<?>> Collection<T> invokeAll(Collection<T> tasks) {
    Objects.requireNonNull(tasks, "tasks");
    ForkJoinTask<?>[] arr = tasks.toArray(new ForkJoinTask<?>[tasks.size()]);
    invokeAll(arr);
    return tasks;
  }

  public static ForkJoinTask<?> adapt(Runnable runnable) {
    Objects.requireNonNull(runnable, "runnable");
    return new AdaptedRunnableAction(runnable);
  }

  public static <T> ForkJoinTask<T> adapt(Runnable runnable, T result) {
    Objects.requireNonNull(runnable, "runnable");
    return new AdaptedRunnable<T>(runnable, result);
  }

  public static <T> ForkJoinTask<T> adapt(Callable<? extends T> callable) {
    Objects.requireNonNull(callable, "callable");
    return new AdaptedCallable<T>(callable);
  }

  @Override
  public boolean cancel(boolean mayInterruptIfRunning) {
    synchronized (this) {
      if (status >= NORMAL) return false;
      status = CANCELLED;
      notifyAll();
      return true;
    }
  }

  @Override
  public boolean isCancelled() {
    return status == CANCELLED;
  }

  @Override
  public boolean isDone() {
    return status >= NORMAL;
  }

  public boolean isCompletedNormally() {
    return status == NORMAL;
  }

  public boolean isCompletedAbnormally() {
    return status > NORMAL;
  }

  public Throwable getException() {
    return exception;
  }

  public void complete(V value) {
    setRawResult(value);
    synchronized (this) {
      status = NORMAL;
      notifyAll();
    }
  }

  public void completeExceptionally(Throwable ex) {
    this.exception = (ex != null) ? ex : new RuntimeException();
    synchronized (this) {
      status = EXCEPTIONAL;
      notifyAll();
    }
  }

  @Override
  public V get() throws InterruptedException, ExecutionException {
    synchronized (this) {
      while (status < NORMAL) {
        wait();
      }
    }
    if (status == CANCELLED) throw new CancellationException();
    if (status == EXCEPTIONAL) throw new ExecutionException(exception);
    return getRawResult();
  }

  @Override
  public V get(long timeout, TimeUnit unit)
      throws InterruptedException, ExecutionException, TimeoutException {
    long deadline = System.currentTimeMillis() + unit.toMillis(timeout);
    synchronized (this) {
      while (status < NORMAL) {
        long remaining = deadline - System.currentTimeMillis();
        if (remaining <= 0) throw new TimeoutException();
        wait(remaining);
      }
    }
    if (status == CANCELLED) throw new CancellationException();
    if (status == EXCEPTIONAL) throw new ExecutionException(exception);
    return getRawResult();
  }

  @Override
  public Future.State state() {
    int s = status;
    if (s == CANCELLED) return Future.State.CANCELLED;
    if (s == EXCEPTIONAL) return Future.State.FAILED;
    if (s == NORMAL) return Future.State.SUCCESS;
    return Future.State.RUNNING;
  }

  @Override
  public V resultNow() {
    if (!isDone()) {
      throw new IllegalStateException("Task has not completed");
    }
    if (status == CANCELLED) {
      throw new IllegalStateException("Task was cancelled");
    }
    if (status == EXCEPTIONAL) {
      throw new IllegalStateException("Task completed exceptionally: " + exception);
    }
    return getRawResult();
  }

  @Override
  public Throwable exceptionNow() {
    if (!isDone()) {
      throw new IllegalStateException("Task has not completed");
    }
    if (status == CANCELLED) {
      throw new IllegalStateException("Task was cancelled");
    }
    if (status != EXCEPTIONAL) {
      throw new IllegalStateException("Task completed normally with a result");
    }
    return exception;
  }

  private static final class AdaptedRunnableAction extends ForkJoinTask<Void> {
    private final Runnable runnable;

    AdaptedRunnableAction(Runnable runnable) {
      this.runnable = runnable;
    }

    @Override
    public Void getRawResult() {
      return null;
    }

    @Override
    protected void setRawResult(Void value) {
    }

    @Override
    protected boolean exec() {
      runnable.run();
      return true;
    }
  }

  private static final class AdaptedRunnable<T> extends ForkJoinTask<T> {
    private final Runnable runnable;
    private T result;

    AdaptedRunnable(Runnable runnable, T result) {
      this.runnable = runnable;
      this.result = result;
    }

    @Override
    public T getRawResult() {
      return result;
    }

    @Override
    protected void setRawResult(T value) {
      this.result = value;
    }

    @Override
    protected boolean exec() {
      runnable.run();
      return true;
    }
  }

  private static final class AdaptedCallable<T> extends ForkJoinTask<T> {
    private final Callable<? extends T> callable;
    private T result;

    AdaptedCallable(Callable<? extends T> callable) {
      this.callable = callable;
    }

    @Override
    public T getRawResult() {
      return result;
    }

    @Override
    protected void setRawResult(T value) {
      this.result = value;
    }

    @Override
    protected boolean exec() {
      try {
        result = callable.call();
        return true;
      } catch (RuntimeException | Error e) {
        throw e;
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }
}
