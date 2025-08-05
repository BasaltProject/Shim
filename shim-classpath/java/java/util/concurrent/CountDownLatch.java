/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.util.concurrent;

import java.util.concurrent.TimeUnit;

public class CountDownLatch {
  private final Object monitor = new Object();
  private int count;

  public CountDownLatch(int count) {
    if (count < 0) {
      throw new IllegalArgumentException("count < 0");
    }
    this.count = count;
  }

  public void await() throws InterruptedException {
    if (Thread.interrupted()) {
      throw new InterruptedException();
    }
    synchronized (monitor) {
      while (count > 0) {
        monitor.wait();
      }
    }
  }

  public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
    if (Thread.interrupted()) {
      throw new InterruptedException();
    }
    long nanos = unit.toNanos(timeout);
    if (nanos <= 0) {
      synchronized (monitor) {
        return count == 0;
      }
    }
    long deadline = System.nanoTime() + nanos;
    synchronized (monitor) {
      while (count > 0) {
        long remaining = deadline - System.nanoTime();
        if (remaining <= 0) {
          return false;
        }
        long millis = remaining / 1000000L;
        int remNanos = (int) (remaining % 1000000L);
        monitor.wait(millis, remNanos);
      }
      return true;
    }
  }

  public void countDown() {
    synchronized (monitor) {
      if (count > 0) {
        count--;
        if (count == 0) {
          monitor.notifyAll();
        }
      }
    }
  }

  public long getCount() {
    synchronized (monitor) {
      return count;
    }
  }

  @Override
  public String toString() {
    return super.toString() + "[Count = " + getCount() + "]";
  }
}
