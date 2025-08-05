/*
 * Copyright (c) 2026 BasaltMC.
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.foreign;

final class PathHandleHelper {
  private PathHandleHelper() {
  }

  static final class Fixed {
    final long offset;
    final long size;

    Fixed(long offset, long size) {
      this.offset = offset;
      this.size = size;
    }

    public long computeOffset(long baseOffset) {
      return baseOffset + offset;
    }

    public MemorySegment slice(MemorySegment segment, long baseOffset) {
      return segment.asSlice(baseOffset + offset, size);
    }
  }

  static final class Indexed {
    final long offset;
    final long elementSize;

    Indexed(long offset, long elementSize) {
      this.offset = offset;
      this.elementSize = elementSize;
    }

    public long computeOffset(long baseOffset, long index) {
      return baseOffset + offset + Math.multiplyExact(index, elementSize);
    }

    public MemorySegment slice(MemorySegment segment, long baseOffset, long index) {
      return segment.asSlice(baseOffset + offset + Math.multiplyExact(index, elementSize), elementSize);
    }
  }
}
