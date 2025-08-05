/*
 * Copyright (c) 2026 BasaltMC.
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.foreign;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.util.Objects;
import java.util.Optional;

public interface MemoryLayout {
  interface PathElement {
    static PathElement groupElement(String name) {
      Objects.requireNonNull(name, "name");
      return new PathElements.GroupByName(name);
    }

    static PathElement groupElement(long index) {
      if (index < 0) {
        throw new IllegalArgumentException("Negative index: " + index);
      }
      return new PathElements.GroupByIndex(index);
    }

    static PathElement sequenceElement(long index) {
      if (index < 0) {
        throw new IllegalArgumentException("Negative index: " + index);
      }
      return new PathElements.SequenceByIndex(index);
    }

    static PathElement sequenceElement(long start, long step) {
      return new PathElements.SequenceByRange(start, step);
    }

    static PathElement sequenceElement() {
      return PathElements.OpenSequence.INSTANCE;
    }

    static PathElement dereferenceElement() {
      return PathElements.Dereference.INSTANCE;
    }
  }

  long byteSize();

  Optional<String> name();

  MemoryLayout withName(String name);

  MemoryLayout withoutName();

  long byteAlignment();

  MemoryLayout withByteAlignment(long byteAlignment);

  default long scale(long offset, long index) {
    return offset + Math.multiplyExact(index, byteSize());
  }

  MethodHandle scaleHandle();

  long byteOffset(PathElement... elements);

  MethodHandle byteOffsetHandle(PathElement... elements);

  VarHandle varHandle(PathElement... elements);

  VarHandle arrayElementVarHandle(PathElement... elements);

  MethodHandle sliceHandle(PathElement... elements);

  MemoryLayout select(PathElement... elements);

  @Override
  boolean equals(Object obj);

  @Override
  int hashCode();

  @Override
  String toString();

  static PaddingLayout paddingLayout(long byteSize) {
    if (byteSize <= 0) {
      throw new IllegalArgumentException("Padding size must be positive: " + byteSize);
    }
    return new PaddingLayoutImpl(byteSize, 1L, null);
  }

  static SequenceLayout sequenceLayout(long elementCount, MemoryLayout elementLayout) {
    if (elementCount < 0) {
      throw new IllegalArgumentException("Negative element count: " + elementCount);
    }
    Objects.requireNonNull(elementLayout, "elementLayout");
    return new SequenceLayoutImpl(elementCount, elementLayout, elementLayout.byteAlignment(), null);
  }

  static StructLayout structLayout(MemoryLayout... memberLayouts) {
    return StructLayoutImpl.of(memberLayouts);
  }

  static UnionLayout unionLayout(MemoryLayout... memberLayouts) {
    return UnionLayoutImpl.of(memberLayouts);
  }
}
