/*
 * Copyright (c) 2026 BasaltMC.
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.foreign;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@FunctionalInterface
public interface SegmentAllocator {
  MemorySegment allocate(long byteSize, long byteAlignment);

  default MemorySegment allocate(MemoryLayout layout) {
    Objects.requireNonNull(layout, "layout");
    return allocate(layout.byteSize(), layout.byteAlignment());
  }

  default MemorySegment allocate(MemoryLayout layout, long count) {
    Objects.requireNonNull(layout, "layout");
    if (count < 0) {
      throw new IllegalArgumentException("Negative count: " + count);
    }
    return allocate(Math.multiplyExact(layout.byteSize(), count), layout.byteAlignment());
  }

  default MemorySegment allocate(long byteSize) {
    return allocate(byteSize, 1L);
  }

  default MemorySegment allocateFrom(String str) {
    return allocateFrom(str, StandardCharsets.UTF_8);
  }

  default MemorySegment allocateFrom(String str, Charset charset) {
    Objects.requireNonNull(str, "str");
    Objects.requireNonNull(charset, "charset");
    byte[] bytes = str.getBytes(charset);
    MemorySegment segment = allocate(bytes.length + 1L, 1L);
    MemorySegment.copy(bytes, 0, segment, ValueLayout.JAVA_BYTE, 0L, bytes.length);
    segment.set(ValueLayout.JAVA_BYTE, bytes.length, (byte) 0);
    return segment;
  }

  default MemorySegment allocateFrom(ValueLayout.OfByte layout, byte value) {
    Objects.requireNonNull(layout, "layout");
    MemorySegment seg = allocate(layout);
    seg.set(layout, 0L, value);
    return seg;
  }

  default MemorySegment allocateFrom(ValueLayout.OfChar layout, char value) {
    Objects.requireNonNull(layout, "layout");
    MemorySegment seg = allocate(layout);
    seg.set(layout, 0L, value);
    return seg;
  }

  default MemorySegment allocateFrom(ValueLayout.OfShort layout, short value) {
    Objects.requireNonNull(layout, "layout");
    MemorySegment seg = allocate(layout);
    seg.set(layout, 0L, value);
    return seg;
  }

  default MemorySegment allocateFrom(ValueLayout.OfInt layout, int value) {
    Objects.requireNonNull(layout, "layout");
    MemorySegment seg = allocate(layout);
    seg.set(layout, 0L, value);
    return seg;
  }

  default MemorySegment allocateFrom(ValueLayout.OfFloat layout, float value) {
    Objects.requireNonNull(layout, "layout");
    MemorySegment seg = allocate(layout);
    seg.set(layout, 0L, value);
    return seg;
  }

  default MemorySegment allocateFrom(ValueLayout.OfLong layout, long value) {
    Objects.requireNonNull(layout, "layout");
    MemorySegment seg = allocate(layout);
    seg.set(layout, 0L, value);
    return seg;
  }

  default MemorySegment allocateFrom(ValueLayout.OfDouble layout, double value) {
    Objects.requireNonNull(layout, "layout");
    MemorySegment seg = allocate(layout);
    seg.set(layout, 0L, value);
    return seg;
  }

  default MemorySegment allocateFrom(AddressLayout layout, MemorySegment value) {
    Objects.requireNonNull(layout, "layout");
    Objects.requireNonNull(value, "value");
    MemorySegment seg = allocate(layout);
    seg.set(layout, 0L, value);
    return seg;
  }

  default MemorySegment allocateFrom(ValueLayout layout, MemorySegment source,
                                      ValueLayout sourceElementLayout, long sourceOffset, long elementCount) {
    Objects.requireNonNull(layout, "layout");
    Objects.requireNonNull(source, "source");
    Objects.requireNonNull(sourceElementLayout, "sourceElementLayout");
    if (elementCount < 0) {
      throw new IllegalArgumentException("Negative elementCount: " + elementCount);
    }
    long size = Math.multiplyExact(layout.byteSize(), elementCount);
    MemorySegment seg = allocate(size, layout.byteAlignment());
    MemorySegment.copy(source, sourceElementLayout, sourceOffset, seg, layout, 0L, elementCount);
    return seg;
  }

  default MemorySegment allocateFrom(ValueLayout.OfByte layout, byte... elements) {
    Objects.requireNonNull(layout, "layout");
    Objects.requireNonNull(elements, "elements");
    MemorySegment seg = allocate(layout, elements.length);
    for (int i = 0; i < elements.length; i++) {
      seg.setAtIndex(layout, i, elements[i]);
    }
    return seg;
  }

  default MemorySegment allocateFrom(ValueLayout.OfShort layout, short... elements) {
    Objects.requireNonNull(layout, "layout");
    Objects.requireNonNull(elements, "elements");
    MemorySegment seg = allocate(layout, elements.length);
    for (int i = 0; i < elements.length; i++) {
      seg.setAtIndex(layout, i, elements[i]);
    }
    return seg;
  }

  default MemorySegment allocateFrom(ValueLayout.OfChar layout, char... elements) {
    Objects.requireNonNull(layout, "layout");
    Objects.requireNonNull(elements, "elements");
    MemorySegment seg = allocate(layout, elements.length);
    for (int i = 0; i < elements.length; i++) {
      seg.setAtIndex(layout, i, elements[i]);
    }
    return seg;
  }

  default MemorySegment allocateFrom(ValueLayout.OfInt layout, int... elements) {
    Objects.requireNonNull(layout, "layout");
    Objects.requireNonNull(elements, "elements");
    MemorySegment seg = allocate(layout, elements.length);
    for (int i = 0; i < elements.length; i++) {
      seg.setAtIndex(layout, i, elements[i]);
    }
    return seg;
  }

  default MemorySegment allocateFrom(ValueLayout.OfFloat layout, float... elements) {
    Objects.requireNonNull(layout, "layout");
    Objects.requireNonNull(elements, "elements");
    MemorySegment seg = allocate(layout, elements.length);
    for (int i = 0; i < elements.length; i++) {
      seg.setAtIndex(layout, i, elements[i]);
    }
    return seg;
  }

  default MemorySegment allocateFrom(ValueLayout.OfLong layout, long... elements) {
    Objects.requireNonNull(layout, "layout");
    Objects.requireNonNull(elements, "elements");
    MemorySegment seg = allocate(layout, elements.length);
    for (int i = 0; i < elements.length; i++) {
      seg.setAtIndex(layout, i, elements[i]);
    }
    return seg;
  }

  default MemorySegment allocateFrom(ValueLayout.OfDouble layout, double... elements) {
    Objects.requireNonNull(layout, "layout");
    Objects.requireNonNull(elements, "elements");
    MemorySegment seg = allocate(layout, elements.length);
    for (int i = 0; i < elements.length; i++) {
      seg.setAtIndex(layout, i, elements[i]);
    }
    return seg;
  }

  static SegmentAllocator slicingAllocator(MemorySegment segment) {
    Objects.requireNonNull(segment, "segment");
    return new SegmentAllocator() {
      private long offset = 0L;

      @Override
      public MemorySegment allocate(long byteSize, long byteAlignment) {
        if (byteSize < 0) {
          throw new IllegalArgumentException("Negative byteSize: " + byteSize);
        }
        if (Long.bitCount(byteAlignment) != 1) {
          throw new IllegalArgumentException("Alignment must be a power of 2: " + byteAlignment);
        }
        long aligned = (offset + (byteAlignment - 1L)) & ~(byteAlignment - 1L);
        if (aligned + byteSize > segment.byteSize() || aligned < 0) {
          throw new IndexOutOfBoundsException("Out of bound access on segment " + segment
              + "; new offset = " + aligned + "; new length = " + byteSize);
        }
        offset = aligned + byteSize;
        return segment.asSlice(aligned, byteSize);
      }
    };
  }

  static SegmentAllocator prefixAllocator(MemorySegment segment) {
    return slicingAllocator(segment);
  }
}
