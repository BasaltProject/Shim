/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.util.concurrent;

public abstract class CountedCompleter<T> extends ForkJoinTask<T> {
  private static final long serialVersionUID = 5232453752276485070L;

  final CountedCompleter<?> completer;
  volatile int pending;

  protected CountedCompleter(CountedCompleter<?> completer, int initialPendingCount) {
    this.completer = completer;
    this.pending = initialPendingCount;
  }

  protected CountedCompleter(CountedCompleter<?> completer) {
    this.completer = completer;
  }

  protected CountedCompleter() {
    this.completer = null;
  }

  public abstract void compute();

  public void onCompletion(CountedCompleter<?> caller) { }

  public boolean onExceptionalCompletion(Throwable ex, CountedCompleter<?> caller) {
    return true;
  }

  public final CountedCompleter<?> getCompleter() {
    return completer;
  }

  public final int getPendingCount() {
    return pending;
  }

  public final void setPendingCount(int count) {
    pending = count;
  }

  public final synchronized void addToPendingCount(int delta) {
    pending += delta;
  }

  public final synchronized boolean compareAndSetPendingCount(int expected, int count) {
    if (pending == expected) {
      pending = count;
      return true;
    }
    return false;
  }

  public final synchronized int decrementPendingCountUnlessZero() {
    int c = pending;
    if (c != 0) {
      pending = c - 1;
    }
    return c;
  }

  public final CountedCompleter<?> getRoot() {
    CountedCompleter<?> a = this;
    CountedCompleter<?> p;
    while ((p = a.completer) != null) {
      a = p;
    }
    return a;
  }

  public final void tryComplete() {
    CountedCompleter<?> a = this;
    CountedCompleter<?> s = a;
    for (;;) {
      int c;
      synchronized (a) {
        c = a.pending;
        if (c != 0) {
          a.pending = c - 1;
        }
      }
      if (c == 0) {
        a.onCompletion(s);
        s = a;
        a = a.completer;
        if (a == null) {
          s.quietlyComplete();
          return;
        }
      } else {
        return;
      }
    }
  }

  public final void propagateCompletion() {
    CountedCompleter<?> a = this;
    CountedCompleter<?> s;
    for (;;) {
      int c;
      synchronized (a) {
        c = a.pending;
        if (c != 0) {
          a.pending = c - 1;
        }
      }
      if (c == 0) {
        s = a;
        a = a.completer;
        if (a == null) {
          s.quietlyComplete();
          return;
        }
      } else {
        return;
      }
    }
  }

  @Override
  public void complete(T rawResult) {
    CountedCompleter<?> p;
    setRawResult(rawResult);
    onCompletion(this);
    quietlyComplete();
    if ((p = completer) != null) {
      p.tryComplete();
    }
  }

  public final CountedCompleter<?> firstComplete() {
    for (;;) {
      int c;
      synchronized (this) {
        c = pending;
        if (c != 0) {
          pending = c - 1;
        }
      }
      if (c == 0) {
        return this;
      }
      return null;
    }
  }

  public final CountedCompleter<?> nextComplete() {
    CountedCompleter<?> p;
    if ((p = completer) != null) {
      return p.firstComplete();
    }
    quietlyComplete();
    return null;
  }

  public final void quietlyCompleteRoot() {
    for (CountedCompleter<?> a = this, p;;) {
      if ((p = a.completer) == null) {
        a.quietlyComplete();
        return;
      }
      a = p;
    }
  }

  public final void helpComplete(int maxTasks) {
    Thread t = Thread.currentThread();
    if (maxTasks > 0 && status >= 0) {
      ForkJoinPool pool = (t instanceof ForkJoinWorkerThread) ? ((ForkJoinWorkerThread) t).pool : this.pool;
      if (pool == null) {
        pool = ForkJoinPool.common;
      }
      pool.helpComplete(this, maxTasks);
    }
  }

  @Override
  void internalPropagateException(Throwable ex) {
    CountedCompleter<?> a = this;
    CountedCompleter<?> s = a;
    while (a.onExceptionalCompletion(ex, s) && (a = (s = a).completer) != null && a.status >= 0
        && a.trySetThrown(ex)) {
      continue;
    }
  }

  @Override
  protected final boolean exec() {
    compute();
    return false;
  }

  @Override
  public T getRawResult() {
    return null;
  }

  @Override
  protected void setRawResult(T t) { }
}
