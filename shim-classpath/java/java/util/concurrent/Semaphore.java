/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.util.concurrent;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

public class Semaphore implements Serializable {
  private static final long serialVersionUID = -3222578661600680210L;

  private final boolean fair;
  private final Object monitor = new Object();
  private int permits;
  private int waiters;

  public Semaphore(int permits) {
    this(permits, false);
  }

  public Semaphore(int permits, boolean fair) {
    this.permits = permits;
    this.fair = fair;
  }

  public void acquire() throws InterruptedException {
    acquire(1);
  }

  public void acquire(int permits) throws InterruptedException {
    if (permits < 0) {
      throw new IllegalArgumentException();
    }
    if (Thread.interrupted()) {
      throw new InterruptedException();
    }
    synchronized (monitor) {
      waiters++;
      try {
        while (this.permits < permits) {
          monitor.wait();
        }
        this.permits -= permits;
      } finally {
        waiters--;
      }
    }
  }

  public void acquireUninterruptibly() {
    acquireUninterruptibly(1);
  }

  public void acquireUninterruptibly(int permits) {
    if (permits < 0) {
      throw new IllegalArgumentException();
    }
    boolean interrupted = false;
    synchronized (monitor) {
      waiters++;
      try {
        while (this.permits < permits) {
          try {
            monitor.wait();
          } catch (InterruptedException e) {
            interrupted = true;
          }
        }
        this.permits -= permits;
      } finally {
        waiters--;
        if (interrupted) {
          Thread.currentThread().interrupt();
        }
      }
    }
  }

  public boolean tryAcquire() {
    return tryAcquire(1);
  }

  public boolean tryAcquire(int permits) {
    if (permits < 0) {
      throw new IllegalArgumentException();
    }
    synchronized (monitor) {
      if (this.permits >= permits) {
        this.permits -= permits;
        return true;
      }
      return false;
    }
  }

  public boolean tryAcquire(long timeout, TimeUnit unit) throws InterruptedException {
    return tryAcquire(1, timeout, unit);
  }

  public boolean tryAcquire(int permits, long timeout, TimeUnit unit) throws InterruptedException {
    if (permits < 0) {
      throw new IllegalArgumentException();
    }
    if (Thread.interrupted()) {
      throw new InterruptedException();
    }
    long nanos = unit.toNanos(timeout);
    if (nanos <= 0) {
      return tryAcquire(permits);
    }
    long deadline = System.nanoTime() + nanos;
    synchronized (monitor) {
      waiters++;
      try {
        while (this.permits < permits) {
          long remaining = deadline - System.nanoTime();
          if (remaining <= 0) {
            return false;
          }
          long millis = remaining / 1000000L;
          int remNanos = (int) (remaining % 1000000L);
          monitor.wait(millis, remNanos);
        }
        this.permits -= permits;
        return true;
      } finally {
        waiters--;
      }
    }
  }

  public void release() {
    release(1);
  }

  public void release(int permits) {
    if (permits < 0) {
      throw new IllegalArgumentException();
    }
    synchronized (monitor) {
      this.permits += permits;
      monitor.notifyAll();
    }
  }

  public int availablePermits() {
    synchronized (monitor) {
      return permits;
    }
  }

  public int drainPermits() {
    synchronized (monitor) {
      int p = permits;
      permits = 0;
      return p;
    }
  }

  protected void reducePermits(int reduction) {
    if (reduction < 0) {
      throw new IllegalArgumentException();
    }
    synchronized (monitor) {
      permits -= reduction;
    }
  }

  public boolean isFair() {
    return fair;
  }

  public boolean hasQueuedThreads() {
    synchronized (monitor) {
      return waiters > 0;
    }
  }

  public int getQueueLength() {
    synchronized (monitor) {
      return waiters;
    }
  }

  @Override
  public String toString() {
    return super.toString() + "[Permits = " + availablePermits() + "]";
  }
}
