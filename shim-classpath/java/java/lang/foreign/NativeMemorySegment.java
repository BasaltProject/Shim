/*
 * Copyright (c) 2026 BasaltMC.
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.foreign;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

final class NativeMemorySegment extends AbstractMemorySegment {
  static final NativeMemorySegment NULL = new NativeMemorySegment(0L, 0L, GlobalArena.INSTANCE.scope(), false, 1L);

  private final long address;
  private final long maxAlignment;

  NativeMemorySegment(long address, long byteSize, MemorySegment.Scope scope, boolean readOnly, long maxAlignment) {
    super(scope, byteSize, readOnly);
    this.address = address;
    this.maxAlignment = maxAlignment;
  }

  @Override
  public long address() {
    return address;
  }

  @Override
  public boolean isNative() {
    return true;
  }

  @Override
  public Optional<Object> heapBase() {
    return Optional.empty();
  }

  @Override
  public long maxByteAlignment() {
    return address == 0L ? 1L : (1L << Long.numberOfTrailingZeros(address));
  }

  @Override
  void checkAlignment(long offset, long alignment) {
    if (Long.bitCount(alignment) != 1) {
      throw new IllegalArgumentException("Alignment must be a power of 2: " + alignment);
    }
    long eff = address + offset;
    if ((eff & (alignment - 1L)) != 0) {
      throw new IllegalArgumentException("Target offset " + offset + " is incompatible with alignment constraint " + alignment);
    }
  }

  @Override
  long arrayBaseOffset() {
    return 0L;
  }

  @Override
  long baseOffset() {
    return 0L;
  }

  @Override
  public MemorySegment asSlice(long offset, long newSize) {
    checkBounds(offset, newSize);
    return new NativeMemorySegment(address + offset, newSize, scope, readOnly, maxAlignment);
  }

  @Override
  public MemorySegment reinterpret(long newSize) {
    return reinterpret(newSize, null, null);
  }

  @Override
  public MemorySegment reinterpret(Arena arena, Consumer<MemorySegment> cleanup) {
    return reinterpret(byteSize, arena, cleanup);
  }

  @Override
  public MemorySegment reinterpret(long newSize, Arena arena, Consumer<MemorySegment> cleanup) {
    if (newSize < 0) {
      throw new IllegalArgumentException("Negative newSize: " + newSize);
    }
    MemorySegment.Scope targetScope = arena != null ? arena.scope() : scope;
    NativeMemorySegment seg = new NativeMemorySegment(address, newSize, targetScope, readOnly, maxAlignment);
    if (cleanup != null && arena instanceof ConfinedArena) {
      ((ConfinedArena) arena).registerCleanup(cleanup, seg);
    } else if (cleanup != null && arena instanceof SharedArena) {
      ((SharedArena) arena).registerCleanup(cleanup, seg);
    }
    return seg;
  }

  @Override
  public MemorySegment asReadOnly() {
    return readOnly ? this : new NativeMemorySegment(address, byteSize, scope, true, maxAlignment);
  }

  @Override
  public Optional<MemorySegment> asOverlappingSlice(MemorySegment other) {
    Objects.requireNonNull(other, "other");
    if (!(other instanceof NativeMemorySegment)) {
      return Optional.empty();
    }
    NativeMemorySegment nOther = (NativeMemorySegment) other;
    long start = Math.max(address, nOther.address);
    long end = Math.min(address + byteSize, nOther.address + nOther.byteSize);
    if (start < end) {
      return Optional.of(asSlice(start - address, end - start));
    }
    return Optional.empty();
  }

  @Override
  public ByteBuffer asByteBuffer() {
    checkAccess();
    if (byteSize > Integer.MAX_VALUE) {
      throw new IllegalStateException("Segment is too large to wrap as ByteBuffer. Size: " + byteSize);
    }
    return new java.nio.DirectByteBuffer(address, (int) byteSize, readOnly);
  }

  @Override
  public byte[] toArray(ValueLayout.OfByte elementLayout) {
    Objects.requireNonNull(elementLayout, "elementLayout");
    checkAccess();
    int len = (int) (byteSize / elementLayout.byteSize());
    byte[] arr = new byte[len];
    MemorySegment.copy(this, elementLayout, 0L, arr, 0, len);
    return arr;
  }

  @Override
  public short[] toArray(ValueLayout.OfShort elementLayout) {
    Objects.requireNonNull(elementLayout, "elementLayout");
    checkAccess();
    int len = (int) (byteSize / elementLayout.byteSize());
    short[] arr = new short[len];
    MemorySegment.copy(this, elementLayout, 0L, arr, 0, len);
    return arr;
  }

  @Override
  public char[] toArray(ValueLayout.OfChar elementLayout) {
    Objects.requireNonNull(elementLayout, "elementLayout");
    checkAccess();
    int len = (int) (byteSize / elementLayout.byteSize());
    char[] arr = new char[len];
    MemorySegment.copy(this, elementLayout, 0L, arr, 0, len);
    return arr;
  }

  @Override
  public int[] toArray(ValueLayout.OfInt elementLayout) {
    Objects.requireNonNull(elementLayout, "elementLayout");
    checkAccess();
    int len = (int) (byteSize / elementLayout.byteSize());
    int[] arr = new int[len];
    MemorySegment.copy(this, elementLayout, 0L, arr, 0, len);
    return arr;
  }

  @Override
  public float[] toArray(ValueLayout.OfFloat elementLayout) {
    Objects.requireNonNull(elementLayout, "elementLayout");
    checkAccess();
    int len = (int) (byteSize / elementLayout.byteSize());
    float[] arr = new float[len];
    MemorySegment.copy(this, elementLayout, 0L, arr, 0, len);
    return arr;
  }

  @Override
  public long[] toArray(ValueLayout.OfLong elementLayout) {
    Objects.requireNonNull(elementLayout, "elementLayout");
    checkAccess();
    int len = (int) (byteSize / elementLayout.byteSize());
    long[] arr = new long[len];
    MemorySegment.copy(this, elementLayout, 0L, arr, 0, len);
    return arr;
  }

  @Override
  public double[] toArray(ValueLayout.OfDouble elementLayout) {
    Objects.requireNonNull(elementLayout, "elementLayout");
    checkAccess();
    int len = (int) (byteSize / elementLayout.byteSize());
    double[] arr = new double[len];
    MemorySegment.copy(this, elementLayout, 0L, arr, 0, len);
    return arr;
  }

  @Override
  public byte get(ValueLayout.OfByte layout, long offset) {
    Objects.requireNonNull(layout, "layout");
    checkAccess();
    checkBounds(offset, 1L);
    checkAlignment(offset, layout.byteAlignment());
    return UNSAFE.getByte(address + offset);
  }

  @Override
  public void set(ValueLayout.OfByte layout, long offset, byte value) {
    Objects.requireNonNull(layout, "layout");
    checkWrite();
    checkBounds(offset, 1L);
    checkAlignment(offset, layout.byteAlignment());
    UNSAFE.putByte(address + offset, value);
  }

  @Override
  public boolean get(ValueLayout.OfBoolean layout, long offset) {
    Objects.requireNonNull(layout, "layout");
    return get(ValueLayout.JAVA_BYTE, offset) != 0;
  }

  @Override
  public void set(ValueLayout.OfBoolean layout, long offset, boolean value) {
    Objects.requireNonNull(layout, "layout");
    set(ValueLayout.JAVA_BYTE, offset, (byte) (value ? 1 : 0));
  }

  @Override
  public char get(ValueLayout.OfChar layout, long offset) {
    Objects.requireNonNull(layout, "layout");
    checkAccess();
    checkBounds(offset, 2L);
    checkAlignment(offset, layout.byteAlignment());
    char val = UNSAFE.getChar(address + offset);
    if (layout.order() != ByteOrder.nativeOrder()) {
      val = Character.reverseBytes(val);
    }
    return val;
  }

  @Override
  public void set(ValueLayout.OfChar layout, long offset, char value) {
    Objects.requireNonNull(layout, "layout");
    checkWrite();
    checkBounds(offset, 2L);
    checkAlignment(offset, layout.byteAlignment());
    char val = (layout.order() != ByteOrder.nativeOrder()) ? Character.reverseBytes(value) : value;
    UNSAFE.putChar(address + offset, val);
  }

  @Override
  public short get(ValueLayout.OfShort layout, long offset) {
    Objects.requireNonNull(layout, "layout");
    checkAccess();
    checkBounds(offset, 2L);
    checkAlignment(offset, layout.byteAlignment());
    short val = UNSAFE.getShort(address + offset);
    if (layout.order() != ByteOrder.nativeOrder()) {
      val = Short.reverseBytes(val);
    }
    return val;
  }

  @Override
  public void set(ValueLayout.OfShort layout, long offset, short value) {
    Objects.requireNonNull(layout, "layout");
    checkWrite();
    checkBounds(offset, 2L);
    checkAlignment(offset, layout.byteAlignment());
    short val = (layout.order() != ByteOrder.nativeOrder()) ? Short.reverseBytes(value) : value;
    UNSAFE.putShort(address + offset, val);
  }

  @Override
  public int get(ValueLayout.OfInt layout, long offset) {
    Objects.requireNonNull(layout, "layout");
    checkAccess();
    checkBounds(offset, 4L);
    checkAlignment(offset, layout.byteAlignment());
    int val = UNSAFE.getInt(address + offset);
    if (layout.order() != ByteOrder.nativeOrder()) {
      val = Integer.reverseBytes(val);
    }
    return val;
  }

  @Override
  public void set(ValueLayout.OfInt layout, long offset, int value) {
    Objects.requireNonNull(layout, "layout");
    checkWrite();
    checkBounds(offset, 4L);
    checkAlignment(offset, layout.byteAlignment());
    int val = (layout.order() != ByteOrder.nativeOrder()) ? Integer.reverseBytes(value) : value;
    UNSAFE.putInt(address + offset, val);
  }

  @Override
  public float get(ValueLayout.OfFloat layout, long offset) {
    Objects.requireNonNull(layout, "layout");
    int bits = get(ValueLayout.JAVA_INT.withByteAlignment(layout.byteAlignment()).withOrder(layout.order()), offset);
    return Float.intBitsToFloat(bits);
  }

  @Override
  public void set(ValueLayout.OfFloat layout, long offset, float value) {
    Objects.requireNonNull(layout, "layout");
    int bits = Float.floatToRawIntBits(value);
    set(ValueLayout.JAVA_INT.withByteAlignment(layout.byteAlignment()).withOrder(layout.order()), offset, bits);
  }

  @Override
  public long get(ValueLayout.OfLong layout, long offset) {
    Objects.requireNonNull(layout, "layout");
    checkAccess();
    checkBounds(offset, 8L);
    checkAlignment(offset, layout.byteAlignment());
    long val = UNSAFE.getLong(address + offset);
    if (layout.order() != ByteOrder.nativeOrder()) {
      val = Long.reverseBytes(val);
    }
    return val;
  }

  @Override
  public void set(ValueLayout.OfLong layout, long offset, long value) {
    Objects.requireNonNull(layout, "layout");
    checkWrite();
    checkBounds(offset, 8L);
    checkAlignment(offset, layout.byteAlignment());
    long val = (layout.order() != ByteOrder.nativeOrder()) ? Long.reverseBytes(value) : value;
    UNSAFE.putLong(address + offset, val);
  }

  @Override
  public double get(ValueLayout.OfDouble layout, long offset) {
    Objects.requireNonNull(layout, "layout");
    long bits = get(ValueLayout.JAVA_LONG.withByteAlignment(layout.byteAlignment()).withOrder(layout.order()), offset);
    return Double.longBitsToDouble(bits);
  }

  @Override
  public void set(ValueLayout.OfDouble layout, long offset, double value) {
    Objects.requireNonNull(layout, "layout");
    long bits = Double.doubleToRawLongBits(value);
    set(ValueLayout.JAVA_LONG.withByteAlignment(layout.byteAlignment()).withOrder(layout.order()), offset, bits);
  }

  @Override
  public MemorySegment get(AddressLayout layout, long offset) {
    Objects.requireNonNull(layout, "layout");
    long addr = get(ValueLayout.JAVA_LONG.withByteAlignment(layout.byteAlignment()).withOrder(layout.order()), offset);
    MemorySegment seg = MemorySegment.ofAddress(addr);
    if (layout.targetLayout().isPresent()) {
      seg = seg.reinterpret(layout.targetLayout().get().byteSize());
    }
    return seg;
  }

  @Override
  public void set(AddressLayout layout, long offset, MemorySegment value) {
    Objects.requireNonNull(layout, "layout");
    Objects.requireNonNull(value, "value");
    set(ValueLayout.JAVA_LONG.withByteAlignment(layout.byteAlignment()).withOrder(layout.order()), offset, value.address());
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!(obj instanceof MemorySegment)) return false;
    MemorySegment other = (MemorySegment) obj;
    return other.isNative() && address == other.address() && byteSize == other.byteSize() && scope.equals(other.scope());
  }

  @Override
  public int hashCode() {
    return Objects.hash(address, byteSize, scope);
  }

  @Override
  public String toString() {
    return "MemorySegment{ kind: native, address: 0x" + Long.toHexString(address) + ", byteSize: " + byteSize + " }";
  }
}
