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

final class HeapMemorySegment extends AbstractMemorySegment {
  final Object base;
  final long baseOffset;
  final long elementAlignment;
  final int arrBaseOffset;

  HeapMemorySegment(Object base, long baseOffset, long byteSize, long elementAlignment,
                    MemorySegment.Scope scope, boolean readOnly) {
    super(scope, byteSize, readOnly);
    this.base = Objects.requireNonNull(base, "base");
    this.baseOffset = baseOffset;
    this.elementAlignment = elementAlignment;
    this.arrBaseOffset = UNSAFE.arrayBaseOffset(base.getClass());
  }

  @Override
  public long address() {
    return 0L;
  }

  @Override
  public boolean isNative() {
    return false;
  }

  @Override
  public Optional<Object> heapBase() {
    return Optional.of(base);
  }

  @Override
  public long maxByteAlignment() {
    return elementAlignment;
  }

  @Override
  void checkAlignment(long offset, long alignment) {
    if (Long.bitCount(alignment) != 1) {
      throw new IllegalArgumentException("Alignment must be a power of 2: " + alignment);
    }
    if (alignment > elementAlignment) {
      throw new IllegalArgumentException("Target offset " + offset + " is incompatible with alignment constraint " + alignment);
    }
    long eff = baseOffset + offset;
    if ((eff & (alignment - 1L)) != 0) {
      throw new IllegalArgumentException("Target offset " + offset + " is incompatible with alignment constraint " + alignment);
    }
  }

  @Override
  long arrayBaseOffset() {
    return arrBaseOffset;
  }

  @Override
  long baseOffset() {
    return baseOffset;
  }

  @Override
  public MemorySegment asSlice(long offset, long newSize) {
    checkBounds(offset, newSize);
    return new HeapMemorySegment(base, baseOffset + offset, newSize, elementAlignment, scope, readOnly);
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
    HeapMemorySegment seg = new HeapMemorySegment(base, baseOffset, newSize, elementAlignment, targetScope, readOnly);
    if (cleanup != null && arena instanceof ConfinedArena) {
      ((ConfinedArena) arena).registerCleanup(cleanup, seg);
    } else if (cleanup != null && arena instanceof SharedArena) {
      ((SharedArena) arena).registerCleanup(cleanup, seg);
    }
    return seg;
  }

  @Override
  public MemorySegment asReadOnly() {
    return readOnly ? this : new HeapMemorySegment(base, baseOffset, byteSize, elementAlignment, scope, true);
  }

  @Override
  public Optional<MemorySegment> asOverlappingSlice(MemorySegment other) {
    Objects.requireNonNull(other, "other");
    if (!(other instanceof HeapMemorySegment)) {
      return Optional.empty();
    }
    HeapMemorySegment hOther = (HeapMemorySegment) other;
    if (base != hOther.base) {
      return Optional.empty();
    }
    long start = Math.max(baseOffset, hOther.baseOffset);
    long end = Math.min(baseOffset + byteSize, hOther.baseOffset + hOther.byteSize);
    if (start < end) {
      return Optional.of(asSlice(start - baseOffset, end - start));
    }
    return Optional.empty();
  }

  @Override
  public ByteBuffer asByteBuffer() {
    checkAccess();
    if (byteSize > Integer.MAX_VALUE) {
      throw new IllegalStateException("Segment is too large to wrap as ByteBuffer. Size: " + byteSize);
    }
    if (base instanceof byte[]) {
      ByteBuffer bb = ByteBuffer.wrap((byte[]) base, (int) baseOffset, (int) byteSize);
      return readOnly ? bb.asReadOnlyBuffer() : bb;
    }
    throw new UnsupportedOperationException("Heap segment is not backed by byte[]");
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
    return UNSAFE.getByteVolatile(base, arrBaseOffset + baseOffset + offset);
  }

  @Override
  public void set(ValueLayout.OfByte layout, long offset, byte value) {
    Objects.requireNonNull(layout, "layout");
    checkWrite();
    checkBounds(offset, 1L);
    checkAlignment(offset, layout.byteAlignment());
    UNSAFE.putByteVolatile(base, arrBaseOffset + baseOffset + offset, value);
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
    char val = UNSAFE.getCharVolatile(base, arrBaseOffset + baseOffset + offset);
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
    UNSAFE.putCharVolatile(base, arrBaseOffset + baseOffset + offset, val);
  }

  @Override
  public short get(ValueLayout.OfShort layout, long offset) {
    Objects.requireNonNull(layout, "layout");
    checkAccess();
    checkBounds(offset, 2L);
    checkAlignment(offset, layout.byteAlignment());
    short val = UNSAFE.getShortVolatile(base, arrBaseOffset + baseOffset + offset);
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
    UNSAFE.putShortVolatile(base, arrBaseOffset + baseOffset + offset, val);
  }

  @Override
  public int get(ValueLayout.OfInt layout, long offset) {
    Objects.requireNonNull(layout, "layout");
    checkAccess();
    checkBounds(offset, 4L);
    checkAlignment(offset, layout.byteAlignment());
    int val = UNSAFE.getIntVolatile(base, arrBaseOffset + baseOffset + offset);
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
    UNSAFE.putIntVolatile(base, arrBaseOffset + baseOffset + offset, val);
  }

  @Override
  public float get(ValueLayout.OfFloat layout, long offset) {
    Objects.requireNonNull(layout, "layout");
    int bits = get(ValueLayout.JAVA_INT.withOrder(layout.order()), offset);
    return Float.intBitsToFloat(bits);
  }

  @Override
  public void set(ValueLayout.OfFloat layout, long offset, float value) {
    Objects.requireNonNull(layout, "layout");
    int bits = Float.floatToRawIntBits(value);
    set(ValueLayout.JAVA_INT.withOrder(layout.order()), offset, bits);
  }

  @Override
  public long get(ValueLayout.OfLong layout, long offset) {
    Objects.requireNonNull(layout, "layout");
    checkAccess();
    checkBounds(offset, 8L);
    checkAlignment(offset, layout.byteAlignment());
    long val = UNSAFE.getLongVolatile(base, arrBaseOffset + baseOffset + offset);
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
    UNSAFE.putLongVolatile(base, arrBaseOffset + baseOffset + offset, val);
  }

  @Override
  public double get(ValueLayout.OfDouble layout, long offset) {
    Objects.requireNonNull(layout, "layout");
    long bits = get(ValueLayout.JAVA_LONG.withOrder(layout.order()), offset);
    return Double.longBitsToDouble(bits);
  }

  @Override
  public void set(ValueLayout.OfDouble layout, long offset, double value) {
    Objects.requireNonNull(layout, "layout");
    long bits = Double.doubleToRawLongBits(value);
    set(ValueLayout.JAVA_LONG.withOrder(layout.order()), offset, bits);
  }

  @Override
  public MemorySegment get(AddressLayout layout, long offset) {
    Objects.requireNonNull(layout, "layout");
    long addr = get(ValueLayout.JAVA_LONG.withOrder(layout.order()), offset);
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
    set(ValueLayout.JAVA_LONG.withOrder(layout.order()), offset, value.address());
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!(obj instanceof MemorySegment)) return false;
    MemorySegment other = (MemorySegment) obj;
    if (other.isNative()) return false;
    return other.heapBase().isPresent() && base == other.heapBase().get()
        && baseOffset == ((HeapMemorySegment) other).baseOffset
        && byteSize == other.byteSize()
        && scope.equals(other.scope());
  }

  @Override
  public int hashCode() {
    return Objects.hash(System.identityHashCode(base), baseOffset, byteSize, scope);
  }

  @Override
  public String toString() {
    return "MemorySegment{ kind: heap, heapBase: " + base + ", address: 0x0, byteSize: " + byteSize + " }";
  }
}
