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

  private static final java.io.ObjectStreamField[] serialPersistentFields = {
    new java.io.ObjectStreamField("sync", Sync.class)
  };

  abstract static class Sync extends AbstractQueuedSynchronizer {
    private static final long serialVersionUID = -5179523762034025860L;

    private void readObject(java.io.ObjectInputStream s) throws java.io.IOException, ClassNotFoundException {
      s.defaultReadObject();
      setState(0);
    }
  }

  static final class NonfairSync extends Sync {
    private static final long serialVersionUID = 7316153563782823691L;
  }

  static final class FairSync extends Sync {
    private static final long serialVersionUID = -3000897897090466540L;
  }

  private transient boolean fair;
  private transient Object monitor = new Object();
  private transient Thread owner;
  private transient int holdCount;
  private transient int waiters;
  private transient java.util.Set<Thread> queued = new java.util.LinkedHashSet<Thread>();

  private transient Sync serialSync;

  private synchronized Sync serialSync() {
    if (serialSync == null) {
      serialSync = fair ? new FairSync() : new NonfairSync();
    }
    return serialSync;
  }

  static Condition serialCondition(ReentrantLock lock) {
    return lock.serialSync().new ConditionObject();
  }

  private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException {
    java.io.ObjectOutputStream.PutField f = s.putFields();
    f.put("sync", serialSync());
    s.writeFields();
  }

  private void readObject(java.io.ObjectInputStream s) throws java.io.IOException, ClassNotFoundException {
    Object sy = s.readFields().get("sync", null);
    fair = sy instanceof FairSync;
    monitor = new Object();
    queued = new java.util.LinkedHashSet<Thread>();
  }

  public ReentrantLock() {
    this(false);
  }

  public ReentrantLock(boolean fair) {
    this.fair = fair;
  }

  public void lock() {
    synchronized (monitor) {
      Thread current = Thread.currentThread();
      if (owner == current) {
        holdCount++;
        return;
      }
      if (owner == null) {
        owner = current;
        holdCount = 1;
        return;
      }
      waiters++;
      queued.add(Thread.currentThread());
      boolean interrupted = false;
      try {
        while (owner != null) {
          try {
            monitor.wait();
          } catch (InterruptedException e) {
            interrupted = true;
          }
        }
        owner = current;
        holdCount = 1;
      } finally {
        waiters--;
      queued.remove(Thread.currentThread());
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
    synchronized (monitor) {
      Thread current = Thread.currentThread();
      if (owner == current) {
        holdCount++;
        return;
      }
      waiters++;
      queued.add(Thread.currentThread());
      try {
        while (owner != null) {
          monitor.wait();
        }
        owner = current;
        holdCount = 1;
      } finally {
        waiters--;
      queued.remove(Thread.currentThread());
      }
    }
  }

  public boolean tryLock() {
    synchronized (monitor) {
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
    synchronized (monitor) {
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
      queued.add(Thread.currentThread());
      try {
        while (owner != null) {
          long remaining = deadline - System.nanoTime();
          if (remaining <= 0) {
            return false;
          }
          long millis = remaining / 1000000L;
          int remNanos = (int) (remaining % 1000000L);
          monitor.wait(millis, remNanos);
        }
        owner = current;
        holdCount = 1;
        return true;
      } finally {
        waiters--;
      queued.remove(Thread.currentThread());
      }
    }
  }

  public void unlock() {
    synchronized (monitor) {
      Thread current = Thread.currentThread();
      if (owner != current) {
        throw new IllegalMonitorStateException();
      }
      holdCount--;
      if (holdCount == 0) {
        owner = null;
        if (waiters > 0) {
          monitor.notifyAll();
        }
      }
    }
  }

  int fullyRelease() {
    synchronized (monitor) {
      Thread current = Thread.currentThread();
      if (owner != current) {
        throw new IllegalMonitorStateException();
      }
      int count = holdCount;
      holdCount = 0;
      owner = null;
      if (waiters > 0) {
        monitor.notifyAll();
      }
      return count;
    }
  }

  void reacquire(int count) {
    synchronized (monitor) {
      Thread current = Thread.currentThread();
      boolean interrupted = false;
      waiters++;
      queued.add(Thread.currentThread());
      try {
        while (owner != null) {
          try {
            monitor.wait();
          } catch (InterruptedException e) {
            interrupted = true;
          }
        }
        owner = current;
        holdCount = count;
      } finally {
        waiters--;
      queued.remove(Thread.currentThread());
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
    synchronized (monitor) {
      return (owner == Thread.currentThread()) ? holdCount : 0;
    }
  }

  public boolean isHeldByCurrentThread() {
    synchronized (monitor) {
      return owner == Thread.currentThread();
    }
  }

  public boolean isLocked() {
    synchronized (monitor) {
      return holdCount > 0;
    }
  }

  public boolean isFair() {
    return fair;
  }

  protected Thread getOwner() {
    synchronized (monitor) {
      return owner;
    }
  }

  public boolean hasQueuedThreads() {
    synchronized (monitor) {
      return waiters > 0;
    }
  }

  public final boolean hasQueuedThread(Thread thread) {
    java.util.Objects.requireNonNull(thread);
    synchronized (monitor) {
      return queued.contains(thread);
    }
  }

  public int getQueueLength() {
    synchronized (monitor) {
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
    private final java.util.ArrayDeque<boolean[]> waitQ = new java.util.ArrayDeque<boolean[]>();

    private long awaitImpl(boolean timed, long nanos, boolean uninterruptible, boolean[] timedOut)
        throws InterruptedException {
      if (!uninterruptible && Thread.interrupted()) {
        throw new InterruptedException();
      }
      long deadline = timed ? System.nanoTime() + nanos : 0L;
      boolean[] token = new boolean[1];
      boolean interrupted = false;
      boolean interruptedBefore = false;
      int count;
      synchronized (condSync) {
        count = fullyRelease();
        waitQ.addLast(token);
        condWaiters++;
        try {
          while (!token[0]) {
            try {
              if (timed) {
                long remaining = deadline - System.nanoTime();
                if (remaining <= 0) {
                  if (waitQ.remove(token)) {
                    condWaiters--;
                    timedOut[0] = true;
                  }
                  break;
                }
                condSync.wait(remaining / 1000000L, (int) (remaining % 1000000L));
              } else {
                condSync.wait();
              }
            } catch (InterruptedException e) {
              interrupted = true;
              if (!uninterruptible) {
                if (waitQ.remove(token)) {
                  condWaiters--;
                  interruptedBefore = true;
                }
                break;
              }
            }
          }
        } finally {
          if (waitQ.remove(token)) {
            condWaiters--;
          }
        }
      }
      reacquire(count);
      if (interrupted) {
        if (interruptedBefore) {
          throw new InterruptedException();
        }
        Thread.currentThread().interrupt();
      }
      return timed ? deadline - System.nanoTime() : 0L;
    }

    public void await() throws InterruptedException {
      awaitImpl(false, 0L, false, new boolean[1]);
    }

    public boolean await(long time, TimeUnit unit) throws InterruptedException {
      boolean[] timedOut = new boolean[1];
      awaitImpl(true, unit.toNanos(time), false, timedOut);
      return !timedOut[0];
    }

    public long awaitNanos(long nanosTimeout) throws InterruptedException {
      return awaitImpl(true, nanosTimeout, false, new boolean[1]);
    }

    public void awaitUninterruptibly() {
      try {
        awaitImpl(false, 0L, true, new boolean[1]);
      } catch (InterruptedException impossible) {
        throw new AssertionError(impossible);
      }
    }

    public boolean awaitUntil(Date deadline) throws InterruptedException {
      Objects.requireNonNull(deadline);
      boolean[] timedOut = new boolean[1];
      awaitImpl(true, (deadline.getTime() - System.currentTimeMillis()) * 1000000L, false, timedOut);
      return !timedOut[0];
    }

    public void signal() {
      if (!isHeldByCurrentThread()) {
        throw new IllegalMonitorStateException();
      }
      synchronized (condSync) {
        boolean[] t = waitQ.pollFirst();
        if (t != null) {
          condWaiters--;
          t[0] = true;
          condSync.notifyAll();
        }
      }
    }

    public void signalAll() {
      if (!isHeldByCurrentThread()) {
        throw new IllegalMonitorStateException();
      }
      synchronized (condSync) {
        boolean[] t;
        while ((t = waitQ.pollFirst()) != null) {
          condWaiters--;
          t[0] = true;
        }
        condSync.notifyAll();
      }
    }
  }
}
