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

public class ReentrantReadWriteLock implements ReadWriteLock, Serializable {
  private static final long serialVersionUID = -6992448646407690164L;

  private final ReadLock readerLock;
  private final WriteLock writerLock;
  private final boolean fair;

  private final Object sync = new Object();
  private Thread writerOwner;
  private int writeHoldCount;
  private int readLockCount;
  private final ThreadLocal<Integer> readHolds = new ThreadLocal<Integer>();

  public ReentrantReadWriteLock() {
    this(false);
  }

  public ReentrantReadWriteLock(boolean fair) {
    this.fair = fair;
    this.readerLock = new ReadLock(this);
    this.writerLock = new WriteLock(this);
  }

  public ReadLock readLock() {
    return readerLock;
  }

  public WriteLock writeLock() {
    return writerLock;
  }

  public boolean isFair() {
    return fair;
  }

  public int getReadLockCount() {
    synchronized (sync) {
      return readLockCount;
    }
  }

  public boolean isWriteLocked() {
    synchronized (sync) {
      return writeHoldCount > 0;
    }
  }

  public boolean isWriteLockedByCurrentThread() {
    synchronized (sync) {
      return writerOwner == Thread.currentThread();
    }
  }

  public int getWriteHoldCount() {
    synchronized (sync) {
      return (writerOwner == Thread.currentThread()) ? writeHoldCount : 0;
    }
  }

  public int getReadHoldCount() {
    Integer c = readHolds.get();
    return c == null ? 0 : c.intValue();
  }

  public static class ReadLock implements Lock, Serializable {
    private static final long serialVersionUID = -5992448646407690164L;
    private final ReentrantReadWriteLock rwLock;

    protected ReadLock(ReentrantReadWriteLock lock) {
      this.rwLock = lock;
    }

    public void lock() {
      synchronized (rwLock.sync) {
        Thread current = Thread.currentThread();
        boolean interrupted = false;
        while (rwLock.writerOwner != null && rwLock.writerOwner != current) {
          try {
            rwLock.sync.wait();
          } catch (InterruptedException e) {
            interrupted = true;
          }
        }
        rwLock.readLockCount++;
        Integer h = rwLock.readHolds.get();
        rwLock.readHolds.set(h == null ? 1 : h + 1);
        if (interrupted) {
          current.interrupt();
        }
      }
    }

    public void lockInterruptibly() throws InterruptedException {
      if (Thread.interrupted()) {
        throw new InterruptedException();
      }
      synchronized (rwLock.sync) {
        Thread current = Thread.currentThread();
        while (rwLock.writerOwner != null && rwLock.writerOwner != current) {
          rwLock.sync.wait();
        }
        rwLock.readLockCount++;
        Integer h = rwLock.readHolds.get();
        rwLock.readHolds.set(h == null ? 1 : h + 1);
      }
    }

    public boolean tryLock() {
      synchronized (rwLock.sync) {
        Thread current = Thread.currentThread();
        if (rwLock.writerOwner != null && rwLock.writerOwner != current) {
          return false;
        }
        rwLock.readLockCount++;
        Integer h = rwLock.readHolds.get();
        rwLock.readHolds.set(h == null ? 1 : h + 1);
        return true;
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
      synchronized (rwLock.sync) {
        Thread current = Thread.currentThread();
        while (rwLock.writerOwner != null && rwLock.writerOwner != current) {
          long remaining = deadline - System.nanoTime();
          if (remaining <= 0) {
            return false;
          }
          long millis = remaining / 1000000L;
          int remNanos = (int) (remaining % 1000000L);
          rwLock.sync.wait(millis, remNanos);
        }
        rwLock.readLockCount++;
        Integer h = rwLock.readHolds.get();
        rwLock.readHolds.set(h == null ? 1 : h + 1);
        return true;
      }
    }

    public void unlock() {
      synchronized (rwLock.sync) {
        Integer h = rwLock.readHolds.get();
        if (h == null || h <= 0) {
          throw new IllegalMonitorStateException("attempt to unlock read lock, not locked by current thread");
        }
        if (h == 1) {
          rwLock.readHolds.remove();
        } else {
          rwLock.readHolds.set(h - 1);
        }
        rwLock.readLockCount--;
        if (rwLock.readLockCount == 0) {
          rwLock.sync.notifyAll();
        }
      }
    }

    public Condition newCondition() {
      throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
      return super.toString() + "[Read locks = " + rwLock.getReadLockCount() + "]";
    }
  }

  public static class WriteLock implements Lock, Serializable {
    private static final long serialVersionUID = -4992448646407690164L;
    private final ReentrantReadWriteLock rwLock;

    protected WriteLock(ReentrantReadWriteLock lock) {
      this.rwLock = lock;
    }

    public void lock() {
      synchronized (rwLock.sync) {
        Thread current = Thread.currentThread();
        if (rwLock.writerOwner == current) {
          rwLock.writeHoldCount++;
          return;
        }
        boolean interrupted = false;
        while (rwLock.writerOwner != null || rwLock.readLockCount > 0) {
          try {
            rwLock.sync.wait();
          } catch (InterruptedException e) {
            interrupted = true;
          }
        }
        rwLock.writerOwner = current;
        rwLock.writeHoldCount = 1;
        if (interrupted) {
          current.interrupt();
        }
      }
    }

    public void lockInterruptibly() throws InterruptedException {
      if (Thread.interrupted()) {
        throw new InterruptedException();
      }
      synchronized (rwLock.sync) {
        Thread current = Thread.currentThread();
        if (rwLock.writerOwner == current) {
          rwLock.writeHoldCount++;
          return;
        }
        while (rwLock.writerOwner != null || rwLock.readLockCount > 0) {
          rwLock.sync.wait();
        }
        rwLock.writerOwner = current;
        rwLock.writeHoldCount = 1;
      }
    }

    public boolean tryLock() {
      synchronized (rwLock.sync) {
        Thread current = Thread.currentThread();
        if (rwLock.writerOwner == current) {
          rwLock.writeHoldCount++;
          return true;
        }
        if (rwLock.writerOwner != null || rwLock.readLockCount > 0) {
          return false;
        }
        rwLock.writerOwner = current;
        rwLock.writeHoldCount = 1;
        return true;
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
      synchronized (rwLock.sync) {
        Thread current = Thread.currentThread();
        if (rwLock.writerOwner == current) {
          rwLock.writeHoldCount++;
          return true;
        }
        while (rwLock.writerOwner != null || rwLock.readLockCount > 0) {
          long remaining = deadline - System.nanoTime();
          if (remaining <= 0) {
            return false;
          }
          long millis = remaining / 1000000L;
          int remNanos = (int) (remaining % 1000000L);
          rwLock.sync.wait(millis, remNanos);
        }
        rwLock.writerOwner = current;
        rwLock.writeHoldCount = 1;
        return true;
      }
    }

    public void unlock() {
      synchronized (rwLock.sync) {
        Thread current = Thread.currentThread();
        if (rwLock.writerOwner != current) {
          throw new IllegalMonitorStateException();
        }
        rwLock.writeHoldCount--;
        if (rwLock.writeHoldCount == 0) {
          rwLock.writerOwner = null;
          rwLock.sync.notifyAll();
        }
      }
    }

    public Condition newCondition() {
      return new WriteCondition();
    }

    public boolean isHeldByCurrentThread() {
      synchronized (rwLock.sync) {
        return rwLock.writerOwner == Thread.currentThread();
      }
    }

    public int getHoldCount() {
      return rwLock.getWriteHoldCount();
    }

    @Override
    public String toString() {
      Thread o = rwLock.writerOwner;
      return super.toString() + ((o == null) ? "[Unlocked]" : "[Locked by thread " + o.getName() + "]");
    }

    private class WriteCondition implements Condition, Serializable {
      private static final long serialVersionUID = -3992448646407690164L;
      private final Object condSync = new Object();

      public void await() throws InterruptedException {
        if (Thread.interrupted()) throw new InterruptedException();
        if (!isHeldByCurrentThread()) throw new IllegalMonitorStateException();
        int count = rwLock.writeHoldCount;
        rwLock.writeHoldCount = 0;
        rwLock.writerOwner = null;
        rwLock.sync.notifyAll();
        boolean interrupted = false;
        synchronized (condSync) {
          try {
            condSync.wait();
          } catch (InterruptedException e) {
            interrupted = true;
          }
        }
        lock();
        rwLock.writeHoldCount = count;
        if (interrupted || Thread.interrupted()) throw new InterruptedException();
      }

      public boolean await(long time, TimeUnit unit) throws InterruptedException {
        if (Thread.interrupted()) throw new InterruptedException();
        if (!isHeldByCurrentThread()) throw new IllegalMonitorStateException();
        long nanos = unit.toNanos(time);
        long deadline = System.nanoTime() + nanos;
        int count = rwLock.writeHoldCount;
        rwLock.writeHoldCount = 0;
        rwLock.writerOwner = null;
        rwLock.sync.notifyAll();
        boolean interrupted = false;
        boolean timedOut = false;
        synchronized (condSync) {
          try {
            long remaining = deadline - System.nanoTime();
            if (remaining > 0) {
              long millis = remaining / 1000000L;
              int remNanos = (int) (remaining % 1000000L);
              condSync.wait(millis, remNanos);
            }
            if (System.nanoTime() >= deadline) timedOut = true;
          } catch (InterruptedException e) {
            interrupted = true;
          }
        }
        lock();
        rwLock.writeHoldCount = count;
        if (interrupted || Thread.interrupted()) throw new InterruptedException();
        return !timedOut;
      }

      public long awaitNanos(long nanosTimeout) throws InterruptedException {
        if (Thread.interrupted()) throw new InterruptedException();
        if (!isHeldByCurrentThread()) throw new IllegalMonitorStateException();
        long start = System.nanoTime();
        long deadline = start + nanosTimeout;
        int count = rwLock.writeHoldCount;
        rwLock.writeHoldCount = 0;
        rwLock.writerOwner = null;
        rwLock.sync.notifyAll();
        boolean interrupted = false;
        synchronized (condSync) {
          try {
            long remaining = deadline - System.nanoTime();
            if (remaining > 0) {
              long millis = remaining / 1000000L;
              int remNanos = (int) (remaining % 1000000L);
              condSync.wait(millis, remNanos);
            }
          } catch (InterruptedException e) {
            interrupted = true;
          }
        }
        lock();
        rwLock.writeHoldCount = count;
        if (interrupted || Thread.interrupted()) throw new InterruptedException();
        long elapsed = System.nanoTime() - start;
        return nanosTimeout - elapsed;
      }

      public void awaitUninterruptibly() {
        if (!isHeldByCurrentThread()) throw new IllegalMonitorStateException();
        int count = rwLock.writeHoldCount;
        rwLock.writeHoldCount = 0;
        rwLock.writerOwner = null;
        rwLock.sync.notifyAll();
        boolean interrupted = false;
        synchronized (condSync) {
          while (true) {
            try {
              condSync.wait();
              break;
            } catch (InterruptedException e) {
              interrupted = true;
            }
          }
        }
        lock();
        rwLock.writeHoldCount = count;
        if (interrupted) Thread.currentThread().interrupt();
      }

      public boolean awaitUntil(Date deadline) throws InterruptedException {
        Objects.requireNonNull(deadline);
        long millis = deadline.getTime() - System.currentTimeMillis();
        return await(millis, TimeUnit.MILLISECONDS);
      }

      public void signal() {
        if (!isHeldByCurrentThread()) throw new IllegalMonitorStateException();
        synchronized (condSync) {
          condSync.notify();
        }
      }

      public void signalAll() {
        if (!isHeldByCurrentThread()) throw new IllegalMonitorStateException();
        synchronized (condSync) {
          condSync.notifyAll();
        }
      }
    }
  }
}
