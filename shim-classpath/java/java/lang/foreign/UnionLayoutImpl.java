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

final class UnionLayoutImpl extends GroupLayoutImpl implements UnionLayout {
  UnionLayoutImpl(long byteSize, long byteAlignment, String name, List<MemoryLayout> memberLayouts) {
    super(byteSize, byteAlignment, name, memberLayouts);
  }

  static UnionLayout of(MemoryLayout... memberLayouts) {
    Objects.requireNonNull(memberLayouts, "memberLayouts");
    long maxSize = 0L;
    long maxAlign = 1L;
    List<MemoryLayout> list = new ArrayList<MemoryLayout>(memberLayouts.length);

    for (MemoryLayout m : memberLayouts) {
      Objects.requireNonNull(m, "memberLayout");
      maxSize = Math.max(maxSize, m.byteSize());
      maxAlign = Math.max(maxAlign, m.byteAlignment());
      list.add(m);
    }
    return new UnionLayoutImpl(maxSize, maxAlign, null, list);
  }

  @Override
  public UnionLayout withName(String name) {
    Objects.requireNonNull(name, "name");
    return new UnionLayoutImpl(byteSize, byteAlignment, name, memberLayouts);
  }

  @Override
  public UnionLayout withoutName() {
    return name == null ? this : new UnionLayoutImpl(byteSize, byteAlignment, null, memberLayouts);
  }

  @Override
  public UnionLayout withByteAlignment(long alignment) {
    if (alignment <= 0 || Long.bitCount(alignment) != 1) {
      throw new IllegalArgumentException("Alignment must be a power of 2: " + alignment);
    }
    return new UnionLayoutImpl(byteSize, alignment, name, memberLayouts);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("[");
    for (int i = 0; i < memberLayouts.size(); i++) {
      if (i > 0) {
        sb.append("|");
      }
      sb.append(memberLayouts.get(i));
    }
    sb.append("]");
    return formatName(sb.toString(), name);
  }
}
