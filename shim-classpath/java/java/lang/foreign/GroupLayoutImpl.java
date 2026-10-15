/*
 * Copyright (c) 2026 BasaltMC.
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.foreign;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

abstract class GroupLayoutImpl extends AbstractMemoryLayout implements GroupLayout {
  final List<MemoryLayout> memberLayouts;

  GroupLayoutImpl(long byteSize, long byteAlignment, String name, List<MemoryLayout> memberLayouts) {
    super(byteSize, byteAlignment, name);
    this.memberLayouts = Collections.unmodifiableList(Objects.requireNonNull(memberLayouts, "memberLayouts"));
  }

  @Override
  public List<MemoryLayout> memberLayouts() {
    return memberLayouts;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null || getClass() != obj.getClass()) return false;
    GroupLayoutImpl other = (GroupLayoutImpl) obj;
    return byteSize == other.byteSize
        && byteAlignment == other.byteAlignment
        && memberLayouts.equals(other.memberLayouts)
        && name().equals(other.name());
  }

  @Override
  public int hashCode() {
    return Objects.hash(byteSize, byteAlignment, memberLayouts, name);
  }
}
