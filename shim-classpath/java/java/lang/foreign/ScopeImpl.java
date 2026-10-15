/*
 * Copyright (c) 2026 BasaltMC.
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.foreign;

final class ScopeImpl implements MemorySegment.Scope {
  volatile boolean alive = true;
  final Thread ownerThread;

  ScopeImpl(Thread ownerThread) {
    this.ownerThread = ownerThread;
  }

  @Override
  public boolean isAlive() {
    return alive;
  }

  boolean isAccessibleBy(Thread thread) {
    return ownerThread == null || ownerThread == thread;
  }
}
