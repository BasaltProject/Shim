/*
 * Copyright (c) 2026 BasaltMC.
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.foreign;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

final class StructLayoutImpl extends GroupLayoutImpl implements StructLayout {
  StructLayoutImpl(long byteSize, long byteAlignment, String name, List<MemoryLayout> memberLayouts) {
    super(byteSize, byteAlignment, name, memberLayouts);
  }

  static StructLayout of(MemoryLayout... memberLayouts) {
    Objects.requireNonNull(memberLayouts, "memberLayouts");
    long totalSize = 0L;
    long maxAlign = 1L;
    List<MemoryLayout> list = new ArrayList<MemoryLayout>(memberLayouts.length);

    for (MemoryLayout m : memberLayouts) {
      Objects.requireNonNull(m, "memberLayout");
      if (totalSize % m.byteAlignment() != 0) {
        throw new IllegalArgumentException("Invalid alignment constraint for member layout: " + m);
      }
      totalSize = Math.addExact(totalSize, m.byteSize());
      maxAlign = Math.max(maxAlign, m.byteAlignment());
      list.add(m);
    }
    return new StructLayoutImpl(totalSize, maxAlign, null, list);
  }

  @Override
  public StructLayout withName(String name) {
    Objects.requireNonNull(name, "name");
    return new StructLayoutImpl(byteSize, byteAlignment, name, memberLayouts);
  }

  @Override
  public StructLayout withoutName() {
    return name == null ? this : new StructLayoutImpl(byteSize, byteAlignment, null, memberLayouts);
  }

  @Override
  public StructLayout withByteAlignment(long alignment) {
    if (alignment <= 0 || Long.bitCount(alignment) != 1) {
      throw new IllegalArgumentException("Alignment must be a power of 2: " + alignment);
    }
    return new StructLayoutImpl(byteSize, alignment, name, memberLayouts);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("[");
    for (MemoryLayout m : memberLayouts) {
      sb.append(m);
    }
    sb.append("]");
    return formatName(sb.toString(), name);
  }
}
