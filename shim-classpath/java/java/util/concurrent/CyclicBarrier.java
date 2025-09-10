/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.util.concurrent;

import java.util.Objects;

public class CyclicBarrier {
  private final int parties;
  private final Runnable barrierCommand;
  private final Object lock = new Object();
  private int count;
  private int generation = 0;
  private boolean broken = false;

  public CyclicBarrier(int parties) {
    this(parties, null);
  }

  public CyclicBarrier(int parties, Runnable barrierAction) {
    if (parties <= 0) {
      throw new IllegalArgumentException("parties must be positive: " + parties);
    }
    this.parties = parties;
    this.count = parties;
    this.barrierCommand = barrierAction;
  }

  public int getParties() {
    return parties;
  }

  public int await() throws InterruptedException, BrokenBarrierException {
    try {
      return dowait(false, 0L);
    } catch (TimeoutException toe) {
      throw new Error(toe);
    }
  }

  public int await(long timeout, TimeUnit unit)
      throws InterruptedException, BrokenBarrierException, TimeoutException {
    Objects.requireNonNull(unit, "unit");
    return dowait(true, unit.toNanos(timeout));
  }

  private int dowait(boolean timed, long nanos)
      throws InterruptedException, BrokenBarrierException, TimeoutException {
    synchronized (lock) {
      if (broken) {
        throw new BrokenBarrierException();
      }
      if (Thread.interrupted()) {
        breakBarrier();
        throw new InterruptedException();
      }

      int index = --count;
      if (index == 0) {
        boolean ranAction = false;
        try {
          if (barrierCommand != null) {
            barrierCommand.run();
          }
          ranAction = true;
          nextGeneration();
          return 0;
        } finally {
          if (!ranAction) {
            breakBarrier();
          }
        }
      }

      int g = generation;
      long deadline = timed ? System.nanoTime() + nanos : 0L;

      while (g == generation && !broken) {
        if (Thread.interrupted()) {
          if (g == generation && !broken) {
            breakBarrier();
            throw new InterruptedException();
          }
          Thread.currentThread().interrupt();
        }

        if (timed) {
          long remaining = deadline - System.nanoTime();
          if (remaining <= 0L) {
            breakBarrier();
            throw new TimeoutException();
          }
          long millis = remaining / 1_000_000L;
          int remNanos = (int) (remaining % 1_000_000L);
          lock.wait(millis, remNanos);
        } else {
          lock.wait();
        }
      }

      if (broken) {
        throw new BrokenBarrierException();
      }

      return index;
    }
  }

  public boolean isBroken() {
    synchronized (lock) {
      return broken;
    }
  }

  public void reset() {
    synchronized (lock) {
      breakBarrier();
      nextGeneration();
      broken = false;
    }
  }

  public int getNumberWaiting() {
    synchronized (lock) {
      return parties - count;
    }
  }

  private void nextGeneration() {
    lock.notifyAll();
    count = parties;
    generation++;
  }

  private void breakBarrier() {
    broken = true;
    count = parties;
    lock.notifyAll();
  }
}
