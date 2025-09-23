/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.util.concurrent;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public abstract class ForkJoinTask<V> implements Future<V>, Serializable {
  private static final long serialVersionUID = -7721805057305804111L;

  static final int DONE = 1 << 31;
  static final int ABNORMAL = 1 << 16;
  static final int THROWN = 1 << 17;
  static final int SMASK = 0xffff;

  volatile int status;
  private transient Throwable exception;
  private transient long throwerId;
  transient ForkJoinPool pool;
  transient Thread submitter;

  public ForkJoinTask() { }

  final synchronized int setDone() {
    int s = status;
    if (s >= 0) {
      s = status = s | DONE;
      notifyAll();
    }
    return s;
  }

  private synchronized int abnormalCompletion(int completion) {
    int s = status;
    if (s >= 0) {
      s = status = s | completion;
      notifyAll();
    }
    return s;
  }

  final int trySetCancelled() {
    return abnormalCompletion(DONE | ABNORMAL);
  }

  final int trySetException(Throwable ex) {
    boolean set = false;
    int s;
    synchronized (this) {
      s = status;
      if (s >= 0) {
        exception = ex;
        throwerId = Thread.currentThread().getId();
        s = status = s | (DONE | ABNORMAL | THROWN);
        set = true;
        notifyAll();
      }
    }
    if (set) {
      internalPropagateException(ex);
    }
    return s;
  }

  void internalPropagateException(Throwable ex) { }

  final boolean trySetThrown(Throwable ex) {
    return (trySetException(ex) & THROWN) != 0 && exception == ex;
  }

  final int doExec() {
    int s = status;
    if (s >= 0) {
      boolean completed;
      try {
        completed = exec();
      } catch (Throwable rex) {
        return trySetException(rex);
      }
      if (completed) {
        s = setDone();
      } else {
        s = status;
      }
    }
    return s;
  }

  private Throwable getThrowableException() {
    Throwable ex = exception;
    if (ex != null && throwerId != Thread.currentThread().getId()) {
      try {
        Constructor<?> noArgCtor = null;
        Constructor<?> oneArgCtor = null;
        for (Constructor<?> c : ex.getClass().getConstructors()) {
          Class<?>[] ps = c.getParameterTypes();
          if (ps.length == 0) {
            noArgCtor = c;
          } else if (ps.length == 1 && ps[0] == Throwable.class) {
            oneArgCtor = c;
            break;
          }
        }
        if (oneArgCtor != null) {
          ex = (Throwable) oneArgCtor.newInstance(ex);
        } else if (noArgCtor != null) {
          Throwable rx = (Throwable) noArgCtor.newInstance();
          rx.initCause(ex);
          ex = rx;
        }
      } catch (Exception ignore) {
        ex = exception;
      }
    }
    return ex;
  }

  @SuppressWarnings("unchecked")
  static <T extends Throwable> void uncheckedThrow(Throwable t) throws T {
    if (t != null) {
      throw (T) t;
    }
    throw new Error("Unknown Exception");
  }

  private void reportException(int s) {
    if ((s & ABNORMAL) != 0) {
      if ((s & THROWN) == 0) {
        throw new CancellationException();
      }
      ForkJoinTask.<RuntimeException>uncheckedThrow(getThrowableException());
    }
  }

  private int awaitDone(boolean interruptible, long deadline) throws InterruptedException {
    int s = status;
    if (s < 0) {
      return s;
    }
    boolean interrupted = false;
    try {
      ForkJoinPool.awaitTask(this, deadline, interruptible);
    } catch (InterruptedException e) {
      interrupted = true;
      if (interruptible) {
        throw e;
      }
    }
    return status;
  }

  private int awaitUninterruptibly(long deadline) {
    boolean interrupted = false;
    for (;;) {
      try {
        ForkJoinPool.awaitTask(this, deadline, true);
        break;
      } catch (InterruptedException e) {
        interrupted = true;
      }
    }
    if (interrupted) {
      Thread.currentThread().interrupt();
    }
    return status;
  }

  public final ForkJoinTask<V> fork() {
    Thread t = Thread.currentThread();
    if (t instanceof ForkJoinWorkerThread) {
      ForkJoinWorkerThread w = (ForkJoinWorkerThread) t;
      w.pool.pushLocal(this, w.workQueue);
    } else {
      ForkJoinPool.common.externalPush(this);
    }
    return this;
  }

  public final V join() {
    int s = status;
    if (s >= 0) {
      s = awaitUninterruptibly(0L);
    }
    if ((s & ABNORMAL) != 0) {
      reportException(s);
    }
    return getRawResult();
  }

  public final V invoke() {
    int s = doExec();
    if (s >= 0) {
      s = awaitUninterruptibly(0L);
    }
    if ((s & ABNORMAL) != 0) {
      reportException(s);
    }
    return getRawResult();
  }

  public static void invokeAll(ForkJoinTask<?> t1, ForkJoinTask<?> t2) {
    int s1, s2;
    if (t1 == null || t2 == null) {
      throw new NullPointerException();
    }
    t2.fork();
    if (((s1 = t1.doExec()) & ABNORMAL) != 0) {
      t1.reportException(s1);
    }
    if (((s2 = t2.awaitUninterruptibly(0L)) & ABNORMAL) != 0) {
      t2.reportException(s2);
    }
  }

  public static void invokeAll(ForkJoinTask<?>... tasks) {
    Throwable ex = null;
    int last = tasks.length - 1;
    for (int i = last; i >= 0; --i) {
      ForkJoinTask<?> t = tasks[i];
      if (t == null) {
        if (ex == null) {
          ex = new NullPointerException();
        }
      } else if (i != 0) {
        t.fork();
      } else if ((t.doExec() & ABNORMAL) != 0 && ex == null) {
        ex = t.getException();
      }
    }
    for (int i = 1; i <= last; ++i) {
      ForkJoinTask<?> t = tasks[i];
      if (t != null) {
        if (ex != null) {
          t.cancel(false);
        } else if ((t.awaitUninterruptibly(0L) & ABNORMAL) != 0) {
          ex = t.getException();
        }
      }
    }
    if (ex != null) {
      ForkJoinTask.<RuntimeException>uncheckedThrow(ex);
    }
  }

  public static <T extends ForkJoinTask<?>> Collection<T> invokeAll(Collection<T> tasks) {
    if (!(tasks instanceof java.util.RandomAccess) || !(tasks instanceof List<?>)) {
      invokeAll(tasks.toArray(new ForkJoinTask<?>[0]));
      return tasks;
    }
    @SuppressWarnings("unchecked")
    List<? extends ForkJoinTask<?>> ts = (List<? extends ForkJoinTask<?>>) tasks;
    Throwable ex = null;
    int last = ts.size() - 1;
    for (int i = last; i >= 0; --i) {
      ForkJoinTask<?> t = ts.get(i);
      if (t == null) {
        if (ex == null) {
          ex = new NullPointerException();
        }
      } else if (i != 0) {
        t.fork();
      } else if ((t.doExec() & ABNORMAL) != 0 && ex == null) {
        ex = t.getException();
      }
    }
    for (int i = 1; i <= last; ++i) {
      ForkJoinTask<?> t = ts.get(i);
      if (t != null) {
        if (ex != null) {
          t.cancel(false);
        } else if ((t.awaitUninterruptibly(0L) & ABNORMAL) != 0) {
          ex = t.getException();
        }
      }
    }
    if (ex != null) {
      ForkJoinTask.<RuntimeException>uncheckedThrow(ex);
    }
    return tasks;
  }

  @Override
  public boolean cancel(boolean mayInterruptIfRunning) {
    int s = abnormalCompletion(DONE | ABNORMAL);
    return (s & (ABNORMAL | THROWN)) == ABNORMAL;
  }

  @Override
  public final boolean isDone() {
    return status < 0;
  }

  @Override
  public final boolean isCancelled() {
    return (status & (ABNORMAL | THROWN)) == ABNORMAL;
  }

  public final boolean isCompletedAbnormally() {
    return (status & ABNORMAL) != 0;
  }

  public final boolean isCompletedNormally() {
    return (status & (DONE | ABNORMAL)) == DONE;
  }

  @Override
  public State state() {
    int s = status;
    return (s >= 0) ? State.RUNNING : (s & (ABNORMAL | THROWN)) == 0 ? State.SUCCESS
        : (s & (ABNORMAL | THROWN)) == ABNORMAL ? State.CANCELLED : State.FAILED;
  }

  @Override
  public V resultNow() {
    int s = status;
    if ((s & ABNORMAL) == 0) {
      if (s >= 0) {
        throw new IllegalStateException("Task has not completed");
      }
      return getRawResult();
    }
    if ((s & THROWN) == 0) {
      throw new IllegalStateException("Task was cancelled");
    }
    throw new IllegalStateException("Task completed with exception");
  }

  @Override
  public Throwable exceptionNow() {
    int s = status;
    Throwable ex;
    if (s >= 0 || (s & (ABNORMAL | THROWN)) != (ABNORMAL | THROWN) || (ex = getThrowableException()) == null) {
      throw new IllegalStateException();
    }
    return ex;
  }

  public final Throwable getException() {
    int s = status;
    return ((s & ABNORMAL) == 0) ? null : ((s & THROWN) == 0) ? new CancellationException()
        : getThrowableException();
  }

  public void completeExceptionally(Throwable ex) {
    trySetException((ex instanceof RuntimeException) || (ex instanceof Error) ? ex : new RuntimeException(ex));
  }

  public void complete(V value) {
    try {
      setRawResult(value);
    } catch (Throwable rex) {
      trySetException(rex);
      return;
    }
    setDone();
  }

  public final void quietlyComplete() {
    setDone();
  }

  @Override
  public final V get() throws InterruptedException, ExecutionException {
    int s = awaitDone(true, 0L);
    if ((s & ABNORMAL) != 0) {
      reportExecutionException(s);
    }
    return getRawResult();
  }

  @Override
  public final V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException,
      TimeoutException {
    long nanos = unit.toNanos(timeout);
    int s = status;
    if (s >= 0 && nanos > 0L) {
      long d = System.nanoTime() + nanos;
      s = awaitDone(true, d == 0L ? 1L : d);
    }
    if (s >= 0) {
      if (Thread.interrupted()) {
        throw new InterruptedException();
      }
      throw new TimeoutException();
    }
    if ((s & ABNORMAL) != 0) {
      reportExecutionException(s);
    }
    return getRawResult();
  }

  private void reportExecutionException(int s) throws ExecutionException {
    Throwable ex = null;
    if (s == (DONE | ABNORMAL)) {
      ex = new CancellationException();
    } else if ((s & (ABNORMAL | THROWN)) == (ABNORMAL | THROWN)) {
      ex = getThrowableException();
    }
    if (ex instanceof CancellationException) {
      throw (CancellationException) ex;
    }
    if (ex != null) {
      throw new ExecutionException(ex);
    }
  }

  public final void quietlyJoin() {
    if (status >= 0) {
      awaitUninterruptibly(0L);
    }
  }

  public final void quietlyInvoke() {
    if (doExec() >= 0) {
      awaitUninterruptibly(0L);
    }
  }

  public final boolean quietlyJoin(long timeout, TimeUnit unit) throws InterruptedException {
    long nanos = unit.toNanos(timeout);
    int s = status;
    if (s >= 0 && nanos > 0L) {
      long d = System.nanoTime() + nanos;
      s = awaitDone(true, d == 0L ? 1L : d);
    }
    if (s >= 0 && Thread.interrupted()) {
      throw new InterruptedException();
    }
    return status < 0;
  }

  public final boolean quietlyJoinUninterruptibly(long timeout, TimeUnit unit) {
    long nanos = unit.toNanos(timeout);
    int s = status;
    if (s >= 0 && nanos > 0L) {
      long d = System.nanoTime() + nanos;
      s = awaitUninterruptibly(d == 0L ? 1L : d);
    }
    return status < 0;
  }

  public void reinitialize() {
    synchronized (this) {
      exception = null;
      status = 0;
    }
  }

  public static ForkJoinPool getPool() {
    Thread t = Thread.currentThread();
    return (t instanceof ForkJoinWorkerThread) ? ((ForkJoinWorkerThread) t).pool : null;
  }

  public static boolean inForkJoinPool() {
    return Thread.currentThread() instanceof ForkJoinWorkerThread;
  }

  public boolean tryUnfork() {
    Thread t = Thread.currentThread();
    if (t instanceof ForkJoinWorkerThread) {
      ForkJoinWorkerThread w = (ForkJoinWorkerThread) t;
      return w.pool.tryUnpush(w.workQueue, this);
    }
    return ForkJoinPool.common.tryRemoveSubmission(this);
  }

  public static int getQueuedTaskCount() {
    Thread t = Thread.currentThread();
    if (t instanceof ForkJoinWorkerThread) {
      ForkJoinWorkerThread w = (ForkJoinWorkerThread) t;
      return w.pool.queueSize(w.workQueue);
    }
    return ForkJoinPool.common.submissionsBy(t);
  }

  public static int getSurplusQueuedTaskCount() {
    Thread t = Thread.currentThread();
    if (t instanceof ForkJoinWorkerThread) {
      ForkJoinWorkerThread w = (ForkJoinWorkerThread) t;
      ForkJoinPool p = w.pool;
      int parallelism = p.getParallelism();
      int a = p.getRunningThreadCount();
      int n = p.queueSize(w.workQueue);
      int half = parallelism >>> 1;
      int surplus = a > half ? 0 : a > (half >>>= 1) ? 1 : a > (half >>>= 1) ? 2 : a > (half >>>= 1) ? 4 : 8;
      return n - surplus;
    }
    return 0;
  }

  public static void helpQuiesce() {
    Thread t = Thread.currentThread();
    if (t instanceof ForkJoinWorkerThread) {
      ForkJoinWorkerThread w = (ForkJoinWorkerThread) t;
      w.pool.helpQuiesce(w.workQueue);
    } else {
      ForkJoinPool.common.awaitQuiescence(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    }
  }

  public abstract V getRawResult();

  protected abstract void setRawResult(V value);

  protected abstract boolean exec();

  public final short getForkJoinTaskTag() {
    return (short) status;
  }

  public final synchronized short setForkJoinTaskTag(short newValue) {
    int s = status;
    status = (s & ~SMASK) | (newValue & SMASK);
    return (short) s;
  }

  public final synchronized boolean compareAndSetForkJoinTaskTag(short expect, short update) {
    int s = status;
    if ((short) s != expect) {
      return false;
    }
    status = (s & ~SMASK) | (update & SMASK);
    return true;
  }

  static final class AdaptedRunnable<T> extends ForkJoinTask<T> implements RunnableFuture<T> {
    final Runnable runnable;
    T result;

    AdaptedRunnable(Runnable runnable, T result) {
      if (runnable == null) {
        throw new NullPointerException();
      }
      this.runnable = runnable;
      this.result = result;
    }

    public final T getRawResult() {
      return result;
    }

    public final void setRawResult(T v) {
      result = v;
    }

    public final boolean exec() {
      runnable.run();
      return true;
    }

    public final void run() {
      invoke();
    }

    public String toString() {
      return super.toString() + "[Wrapped task = " + runnable + "]";
    }
  }

  static final class AdaptedRunnableAction extends ForkJoinTask<Void> implements RunnableFuture<Void> {
    final Runnable runnable;

    AdaptedRunnableAction(Runnable runnable) {
      if (runnable == null) {
        throw new NullPointerException();
      }
      this.runnable = runnable;
    }

    public final Void getRawResult() {
      return null;
    }

    public final void setRawResult(Void v) { }

    public final boolean exec() {
      runnable.run();
      return true;
    }

    public final void run() {
      invoke();
    }

    public String toString() {
      return super.toString() + "[Wrapped task = " + runnable + "]";
    }
  }

  static final class RunnableExecuteAction extends ForkJoinTask<Void> {
    final Runnable runnable;

    RunnableExecuteAction(Runnable runnable) {
      if (runnable == null) {
        throw new NullPointerException();
      }
      this.runnable = runnable;
    }

    public final Void getRawResult() {
      return null;
    }

    public final void setRawResult(Void v) { }

    public final boolean exec() {
      runnable.run();
      return true;
    }

    @Override
    void internalPropagateException(Throwable ex) {
      ForkJoinTask.<RuntimeException>uncheckedThrow(ex);
    }
  }

  static final class AdaptedCallable<T> extends ForkJoinTask<T> implements RunnableFuture<T> {
    final Callable<? extends T> callable;
    T result;

    AdaptedCallable(Callable<? extends T> callable) {
      if (callable == null) {
        throw new NullPointerException();
      }
      this.callable = callable;
    }

    public final T getRawResult() {
      return result;
    }

    public final void setRawResult(T v) {
      result = v;
    }

    public final boolean exec() {
      try {
        result = callable.call();
        return true;
      } catch (RuntimeException rex) {
        throw rex;
      } catch (Exception ex) {
        throw new RuntimeException(ex);
      }
    }

    public final void run() {
      invoke();
    }

    public String toString() {
      return super.toString() + "[Wrapped task = " + callable + "]";
    }
  }

  static final class AdaptedInterruptibleCallable<T> extends ForkJoinTask<T> implements RunnableFuture<T> {
    final Callable<? extends T> callable;
    transient volatile Thread runner;
    T result;

    AdaptedInterruptibleCallable(Callable<? extends T> callable) {
      if (callable == null) {
        throw new NullPointerException();
      }
      this.callable = callable;
    }

    public final T getRawResult() {
      return result;
    }

    public final void setRawResult(T v) {
      result = v;
    }

    public final boolean exec() {
      Thread.interrupted();
      runner = Thread.currentThread();
      try {
        if (!isDone()) {
          result = callable.call();
        }
        return true;
      } catch (RuntimeException rex) {
        throw rex;
      } catch (Exception ex) {
        ForkJoinTask.<RuntimeException>uncheckedThrow(ex);
        return false;
      } finally {
        runner = null;
        Thread.interrupted();
      }
    }

    public final void run() {
      invoke();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
      Thread t;
      boolean stat = super.cancel(false);
      if (mayInterruptIfRunning && (t = runner) != null) {
        try {
          t.interrupt();
        } catch (Throwable ignore) { }
      }
      return stat;
    }

    public String toString() {
      return super.toString() + "[Wrapped task = " + callable + "]";
    }
  }

  public static ForkJoinTask<?> adapt(Runnable runnable) {
    return new AdaptedRunnableAction(runnable);
  }

  public static <T> ForkJoinTask<T> adapt(Runnable runnable, T result) {
    return new AdaptedRunnable<T>(runnable, result);
  }

  public static <T> ForkJoinTask<T> adapt(Callable<? extends T> callable) {
    return new AdaptedCallable<T>(callable);
  }

  public static <T> ForkJoinTask<T> adaptInterruptible(Callable<? extends T> callable) {
    return new AdaptedInterruptibleCallable<T>(callable);
  }

  public static ForkJoinTask<?> adaptInterruptible(Runnable runnable) {
    return adaptInterruptible(runnable, null);
  }

  public static <T> ForkJoinTask<T> adaptInterruptible(Runnable runnable, T result) {
    Objects.requireNonNull(runnable);
    return new AdaptedInterruptibleCallable<T>(new Callable<T>() {
      public T call() {
        runnable.run();
        return result;
      }
    });
  }
}
