/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.util.concurrent.locks;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class ReentrantLock implements Lock, Serializable {
  private static final long serialVersionUID = 7373984872572414699L;

  private final boolean fair;
  private final Object sync = new Object();
  private Thread owner;
  private int holdCount;
  private int waiters;

  public ReentrantLock() {
    this(false);
  }

  public ReentrantLock(boolean fair) {
    this.fair = fair;
  }

  public void lock() {
    synchronized (sync) {
      Thread current = Thread.currentThread();
      if (owner == current) {
        holdCount++;
        return;
      }
      waiters++;
      boolean interrupted = false;
      try {
        while (owner != null) {
          try {
            sync.wait();
          } catch (InterruptedException e) {
            interrupted = true;
          }
        }
        owner = current;
        holdCount = 1;
      } finally {
        waiters--;
        if (interrupted) {
          current.interrupt();
        }
      }
    }
  }

  public void lockInterruptibly() throws InterruptedException {
    if (Thread.interrupted()) {
      throw new InterruptedException();
    }
    synchronized (sync) {
      Thread current = Thread.currentThread();
      if (owner == current) {
        holdCount++;
        return;
      }
      waiters++;
      try {
        while (owner != null) {
          sync.wait();
        }
        owner = current;
        holdCount = 1;
      } finally {
        waiters--;
      }
    }
  }

  public boolean tryLock() {
    synchronized (sync) {
      Thread current = Thread.currentThread();
      if (owner == null) {
        owner = current;
        holdCount = 1;
        return true;
      } else if (owner == current) {
        holdCount++;
        return true;
      }
      return false;
    }
  }

  public boolean tryLock(long timeout, TimeUnit unit) throws InterruptedException {
    if (Thread.interrupted()) {
      throw new InterruptedException();
    }
    long nanos = unit.toNanos(timeout);
    if (nanos <= 0) {
      return tryLock();
    }
    long deadline = System.nanoTime() + nanos;
    synchronized (sync) {
      Thread current = Thread.currentThread();
      if (owner == null) {
        owner = current;
        holdCount = 1;
        return true;
      } else if (owner == current) {
        holdCount++;
        return true;
      }
      waiters++;
      try {
        while (owner != null) {
          long remaining = deadline - System.nanoTime();
          if (remaining <= 0) {
            return false;
          }
          long millis = remaining / 1000000L;
          int remNanos = (int) (remaining % 1000000L);
          sync.wait(millis, remNanos);
        }
        owner = current;
        holdCount = 1;
        return true;
      } finally {
        waiters--;
      }
    }
  }

  public void unlock() {
    synchronized (sync) {
      Thread current = Thread.currentThread();
      if (owner != current) {
        throw new IllegalMonitorStateException();
      }
      holdCount--;
      if (holdCount == 0) {
        owner = null;
        sync.notifyAll();
      }
    }
  }

  int fullyRelease() {
    synchronized (sync) {
      Thread current = Thread.currentThread();
      if (owner != current) {
        throw new IllegalMonitorStateException();
      }
      int count = holdCount;
      holdCount = 0;
      owner = null;
      sync.notifyAll();
      return count;
    }
  }

  void reacquire(int count) {
    synchronized (sync) {
      Thread current = Thread.currentThread();
      boolean interrupted = false;
      waiters++;
      try {
        while (owner != null) {
          try {
            sync.wait();
          } catch (InterruptedException e) {
            interrupted = true;
          }
        }
        owner = current;
        holdCount = count;
      } finally {
        waiters--;
        if (interrupted) {
          current.interrupt();
        }
      }
    }
  }

  public Condition newCondition() {
    return new ConditionObject();
  }

  public int getHoldCount() {
    synchronized (sync) {
      return (owner == Thread.currentThread()) ? holdCount : 0;
    }
  }

  public boolean isHeldByCurrentThread() {
    synchronized (sync) {
      return owner == Thread.currentThread();
    }
  }

  public boolean isLocked() {
    synchronized (sync) {
      return holdCount > 0;
    }
  }

  public boolean isFair() {
    return fair;
  }

  protected Thread getOwner() {
    synchronized (sync) {
      return owner;
    }
  }

  public boolean hasQueuedThreads() {
    synchronized (sync) {
      return waiters > 0;
    }
  }

  public int getQueueLength() {
    synchronized (sync) {
      return waiters;
    }
  }

  public boolean hasWaiters(Condition condition) {
    if (condition == null) throw new NullPointerException();
    if (!(condition instanceof ConditionObject)) throw new IllegalArgumentException("not owner of condition");
    if (!isHeldByCurrentThread()) throw new IllegalMonitorStateException();
    ConditionObject c = (ConditionObject) condition;
    synchronized (c.condSync) {
      return c.condWaiters > 0;
    }
  }

  public int getWaitQueueLength(Condition condition) {
    if (condition == null) throw new NullPointerException();
    if (!(condition instanceof ConditionObject)) throw new IllegalArgumentException("not owner of condition");
    if (!isHeldByCurrentThread()) throw new IllegalMonitorStateException();
    ConditionObject c = (ConditionObject) condition;
    synchronized (c.condSync) {
      return c.condWaiters;
    }
  }

  @Override
  public String toString() {
    Thread o = getOwner();
    return super.toString() + ((o == null) ? "[Unlocked]" : "[Locked by thread " + o.getName() + "]");
  }

  public class ConditionObject implements Condition, Serializable {
    private static final long serialVersionUID = -5782782379348924978L;
    private final Object condSync = new Object();
    private int condWaiters;

    public void await() throws InterruptedException {
      if (Thread.interrupted()) {
        throw new InterruptedException();
      }
      int count = fullyRelease();
      boolean interrupted = false;
      synchronized (condSync) {
        condWaiters++;
        try {
          condSync.wait();
        } catch (InterruptedException e) {
          interrupted = true;
        } finally {
          condWaiters--;
        }
      }
      reacquire(count);
      if (interrupted || Thread.interrupted()) {
        throw new InterruptedException();
      }
    }

    public boolean await(long time, TimeUnit unit) throws InterruptedException {
      if (Thread.interrupted()) {
        throw new InterruptedException();
      }
      long nanos = unit.toNanos(time);
      if (nanos <= 0) {
        int count = fullyRelease();
        reacquire(count);
        return false;
      }
      long deadline = System.nanoTime() + nanos;
      int count = fullyRelease();
      boolean interrupted = false;
      boolean timedOut = false;
      synchronized (condSync) {
        condWaiters++;
        try {
          long remaining = deadline - System.nanoTime();
          if (remaining > 0) {
            long millis = remaining / 1000000L;
            int remNanos = (int) (remaining % 1000000L);
            condSync.wait(millis, remNanos);
          }
          if (System.nanoTime() >= deadline) {
            timedOut = true;
          }
        } catch (InterruptedException e) {
          interrupted = true;
        } finally {
          condWaiters--;
        }
      }
      reacquire(count);
      if (interrupted || Thread.interrupted()) {
        throw new InterruptedException();
      }
      return !timedOut;
    }

    public long awaitNanos(long nanosTimeout) throws InterruptedException {
      if (Thread.interrupted()) {
        throw new InterruptedException();
      }
      if (nanosTimeout <= 0) {
        int count = fullyRelease();
        reacquire(count);
        return 0;
      }
      long start = System.nanoTime();
      long deadline = start + nanosTimeout;
      int count = fullyRelease();
      boolean interrupted = false;
      synchronized (condSync) {
        condWaiters++;
        try {
          long remaining = deadline - System.nanoTime();
          if (remaining > 0) {
            long millis = remaining / 1000000L;
            int remNanos = (int) (remaining % 1000000L);
            condSync.wait(millis, remNanos);
          }
        } catch (InterruptedException e) {
          interrupted = true;
        } finally {
          condWaiters--;
        }
      }
      reacquire(count);
      if (interrupted || Thread.interrupted()) {
        throw new InterruptedException();
      }
      long elapsed = System.nanoTime() - start;
      return nanosTimeout - elapsed;
    }

    public void awaitUninterruptibly() {
      int count = fullyRelease();
      synchronized (condSync) {
        condWaiters++;
        boolean interrupted = false;
        try {
          while (true) {
            try {
              condSync.wait();
              break;
            } catch (InterruptedException e) {
              interrupted = true;
            }
          }
        } finally {
          condWaiters--;
          if (interrupted) {
            Thread.currentThread().interrupt();
          }
        }
      }
      reacquire(count);
    }

    public boolean awaitUntil(Date deadline) throws InterruptedException {
      Objects.requireNonNull(deadline);
      long millis = deadline.getTime() - System.currentTimeMillis();
      return await(millis, TimeUnit.MILLISECONDS);
    }

    public void signal() {
      if (!isHeldByCurrentThread()) {
        throw new IllegalMonitorStateException();
      }
      synchronized (condSync) {
        condSync.notify();
      }
    }

    public void signalAll() {
      if (!isHeldByCurrentThread()) {
        throw new IllegalMonitorStateException();
      }
      synchronized (condSync) {
        condSync.notifyAll();
      }
    }
  }
}
