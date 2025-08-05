/*
 * Copyright (c) 2026 BasaltMC.
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.foreign;

import sun.misc.Unsafe;

final class AutoArena implements Arena {
  private final ScopeImpl scope = new ScopeImpl(null);

  AutoArena() {
  }

  @Override
  public MemorySegment allocate(long byteSize, long byteAlignment) {
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
    return new NativeMemorySegment(aligned, byteSize, scope, false, byteAlignment);
  }

  @Override
  public MemorySegment.Scope scope() {
    return scope;
  }

  @Override
  public void close() {
    throw new UnsupportedOperationException("Cannot close an automatic arena");
  }
}
