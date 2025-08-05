/*
 * Copyright (c) 2026 BasaltMC.
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.foreign;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import sun.misc.Unsafe;

final class ConfinedArena implements Arena {
  private final Thread ownerThread;
  private final ScopeImpl scope;
  private final List<Long> allocations = new ArrayList<Long>();
  private final List<Consumer<MemorySegment>> cleanups = new ArrayList<Consumer<MemorySegment>>();
  private final List<MemorySegment> cleanupSegments = new ArrayList<MemorySegment>();

  ConfinedArena(Thread ownerThread) {
    this.ownerThread = ownerThread;
    this.scope = new ScopeImpl(ownerThread);
  }

  private void checkAccess() {
    if (!scope.isAlive()) {
      throw new IllegalStateException("Arena is already closed");
    }
    if (Thread.currentThread() != ownerThread) {
      throw new WrongThreadException("Arena is confined to thread " + ownerThread);
    }
  }

  @Override
  public MemorySegment allocate(long byteSize, long byteAlignment) {
    checkAccess();
    if (byteSize < 0) {
      throw new IllegalArgumentException("Negative byteSize: " + byteSize);
    }
    if (Long.bitCount(byteAlignment) != 1) {
      throw new IllegalArgumentException("Alignment must be a power of 2: " + byteAlignment);
    }
    if (byteSize == 0) {
      return new NativeMemorySegment(0L, 0L, scope, false, 1L);
    }
    long pad = byteAlignment - 1L;
    long raw = Unsafe.getUnsafe().allocateMemory(byteSize + pad);
    long aligned = (raw + pad) & ~pad;
    Unsafe.getUnsafe().setMemory(null, aligned, byteSize, (byte) 0);
    allocations.add(raw);
    return new NativeMemorySegment(aligned, byteSize, scope, false, byteAlignment);
  }

  void registerCleanup(Consumer<MemorySegment> cleanup, MemorySegment segment) {
    checkAccess();
    cleanups.add(cleanup);
    cleanupSegments.add(segment);
  }

  @Override
  public MemorySegment.Scope scope() {
    return scope;
  }

  @Override
  public void close() {
    if (Thread.currentThread() != ownerThread) {
      throw new WrongThreadException("Arena is confined to thread " + ownerThread);
    }
    if (!scope.alive) {
      throw new IllegalStateException("Arena is already closed");
    }
    scope.alive = false;
    for (int i = 0; i < cleanups.size(); i++) {
      try {
        cleanups.get(i).accept(cleanupSegments.get(i));
      } catch (Throwable ignored) {
      }
    }
    cleanups.clear();
    cleanupSegments.clear();
    for (Long raw : allocations) {
      Unsafe.getUnsafe().freeMemory(raw);
    }
    allocations.clear();
  }
}
