/*
 * Copyright (c) 2026 BasaltMC.
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.foreign;

import java.nio.ByteOrder;
import java.util.Objects;
import java.util.Optional;

final class AddressLayoutImpl extends ValueLayoutImpl implements AddressLayout {
  static final AddressLayoutImpl DEFAULT = new AddressLayoutImpl(8L, 8L, null, ByteOrder.nativeOrder(), null);
  static final AddressLayoutImpl UNALIGNED = new AddressLayoutImpl(8L, 1L, null, ByteOrder.nativeOrder(), null);

  private final MemoryLayout targetLayout;

  AddressLayoutImpl(long byteSize, long byteAlignment, String name, ByteOrder order, MemoryLayout targetLayout) {
    super(byteSize, byteAlignment, name, MemorySegment.class, order, 'a');
    this.targetLayout = targetLayout;
  }

  @Override
  public AddressLayout withName(String name) {
    Objects.requireNonNull(name, "name");
    return new AddressLayoutImpl(byteSize, byteAlignment, name, order, targetLayout);
  }

  @Override
  public AddressLayout withoutName() {
    return name == null ? this : new AddressLayoutImpl(byteSize, byteAlignment, null, order, targetLayout);
  }

  @Override
  public AddressLayout withByteAlignment(long alignment) {
    if (alignment <= 0 || Long.bitCount(alignment) != 1) {
      throw new IllegalArgumentException("Alignment must be a power of 2: " + alignment);
    }
    return new AddressLayoutImpl(byteSize, alignment, name, order, targetLayout);
  }

  @Override
  public AddressLayout withOrder(ByteOrder newOrder) {
    Objects.requireNonNull(newOrder, "newOrder");
    return newOrder == order ? this : new AddressLayoutImpl(byteSize, byteAlignment, name, newOrder, targetLayout);
  }

  @Override
  public AddressLayout withTargetLayout(MemoryLayout layout) {
    Objects.requireNonNull(layout, "layout");
    return new AddressLayoutImpl(byteSize, byteAlignment, name, order, layout);
  }

  @Override
  public AddressLayout withoutTargetLayout() {
    return targetLayout == null ? this : new AddressLayoutImpl(byteSize, byteAlignment, name, order, null);
  }

  @Override
  public Optional<MemoryLayout> targetLayout() {
    return Optional.ofNullable(targetLayout);
  }

  @Override
  public boolean equals(Object obj) {
    if (!super.equals(obj)) return false;
    if (!(obj instanceof AddressLayout)) return false;
    AddressLayout other = (AddressLayout) obj;
    return targetLayout().equals(other.targetLayout());
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), targetLayout);
  }
}
