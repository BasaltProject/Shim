/*
 * Copyright (c) 2026 BasaltMC.
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.foreign;

import java.util.Objects;

final class PaddingLayoutImpl extends AbstractMemoryLayout implements PaddingLayout {
  PaddingLayoutImpl(long byteSize, long byteAlignment, String name) {
    super(byteSize, byteAlignment, name);
  }

  @Override
  public PaddingLayout withName(String name) {
    Objects.requireNonNull(name, "name");
    return new PaddingLayoutImpl(byteSize, byteAlignment, name);
  }

  @Override
  public PaddingLayout withoutName() {
    return name == null ? this : new PaddingLayoutImpl(byteSize, byteAlignment, null);
  }

  @Override
  public PaddingLayout withByteAlignment(long alignment) {
    if (alignment <= 0 || Long.bitCount(alignment) != 1) {
      throw new IllegalArgumentException("Alignment must be a power of 2: " + alignment);
    }
    return new PaddingLayoutImpl(byteSize, alignment, name);
  }

  @Override
  public String toString() {
    String s = "x" + byteSize;
    if (byteAlignment != 1L) {
      s = byteAlignment + "%" + s;
    }
    return formatName(s, name);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!(obj instanceof PaddingLayout)) return false;
    PaddingLayout other = (PaddingLayout) obj;
    return byteSize == other.byteSize()
        && byteAlignment == other.byteAlignment()
        && name().equals(other.name());
  }

  @Override
  public int hashCode() {
    return Objects.hash(byteSize, byteAlignment, name);
  }
}
