/*
 * Copyright (c) 2026 BasaltMC.
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.foreign;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;

public interface MemorySegment {
  MemorySegment NULL = NativeMemorySegment.NULL;

  interface Scope {
    boolean isAlive();

    @Override
    boolean equals(Object obj);

    @Override
    int hashCode();
  }

  long address();

  Optional<Object> heapBase();

  Spliterator<MemorySegment> spliterator(MemoryLayout layout);

  Stream<MemorySegment> elements(MemoryLayout layout);

  MemorySegment.Scope scope();

  boolean isAccessibleBy(Thread thread);

  long byteSize();

  long maxByteAlignment();

  MemorySegment asSlice(long offset, long newSize);

  MemorySegment asSlice(long offset, long newSize, long byteAlignment);

  MemorySegment asSlice(long offset, MemoryLayout layout);

  MemorySegment asSlice(long offset);

  MemorySegment reinterpret(long newSize);

  MemorySegment reinterpret(Arena arena, Consumer<MemorySegment> cleanup);

  MemorySegment reinterpret(long newSize, Arena arena, Consumer<MemorySegment> cleanup);

  boolean isReadOnly();

  MemorySegment asReadOnly();

  boolean isNative();

  boolean isMapped();

  Optional<MemorySegment> asOverlappingSlice(MemorySegment other);

  MemorySegment fill(byte value);

  MemorySegment copyFrom(MemorySegment src);

  long mismatch(MemorySegment other);

  boolean isLoaded();

  void load();

  void unload();

  void force();

  ByteBuffer asByteBuffer();

  byte[] toArray(ValueLayout.OfByte elementLayout);

  short[] toArray(ValueLayout.OfShort elementLayout);

  char[] toArray(ValueLayout.OfChar elementLayout);

  int[] toArray(ValueLayout.OfInt elementLayout);

  float[] toArray(ValueLayout.OfFloat elementLayout);

  long[] toArray(ValueLayout.OfLong elementLayout);

  double[] toArray(ValueLayout.OfDouble elementLayout);

  default String getString(long offset) {
    return getString(offset, StandardCharsets.UTF_8);
  }

  String getString(long offset, Charset charset);

  default void setString(long offset, String str) {
    setString(offset, str, StandardCharsets.UTF_8);
  }

  void setString(long offset, String str, Charset charset);

  byte get(ValueLayout.OfByte layout, long offset);

  void set(ValueLayout.OfByte layout, long offset, byte value);

  boolean get(ValueLayout.OfBoolean layout, long offset);

  void set(ValueLayout.OfBoolean layout, long offset, boolean value);

  char get(ValueLayout.OfChar layout, long offset);

  void set(ValueLayout.OfChar layout, long offset, char value);

  short get(ValueLayout.OfShort layout, long offset);

  void set(ValueLayout.OfShort layout, long offset, short value);

  int get(ValueLayout.OfInt layout, long offset);

  void set(ValueLayout.OfInt layout, long offset, int value);

  float get(ValueLayout.OfFloat layout, long offset);

  void set(ValueLayout.OfFloat layout, long offset, float value);

  long get(ValueLayout.OfLong layout, long offset);

  void set(ValueLayout.OfLong layout, long offset, long value);

  double get(ValueLayout.OfDouble layout, long offset);

  void set(ValueLayout.OfDouble layout, long offset, double value);

  MemorySegment get(AddressLayout layout, long offset);

  void set(AddressLayout layout, long offset, MemorySegment value);

  default byte getAtIndex(ValueLayout.OfByte layout, long index) {
    return get(layout, Math.multiplyExact(index, layout.byteSize()));
  }

  default boolean getAtIndex(ValueLayout.OfBoolean layout, long index) {
    return get(layout, Math.multiplyExact(index, layout.byteSize()));
  }

  default char getAtIndex(ValueLayout.OfChar layout, long index) {
    return get(layout, Math.multiplyExact(index, layout.byteSize()));
  }

  default void setAtIndex(ValueLayout.OfChar layout, long index, char value) {
    set(layout, Math.multiplyExact(index, layout.byteSize()), value);
  }

  default short getAtIndex(ValueLayout.OfShort layout, long index) {
    return get(layout, Math.multiplyExact(index, layout.byteSize()));
  }

  default void setAtIndex(ValueLayout.OfByte layout, long index, byte value) {
    set(layout, Math.multiplyExact(index, layout.byteSize()), value);
  }

  default void setAtIndex(ValueLayout.OfBoolean layout, long index, boolean value) {
    set(layout, Math.multiplyExact(index, layout.byteSize()), value);
  }

  default void setAtIndex(ValueLayout.OfShort layout, long index, short value) {
    set(layout, Math.multiplyExact(index, layout.byteSize()), value);
  }

  default int getAtIndex(ValueLayout.OfInt layout, long index) {
    return get(layout, Math.multiplyExact(index, layout.byteSize()));
  }

  default void setAtIndex(ValueLayout.OfInt layout, long index, int value) {
    set(layout, Math.multiplyExact(index, layout.byteSize()), value);
  }

  default float getAtIndex(ValueLayout.OfFloat layout, long index) {
    return get(layout, Math.multiplyExact(index, layout.byteSize()));
  }

  default void setAtIndex(ValueLayout.OfFloat layout, long index, float value) {
    set(layout, Math.multiplyExact(index, layout.byteSize()), value);
  }

  default long getAtIndex(ValueLayout.OfLong layout, long index) {
    return get(layout, Math.multiplyExact(index, layout.byteSize()));
  }

  default void setAtIndex(ValueLayout.OfLong layout, long index, long value) {
    set(layout, Math.multiplyExact(index, layout.byteSize()), value);
  }

  default double getAtIndex(ValueLayout.OfDouble layout, long index) {
    return get(layout, Math.multiplyExact(index, layout.byteSize()));
  }

  default void setAtIndex(ValueLayout.OfDouble layout, long index, double value) {
    set(layout, Math.multiplyExact(index, layout.byteSize()), value);
  }

  default MemorySegment getAtIndex(AddressLayout layout, long index) {
    return get(layout, Math.multiplyExact(index, layout.byteSize()));
  }

  default void setAtIndex(AddressLayout layout, long index, MemorySegment value) {
    set(layout, Math.multiplyExact(index, layout.byteSize()), value);
  }

  @Override
  boolean equals(Object obj);

  @Override
  int hashCode();

  static MemorySegment ofBuffer(Buffer buffer) {
    return AbstractMemorySegment.ofBuffer(buffer);
  }

  static MemorySegment ofArray(byte[] array) {
    Objects.requireNonNull(array, "array");
    return new HeapMemorySegment(array, 0L, array.length, 1L, GlobalArena.INSTANCE.scope(), false);
  }

  static MemorySegment ofArray(char[] array) {
    Objects.requireNonNull(array, "array");
    return new HeapMemorySegment(array, 0L, (long) array.length * 2L, 2L, GlobalArena.INSTANCE.scope(), false);
  }

  static MemorySegment ofArray(short[] array) {
    Objects.requireNonNull(array, "array");
    return new HeapMemorySegment(array, 0L, (long) array.length * 2L, 2L, GlobalArena.INSTANCE.scope(), false);
  }

  static MemorySegment ofArray(int[] array) {
    Objects.requireNonNull(array, "array");
    return new HeapMemorySegment(array, 0L, (long) array.length * 4L, 4L, GlobalArena.INSTANCE.scope(), false);
  }

  static MemorySegment ofArray(float[] array) {
    Objects.requireNonNull(array, "array");
    return new HeapMemorySegment(array, 0L, (long) array.length * 4L, 4L, GlobalArena.INSTANCE.scope(), false);
  }

  static MemorySegment ofArray(long[] array) {
    Objects.requireNonNull(array, "array");
    return new HeapMemorySegment(array, 0L, (long) array.length * 8L, 8L, GlobalArena.INSTANCE.scope(), false);
  }

  static MemorySegment ofArray(double[] array) {
    Objects.requireNonNull(array, "array");
    return new HeapMemorySegment(array, 0L, (long) array.length * 8L, 8L, GlobalArena.INSTANCE.scope(), false);
  }

  static MemorySegment ofAddress(long address) {
    long align = address == 0L ? 1L : (1L << Long.numberOfTrailingZeros(address));
    return new NativeMemorySegment(address, 0L, GlobalArena.INSTANCE.scope(), false, align);
  }

  static void copy(MemorySegment src, long srcOffset, MemorySegment dst, long dstOffset, long bytes) {
    AbstractMemorySegment.copyBytes(src, srcOffset, dst, dstOffset, bytes);
  }

  static void copy(MemorySegment src, ValueLayout srcLayout, long srcOffset,
                   MemorySegment dst, ValueLayout dstLayout, long dstOffset, long elementCount) {
    AbstractMemorySegment.copyElements(src, srcLayout, srcOffset, dst, dstLayout, dstOffset, elementCount);
  }

  static void copy(MemorySegment src, ValueLayout srcLayout, long srcOffset,
                   Object dstArray, int dstIndex, int elementCount) {
    AbstractMemorySegment.copyToArray(src, srcLayout, srcOffset, dstArray, dstIndex, elementCount);
  }

  static void copy(Object srcArray, int srcIndex,
                   MemorySegment dst, ValueLayout dstLayout, long dstOffset, int elementCount) {
    AbstractMemorySegment.copyFromArray(srcArray, srcIndex, dst, dstLayout, dstOffset, elementCount);
  }

  static long mismatch(MemorySegment src, long srcFromOffset, long srcToOffset,
                       MemorySegment dst, long dstFromOffset, long dstToOffset) {
    return AbstractMemorySegment.mismatchRange(src, srcFromOffset, srcToOffset, dst, dstFromOffset, dstToOffset);
  }
}
