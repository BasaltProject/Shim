/*
 * Based on Avian (https://github.com/ReadyTalk/avian).
 * Original work Copyright (c) 2008-2015, Avian Contributors.
 * Original work licensed under ISC; see LICENSE.txt:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE.txt
 *
 * Modifications Copyright (c) 2026 BasaltMC.
 * Modifications licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: ISC AND MIT
 */

/* Copyright (c) 2008-2015, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.util.concurrent;

public class FutureTask<V> implements RunnableFuture<V> {
  private static final int NEW = 0;
  private static final int COMPLETING = 1;
  private static final int NORMAL = 2;
  private static final int EXCEPTIONAL = 3;
  private static final int CANCELLED = 4;
  private static final int INTERRUPTING = 5;
  private static final int INTERRUPTED = 6;

  private volatile int state;
  private Callable<V> callable;
  private Object outcome;
  private volatile Thread runner;

  @SuppressWarnings("unchecked")
  private V report(int s) throws ExecutionException {
    Object x = outcome;
    if (s == NORMAL) {
      return (V) x;
    }
    if (s >= CANCELLED) {
      throw new CancellationException();
    }
    throw new ExecutionException((Throwable) x);
  }

  public FutureTask(Callable<V> callable) {
    if (callable == null) {
      throw new NullPointerException();
    }
    this.callable = callable;
    this.state = NEW;
  }

  public FutureTask(Runnable runnable, V result) {
    this.callable = Executors.callable(runnable, result);
    this.state = NEW;
  }

  public boolean isCancelled() {
    return state >= CANCELLED;
  }

  public boolean isDone() {
    return state != NEW;
  }

  public boolean cancel(boolean mayInterruptIfRunning) {
    synchronized (this) {
      if (state != NEW) {
        return false;
      }
      state = mayInterruptIfRunning ? INTERRUPTING : CANCELLED;
      if (mayInterruptIfRunning) {
        try {
          Thread t = runner;
          if (t != null) {
            t.interrupt();
          }
        } finally {
          state = INTERRUPTED;
        }
      }
      callable = null;
      notifyAll();
    }
    done();
    return true;
  }

  public V get() throws InterruptedException, ExecutionException {
    int s;
    synchronized (this) {
      while ((s = state) <= COMPLETING) {
        wait();
      }
    }
    return report(s);
  }

  public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
    if (unit == null) {
      throw new NullPointerException();
    }
    long nanos = unit.toNanos(timeout);
    long deadline = System.nanoTime() + nanos;
    int s;
    synchronized (this) {
      while ((s = state) <= COMPLETING) {
        if (nanos <= 0L) {
          throw new TimeoutException();
        }
        wait(nanos / 1000000L, (int) (nanos % 1000000L));
        nanos = deadline - System.nanoTime();
      }
    }
    return report(s);
  }

  public V resultNow() {
    switch (state()) {
    case SUCCESS:
      @SuppressWarnings("unchecked")
      V result = (V) outcome;
      return result;
    case FAILED:
      throw new IllegalStateException("Task completed with exception");
    case CANCELLED:
      throw new IllegalStateException("Task was cancelled");
    default:
      throw new IllegalStateException("Task has not completed");
    }
  }

  public Throwable exceptionNow() {
    switch (state()) {
    case SUCCESS:
      throw new IllegalStateException("Task completed with a result");
    case FAILED:
      return (Throwable) outcome;
    case CANCELLED:
      throw new IllegalStateException("Task was cancelled");
    default:
      throw new IllegalStateException("Task has not completed");
    }
  }

  public State state() {
    int s = state;
    while (s == COMPLETING) {
      Thread.yield();
      s = state;
    }
    switch (s) {
    case NORMAL:
      return State.SUCCESS;
    case EXCEPTIONAL:
      return State.FAILED;
    case CANCELLED:
    case INTERRUPTING:
    case INTERRUPTED:
      return State.CANCELLED;
    default:
      return State.RUNNING;
    }
  }

  protected void done() { }

  protected void set(V v) {
    synchronized (this) {
      if (state != NEW) {
        return;
      }
      outcome = v;
      state = NORMAL;
      callable = null;
      notifyAll();
    }
    done();
  }

  protected void setException(Throwable t) {
    synchronized (this) {
      if (state != NEW) {
        return;
      }
      outcome = t;
      state = EXCEPTIONAL;
      callable = null;
      notifyAll();
    }
    done();
  }

  private boolean claim() {
    synchronized (this) {
      if (state != NEW || runner != null) {
        return false;
      }
      runner = Thread.currentThread();
      return true;
    }
  }

  public void run() {
    if (!claim()) {
      return;
    }
    try {
      Callable<V> c = callable;
      if (c != null && state == NEW) {
        V result;
        boolean ran;
        try {
          result = c.call();
          ran = true;
        } catch (Throwable ex) {
          result = null;
          ran = false;
          setException(ex);
        }
        if (ran) {
          set(result);
        }
      }
    } finally {
      runner = null;
      int s = state;
      if (s >= INTERRUPTING) {
        handlePossibleCancellationInterrupt(s);
      }
    }
  }

  protected boolean runAndReset() {
    if (!claim()) {
      return false;
    }
    boolean ran = false;
    int s = state;
    try {
      Callable<V> c = callable;
      if (c != null && s == NEW) {
        try {
          c.call();
          ran = true;
        } catch (Throwable ex) {
          setException(ex);
        }
      }
    } finally {
      runner = null;
      s = state;
      if (s >= INTERRUPTING) {
        handlePossibleCancellationInterrupt(s);
      }
    }
    return ran && s == NEW;
  }

  private void handlePossibleCancellationInterrupt(int s) {
    if (s == INTERRUPTING) {
      while (state == INTERRUPTING) {
        Thread.yield();
      }
    }
  }

  public String toString() {
    final String status;
    switch (state) {
    case NORMAL:
      status = "[Completed normally]";
      break;
    case EXCEPTIONAL:
      status = "[Completed exceptionally: " + outcome + "]";
      break;
    case CANCELLED:
    case INTERRUPTING:
    case INTERRUPTED:
      status = "[Cancelled]";
      break;
    default:
      final Callable<?> callable = this.callable;
      status = (callable == null) ? "[Not completed]" : "[Not completed, task = " + callable + "]";
    }
    return super.toString() + status;
  }
}
