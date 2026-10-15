/*
 * Copyright (c) 2026 BasaltMC.
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.foreign;

public interface Arena extends SegmentAllocator, AutoCloseable {
  static Arena ofAuto() {
    return new AutoArena();
  }

  static Arena global() {
    return GlobalArena.INSTANCE;
  }

  static Arena ofConfined() {
    return new ConfinedArena(Thread.currentThread());
  }

  static Arena ofShared() {
    return new SharedArena();
  }

  @Override
  MemorySegment allocate(long byteSize, long byteAlignment);

  MemorySegment.Scope scope();

  @Override
  void close();
}
