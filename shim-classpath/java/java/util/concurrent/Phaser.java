/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.util.concurrent;

import java.util.Objects;

public class Phaser {
  private final Phaser parent;
  private final Phaser root;
  private final Object lock = new Object();

  private int phase = 0;
  private int parties = 0;
  private int unarrived = 0;
  private boolean terminated = false;

  public Phaser() {
    this(null, 0);
  }

  public Phaser(int parties) {
    this(null, parties);
  }

  public Phaser(Phaser parent) {
    this(parent, 0);
  }

  public Phaser(Phaser parent, int parties) {
    if (parties < 0 || parties > 65535) {
      throw new IllegalArgumentException("parties out of range: " + parties);
    }
    this.parent = parent;
    this.root = parent != null ? parent.root : this;
    this.parties = parties;
    this.unarrived = parties;
  }

  public int register() {
    return bulkRegister(1);
  }

  public int bulkRegister(int parties) {
    if (parties < 0) {
      throw new IllegalArgumentException("parties cannot be negative: " + parties);
    }
    if (parties == 0) {
      return getPhase();
    }
    synchronized (lock) {
      if (terminated) {
        return getPhase();
      }
      this.parties += parties;
      this.unarrived += parties;
      return phase;
    }
  }

  public int arrive() {
    synchronized (lock) {
      return doArrive(false);
    }
  }

  public int arriveAndDeregister() {
    synchronized (lock) {
      return doArrive(true);
    }
  }

  public int arriveAndAwaitAdvance() {
    synchronized (lock) {
      int p = doArrive(false);
      while (!terminated && phase == p) {
        try {
          lock.wait();
        } catch (InterruptedException e) {
          // arriveAndAwaitAdvance is uninterruptible
          Thread.currentThread().interrupt();
        }
      }
      return getPhase();
    }
  }

  public int awaitAdvance(int phase) {
    synchronized (lock) {
      if (this.phase != phase || terminated) {
        return getPhase();
      }
      while (this.phase == phase && !terminated) {
        try {
          lock.wait();
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      }
      return getPhase();
    }
  }

  public int awaitAdvanceInterruptibly(int phase) throws InterruptedException {
    synchronized (lock) {
      if (this.phase != phase || terminated) {
        return getPhase();
      }
      while (this.phase == phase && !terminated) {
        lock.wait();
      }
      return getPhase();
    }
  }

  public int awaitAdvanceInterruptibly(int phase, long timeout, TimeUnit unit)
      throws InterruptedException, TimeoutException {
    Objects.requireNonNull(unit, "unit");
    long nanos = unit.toNanos(timeout);
    long deadline = System.nanoTime() + nanos;

    synchronized (lock) {
      if (this.phase != phase || terminated) {
        return getPhase();
      }
      while (this.phase == phase && !terminated) {
        long remaining = deadline - System.nanoTime();
        if (remaining <= 0L) {
          throw new TimeoutException();
        }
        long millis = remaining / 1_000_000L;
        int remNanos = (int) (remaining % 1_000_000L);
        lock.wait(millis, remNanos);
      }
      return getPhase();
    }
  }

  private int doArrive(boolean deregister) {
    if (terminated) {
      return getPhase();
    }
    if (unarrived <= 0) {
      throw new IllegalStateException("unregistered arrive");
    }

    int currentPhase = phase;
    unarrived--;
    if (deregister) {
      parties--;
    }

    if (unarrived == 0) {
      boolean term = onAdvance(currentPhase, parties);
      if (term) {
        terminated = true;
      } else {
        phase++;
      }
      unarrived = parties;
      lock.notifyAll();
    }

    return currentPhase;
  }

  protected boolean onAdvance(int phase, int registeredParties) {
    return registeredParties == 0;
  }

  public void forceTermination() {
    synchronized (lock) {
      terminated = true;
      lock.notifyAll();
    }
  }

  public final int getPhase() {
    synchronized (lock) {
      return terminated ? (Integer.MIN_VALUE + phase) : phase;
    }
  }

  public int getRegisteredParties() {
    synchronized (lock) {
      return parties;
    }
  }

  public int getArrivedParties() {
    synchronized (lock) {
      return parties - unarrived;
    }
  }

  public int getUnarrivedParties() {
    synchronized (lock) {
      return unarrived;
    }
  }

  public boolean isTerminated() {
    synchronized (lock) {
      return terminated;
    }
  }

  public Phaser getParent() {
    return parent;
  }

  public Phaser getRoot() {
    return root;
  }
}
