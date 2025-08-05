/*
 * Copyright (c) 2026 BasaltMC.
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.foreign;

import java.util.Objects;

final class SequenceLayoutImpl extends AbstractMemoryLayout implements SequenceLayout {
  private final long elementCount;
  private final MemoryLayout elementLayout;

  SequenceLayoutImpl(long elementCount, MemoryLayout elementLayout, long byteAlignment, String name) {
    super(Math.multiplyExact(elementCount, elementLayout.byteSize()), byteAlignment, name);
    this.elementCount = elementCount;
    this.elementLayout = Objects.requireNonNull(elementLayout, "elementLayout");
  }

  @Override
  public long elementCount() {
    return elementCount;
  }

  @Override
  public MemoryLayout elementLayout() {
    return elementLayout;
  }

  @Override
  public SequenceLayout withElementCount(long count) {
    if (count < 0) {
      throw new IllegalArgumentException("Negative element count: " + count);
    }
    return new SequenceLayoutImpl(count, elementLayout, byteAlignment, name);
  }

  @Override
  public SequenceLayout withName(String name) {
    Objects.requireNonNull(name, "name");
    return new SequenceLayoutImpl(elementCount, elementLayout, byteAlignment, name);
  }

  @Override
  public SequenceLayout withoutName() {
    return name == null ? this : new SequenceLayoutImpl(elementCount, elementLayout, byteAlignment, null);
  }

  @Override
  public SequenceLayout withByteAlignment(long alignment) {
    if (alignment <= 0 || Long.bitCount(alignment) != 1) {
      throw new IllegalArgumentException("Alignment must be a power of 2: " + alignment);
    }
    return new SequenceLayoutImpl(elementCount, elementLayout, alignment, name);
  }

  @Override
  public SequenceLayout flatten() {
    if (elementLayout instanceof SequenceLayout) {
      SequenceLayout flatElem = ((SequenceLayout) elementLayout).flatten();
      long totalCount = Math.multiplyExact(elementCount, flatElem.elementCount());
      return MemoryLayout.sequenceLayout(totalCount, flatElem.elementLayout());
    }
    return this;
  }

  @Override
  public SequenceLayout reshape(long... elementCounts) {
    Objects.requireNonNull(elementCounts, "elementCounts");
    if (elementCounts.length == 0) {
      throw new IllegalArgumentException("Dimensions must not be empty");
    }
    long product = 1L;
    for (long d : elementCounts) {
      if (d < 0) {
        throw new IllegalArgumentException("Negative dimension: " + d);
      }
      product = Math.multiplyExact(product, d);
    }
    SequenceLayout flattened = flatten();
    if (product != flattened.elementCount()) {
      throw new IllegalArgumentException("Product of dimensions " + product + " != flattened count " + flattened.elementCount());
    }
    MemoryLayout current = flattened.elementLayout();
    for (int i = elementCounts.length - 1; i >= 0; i--) {
      current = MemoryLayout.sequenceLayout(elementCounts[i], current);
    }
    return (SequenceLayout) current;
  }

  @Override
  public String toString() {
    String s = "[" + elementCount + ":" + elementLayout + "]";
    return formatName(s, name);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!(obj instanceof SequenceLayout)) return false;
    SequenceLayout other = (SequenceLayout) obj;
    return elementCount == other.elementCount()
        && elementLayout.equals(other.elementLayout())
        && byteAlignment == other.byteAlignment()
        && name().equals(other.name());
  }

  @Override
  public int hashCode() {
    return Objects.hash(elementCount, elementLayout, byteAlignment, name);
  }
}
