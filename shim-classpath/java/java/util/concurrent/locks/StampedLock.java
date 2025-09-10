/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.util.concurrent.locks;

import java.io.Serializable;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class StampedLock implements Serializable {
  private static final long serialVersionUID = -6001602636862214147L;

  private static final long WRITE_BIT = 1L;
  private static final long READ_BIT = 2L;
  private static final long SEQUENCE_SHIFT = 8L;
  private static final long SEQUENCE_MASK = ~255L;

  private final Object sync = new Object();
  private long sequence = 1L;
  private int readers = 0;
  private Thread writer = null;

  public StampedLock() {
  }

  public long writeLock() {
    synchronized (sync) {
      Thread current = Thread.currentThread();
      while (writer != null || readers > 0) {
        try {
          sync.wait();
        } catch (InterruptedException e) {
          // writeLock is uninterruptible
          current.interrupt();
        }
      }
      sequence++;
      writer = current;
      return (sequence << SEQUENCE_SHIFT) | WRITE_BIT;
    }
  }

  public long tryWriteLock() {
    synchronized (sync) {
      if (writer == null && readers == 0) {
        sequence++;
        writer = Thread.currentThread();
        return (sequence << SEQUENCE_SHIFT) | WRITE_BIT;
      }
      return 0L;
    }
  }

  public long tryWriteLock(long timeout, TimeUnit unit) throws InterruptedException {
    Objects.requireNonNull(unit, "unit");
    long nanos = unit.toNanos(timeout);
    long deadline = System.nanoTime() + nanos;

    synchronized (sync) {
      while (writer != null || readers > 0) {
        long remaining = deadline - System.nanoTime();
        if (remaining <= 0L) {
          return 0L;
        }
        long millis = remaining / 1_000_000L;
        int remNanos = (int) (remaining % 1_000_000L);
        sync.wait(millis, remNanos);
      }
      sequence++;
      writer = Thread.currentThread();
      return (sequence << SEQUENCE_SHIFT) | WRITE_BIT;
    }
  }

  public void unlockWrite(long stamp) {
    synchronized (sync) {
      if ((stamp & WRITE_BIT) == 0L || writer != Thread.currentThread()) {
        throw new IllegalMonitorStateException("invalid write lock stamp");
      }
      writer = null;
      sequence++;
      sync.notifyAll();
    }
  }

  public long readLock() {
    synchronized (sync) {
      Thread current = Thread.currentThread();
      while (writer != null) {
        try {
          sync.wait();
        } catch (InterruptedException e) {
          // readLock is uninterruptible
          current.interrupt();
        }
      }
      readers++;
      return (sequence << SEQUENCE_SHIFT) | READ_BIT;
    }
  }

  public long tryReadLock() {
    synchronized (sync) {
      if (writer == null) {
        readers++;
        return (sequence << SEQUENCE_SHIFT) | READ_BIT;
      }
      return 0L;
    }
  }

  public long tryReadLock(long timeout, TimeUnit unit) throws InterruptedException {
    Objects.requireNonNull(unit, "unit");
    long nanos = unit.toNanos(timeout);
    long deadline = System.nanoTime() + nanos;

    synchronized (sync) {
      while (writer != null) {
        long remaining = deadline - System.nanoTime();
        if (remaining <= 0L) {
          return 0L;
        }
        long millis = remaining / 1_000_000L;
        int remNanos = (int) (remaining % 1_000_000L);
        sync.wait(millis, remNanos);
      }
      readers++;
      return (sequence << SEQUENCE_SHIFT) | READ_BIT;
    }
  }

  public void unlockRead(long stamp) {
    synchronized (sync) {
      if ((stamp & READ_BIT) == 0L || readers <= 0) {
        throw new IllegalMonitorStateException("invalid read lock stamp");
      }
      readers--;
      sync.notifyAll();
    }
  }

  public long tryOptimisticRead() {
    synchronized (sync) {
      if (writer != null) {
        return 0L;
      }
      return sequence << SEQUENCE_SHIFT;
    }
  }

  public boolean validate(long stamp) {
    synchronized (sync) {
      return stamp != 0L && (stamp & SEQUENCE_MASK) == (sequence << SEQUENCE_SHIFT) && writer == null;
    }
  }

  public void unlock(long stamp) {
    if (isWriteLockStamp(stamp)) {
      unlockWrite(stamp);
    } else if (isReadLockStamp(stamp)) {
      unlockRead(stamp);
    } else {
      throw new IllegalMonitorStateException("stamp is not a held lock stamp");
    }
  }

  public long tryConvertToWriteLock(long stamp) {
    synchronized (sync) {
      if (isWriteLockStamp(stamp)) {
        if (writer == Thread.currentThread()) {
          return stamp;
        }
        return 0L;
      }
      if (isReadLockStamp(stamp)) {
        if (readers == 1 && writer == null) {
          readers = 0;
          sequence++;
          writer = Thread.currentThread();
          return (sequence << SEQUENCE_SHIFT) | WRITE_BIT;
        }
        return 0L;
      }
      if (isOptimisticReadStamp(stamp)) {
        if (validate(stamp) && writer == null && readers == 0) {
          sequence++;
          writer = Thread.currentThread();
          return (sequence << SEQUENCE_SHIFT) | WRITE_BIT;
        }
        return 0L;
      }
      return 0L;
    }
  }

  public long tryConvertToReadLock(long stamp) {
    synchronized (sync) {
      if (isReadLockStamp(stamp)) {
        return stamp;
      }
      if (isWriteLockStamp(stamp)) {
        if (writer == Thread.currentThread()) {
          writer = null;
          readers = 1;
          sequence++;
          sync.notifyAll();
          return (sequence << SEQUENCE_SHIFT) | READ_BIT;
        }
        return 0L;
      }
      if (isOptimisticReadStamp(stamp)) {
        if (validate(stamp) && writer == null) {
          readers++;
          return (sequence << SEQUENCE_SHIFT) | READ_BIT;
        }
        return 0L;
      }
      return 0L;
    }
  }

  public long tryConvertToOptimisticRead(long stamp) {
    synchronized (sync) {
      if (isWriteLockStamp(stamp)) {
        if (writer == Thread.currentThread()) {
          writer = null;
          sequence++;
          sync.notifyAll();
          return sequence << SEQUENCE_SHIFT;
        }
        return 0L;
      }
      if (isReadLockStamp(stamp)) {
        if (readers > 0) {
          readers--;
          sync.notifyAll();
          return sequence << SEQUENCE_SHIFT;
        }
        return 0L;
      }
      if (validate(stamp)) {
        return stamp;
      }
      return 0L;
    }
  }

  public boolean isWriteLocked() {
    synchronized (sync) {
      return writer != null;
    }
  }

  public boolean isReadLocked() {
    synchronized (sync) {
      return readers > 0;
    }
  }

  public int getReadLockCount() {
    synchronized (sync) {
      return readers;
    }
  }

  public static boolean isWriteLockStamp(long stamp) {
    return (stamp & WRITE_BIT) != 0L;
  }

  public static boolean isReadLockStamp(long stamp) {
    return (stamp & READ_BIT) != 0L;
  }

  public static boolean isLockStamp(long stamp) {
    return (stamp & (WRITE_BIT | READ_BIT)) != 0L;
  }

  public static boolean isOptimisticReadStamp(long stamp) {
    return stamp != 0L && (stamp & (WRITE_BIT | READ_BIT)) == 0L;
  }

  public Lock asReadLock() {
    return new ReadLockView();
  }

  public Lock asWriteLock() {
    return new WriteLockView();
  }

  public ReadWriteLock asReadWriteLock() {
    return new ReadWriteLockView();
  }

  private class ReadLockView implements Lock {
    private final ThreadLocal<Long> holder = new ThreadLocal<>();

    @Override
    public void lock() {
      holder.set(readLock());
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
      if (Thread.interrupted()) throw new InterruptedException();
      holder.set(readLock());
    }

    @Override
    public boolean tryLock() {
      long s = tryReadLock();
      if (s != 0L) {
        holder.set(s);
        return true;
      }
      return false;
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
      long s = tryReadLock(time, unit);
      if (s != 0L) {
        holder.set(s);
        return true;
      }
      return false;
    }

    @Override
    public void unlock() {
      Long s = holder.get();
      if (s == null) throw new IllegalMonitorStateException();
      holder.remove();
      unlockRead(s);
    }

    @Override
    public Condition newCondition() {
      throw new UnsupportedOperationException();
    }
  }

  private class WriteLockView implements Lock {
    private final ThreadLocal<Long> holder = new ThreadLocal<>();

    @Override
    public void lock() {
      holder.set(writeLock());
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
      if (Thread.interrupted()) throw new InterruptedException();
      holder.set(writeLock());
    }

    @Override
    public boolean tryLock() {
      long s = tryWriteLock();
      if (s != 0L) {
        holder.set(s);
        return true;
      }
      return false;
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
      long s = tryWriteLock(time, unit);
      if (s != 0L) {
        holder.set(s);
        return true;
      }
      return false;
    }

    @Override
    public void unlock() {
      Long s = holder.get();
      if (s == null) throw new IllegalMonitorStateException();
      holder.remove();
      unlockWrite(s);
    }

    @Override
    public Condition newCondition() {
      throw new UnsupportedOperationException();
    }
  }

  private class ReadWriteLockView implements ReadWriteLock {
    @Override
    public Lock readLock() {
      return asReadLock();
    }

    @Override
    public Lock writeLock() {
      return asWriteLock();
    }
  }
}
