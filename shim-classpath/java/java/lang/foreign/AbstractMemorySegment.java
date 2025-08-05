/*
 * Copyright (c) 2026 BasaltMC.
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.foreign;

import java.io.ByteArrayOutputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import sun.misc.Unsafe;

abstract class AbstractMemorySegment implements MemorySegment {
  static final Unsafe UNSAFE = Unsafe.getUnsafe();

  final MemorySegment.Scope scope;
  final long byteSize;
  final boolean readOnly;

  AbstractMemorySegment(MemorySegment.Scope scope, long byteSize, boolean readOnly) {
    this.scope = Objects.requireNonNull(scope, "scope");
    this.byteSize = byteSize;
    this.readOnly = readOnly;
  }

  @Override
  public MemorySegment.Scope scope() {
    return scope;
  }

  @Override
  public long byteSize() {
    return byteSize;
  }

  @Override
  public boolean isReadOnly() {
    return readOnly;
  }

  @Override
  public boolean isAccessibleBy(Thread thread) {
    if (scope instanceof ScopeImpl) {
      return ((ScopeImpl) scope).isAccessibleBy(thread);
    }
    return true;
  }

  void checkAlive() {
    if (!scope.isAlive()) {
      throw new IllegalStateException("Segment scope is closed");
    }
  }

  void checkAccess() {
    checkAlive();
    if (!isAccessibleBy(Thread.currentThread())) {
      throw new WrongThreadException("Segment is not accessible by current thread");
    }
  }

  void checkWrite() {
    checkAccess();
    if (readOnly) {
      throw new UnsupportedOperationException("Segment is read-only");
    }
  }

  void checkBounds(long offset, long length) {
    if (offset < 0 || length < 0 || offset > byteSize - length) {
      throw new IndexOutOfBoundsException("Out of bound access: offset=" + offset + ", length=" + length + ", size=" + byteSize);
    }
  }

  abstract void checkAlignment(long offset, long alignment);

  @Override
  public MemorySegment asSlice(long offset) {
    return asSlice(offset, byteSize - offset);
  }

  @Override
  public MemorySegment asSlice(long offset, MemoryLayout layout) {
    Objects.requireNonNull(layout, "layout");
    return asSlice(offset, layout.byteSize(), layout.byteAlignment());
  }

  @Override
  public MemorySegment asSlice(long offset, long newSize, long byteAlignment) {
    checkBounds(offset, newSize);
    checkAlignment(offset, byteAlignment);
    return asSlice(offset, newSize);
  }

  @Override
  public MemorySegment fill(byte value) {
    checkWrite();
    if (byteSize > 0) {
      Object base = heapBase().orElse(null);
      long rawOffset = isNative() ? address() : (arrayBaseOffset() + baseOffset());
      UNSAFE.setMemory(base, rawOffset, byteSize, value);
    }
    return this;
  }

  @Override
  public MemorySegment copyFrom(MemorySegment src) {
    checkWrite();
    Objects.requireNonNull(src, "src");
    if (byteSize < src.byteSize()) {
      throw new IndexOutOfBoundsException("Target segment smaller than source segment: " + byteSize + " < " + src.byteSize());
    }
    MemorySegment.copy(src, 0L, this, 0L, src.byteSize());
    return this;
  }

  @Override
  public long mismatch(MemorySegment other) {
    Objects.requireNonNull(other, "other");
    return mismatchRange(this, 0L, byteSize, other, 0L, other.byteSize());
  }

  @Override
  public Stream<MemorySegment> elements(MemoryLayout layout) {
    Objects.requireNonNull(layout, "layout");
    checkAccess();
    long elemSize = layout.byteSize();
    if (elemSize <= 0) {
      throw new IllegalArgumentException("Layout size must be positive: " + elemSize);
    }
    if (byteSize % elemSize != 0) {
      throw new IllegalArgumentException("Segment size is not a multiple of layout size");
    }
    if (layout.byteAlignment() > maxByteAlignment()) {
      throw new IllegalArgumentException("Incompatible alignment constraints");
    }
    checkAlignment(0L, layout.byteAlignment());
    long count = byteSize / elemSize;
    return LongStream.range(0, count).mapToObj(i -> asSlice(i * elemSize, elemSize));
  }

  @Override
  public Spliterator<MemorySegment> spliterator(MemoryLayout layout) {
    return elements(layout).spliterator();
  }

  @Override
  public String getString(long offset, Charset charset) {
    Objects.requireNonNull(charset, "charset");
    checkAccess();
    if (offset < 0 || offset > byteSize) {
      throw new IndexOutOfBoundsException("Offset out of bounds: " + offset);
    }
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    long cur = offset;
    while (cur < byteSize) {
      byte b = get(ValueLayout.JAVA_BYTE, cur++);
      if (b == 0) {
        return new String(baos.toByteArray(), charset);
      }
      baos.write(b);
    }
    throw new IllegalArgumentException("String is not null-terminated within segment bounds");
  }

  @Override
  public void setString(long offset, String str, Charset charset) {
    Objects.requireNonNull(str, "str");
    Objects.requireNonNull(charset, "charset");
    checkWrite();
    byte[] bytes = str.getBytes(charset);
    checkBounds(offset, bytes.length + 1L);
    for (int i = 0; i < bytes.length; i++) {
      set(ValueLayout.JAVA_BYTE, offset + i, bytes[i]);
    }
    set(ValueLayout.JAVA_BYTE, offset + bytes.length, (byte) 0);
  }

  @Override
  public boolean isLoaded() {
    checkAccess();
    return true;
  }

  @Override
  public void load() {
    checkAccess();
  }

  @Override
  public void unload() {
    checkAccess();
  }

  @Override
  public void force() {
    checkAccess();
  }

  @Override
  public boolean isMapped() {
    return false;
  }

  abstract long arrayBaseOffset();

  abstract long baseOffset();

  static MemorySegment ofBuffer(Buffer buffer) {
    Objects.requireNonNull(buffer, "buffer");
    if (buffer instanceof ByteBuffer) {
      ByteBuffer bb = (ByteBuffer) buffer;
      if (bb.isDirect()) {
        long addr = ((java.nio.DirectByteBuffer) bb).address();
        return new NativeMemorySegment(addr + bb.position(), bb.remaining(),
            GlobalArena.INSTANCE.scope(), bb.isReadOnly(), 1L);
      } else if (bb.hasArray()) {
        return new HeapMemorySegment(bb.array(), bb.arrayOffset() + bb.position(),
            bb.remaining(), 1L, GlobalArena.INSTANCE.scope(), bb.isReadOnly());
      }
    }
    throw new IllegalArgumentException("Unsupported buffer type: " + buffer.getClass().getName());
  }

  static void copyBytes(MemorySegment src, long srcOffset, MemorySegment dst, long dstOffset, long bytes) {
    Objects.requireNonNull(src, "src");
    Objects.requireNonNull(dst, "dst");
    if (bytes < 0) {
      throw new IllegalArgumentException("Negative bytes: " + bytes);
    }
    if (bytes == 0) {
      return;
    }
    AbstractMemorySegment srcImpl = (AbstractMemorySegment) src;
    AbstractMemorySegment dstImpl = (AbstractMemorySegment) dst;
    srcImpl.checkAccess();
    dstImpl.checkWrite();
    srcImpl.checkBounds(srcOffset, bytes);
    dstImpl.checkBounds(dstOffset, bytes);

    Object srcBase = srcImpl.heapBase().orElse(null);
    long srcAddr = srcImpl.isNative() ? (srcImpl.address() + srcOffset)
        : (srcImpl.arrayBaseOffset() + srcImpl.baseOffset() + srcOffset);

    Object dstBase = dstImpl.heapBase().orElse(null);
    long dstAddr = dstImpl.isNative() ? (dstImpl.address() + dstOffset)
        : (dstImpl.arrayBaseOffset() + dstImpl.baseOffset() + dstOffset);

    UNSAFE.copyMemory(srcBase, srcAddr, dstBase, dstAddr, bytes);
  }

  static void copyElements(MemorySegment src, ValueLayout srcLayout, long srcOffset,
                           MemorySegment dst, ValueLayout dstLayout, long dstOffset, long elementCount) {
    Objects.requireNonNull(srcLayout, "srcLayout");
    Objects.requireNonNull(dstLayout, "dstLayout");
    if (srcLayout.byteSize() != dstLayout.byteSize()) {
      throw new IllegalArgumentException("Layout sizes mismatch: " + srcLayout.byteSize() + " vs " + dstLayout.byteSize());
    }
    if (elementCount < 0) {
      throw new IllegalArgumentException("Negative elementCount: " + elementCount);
    }
    long totalBytes = Math.multiplyExact(elementCount, srcLayout.byteSize());
    if (srcLayout.order() == dstLayout.order()) {
      copyBytes(src, srcOffset, dst, dstOffset, totalBytes);
    } else {
      long sz = srcLayout.byteSize();
      for (long i = 0; i < elementCount; i++) {
        long sOff = srcOffset + i * sz;
        long dOff = dstOffset + i * sz;
        if (sz == 1) {
          dst.set((ValueLayout.OfByte) dstLayout, dOff, src.get((ValueLayout.OfByte) srcLayout, sOff));
        } else if (sz == 2) {
          short val = Short.reverseBytes(src.get((ValueLayout.OfShort) srcLayout, sOff));
          dst.set((ValueLayout.OfShort) dstLayout, dOff, val);
        } else if (sz == 4) {
          int val = Integer.reverseBytes(src.get((ValueLayout.OfInt) srcLayout, sOff));
          dst.set((ValueLayout.OfInt) dstLayout, dOff, val);
        } else if (sz == 8) {
          long val = Long.reverseBytes(src.get((ValueLayout.OfLong) srcLayout, sOff));
          dst.set((ValueLayout.OfLong) dstLayout, dOff, val);
        }
      }
    }
  }

  static void copyToArray(MemorySegment src, ValueLayout srcLayout, long srcOffset,
                          Object dstArray, int dstIndex, int elementCount) {
    Objects.requireNonNull(src, "src");
    Objects.requireNonNull(srcLayout, "srcLayout");
    Objects.requireNonNull(dstArray, "dstArray");
    if (dstIndex < 0 || elementCount < 0) {
      throw new IndexOutOfBoundsException();
    }
    long sz = srcLayout.byteSize();
    long totalBytes = Math.multiplyExact((long) elementCount, sz);
    AbstractMemorySegment srcImpl = (AbstractMemorySegment) src;
    srcImpl.checkAccess();
    srcImpl.checkBounds(srcOffset, totalBytes);

    int baseOffset = UNSAFE.arrayBaseOffset(dstArray.getClass());
    int scale = UNSAFE.arrayIndexScale(dstArray.getClass());
    if (scale != (int) sz) {
      throw new IllegalArgumentException("Array element size mismatch");
    }
    long arrOffset = baseOffset + (long) dstIndex * scale;

    if (srcLayout.order() == ByteOrder.nativeOrder()) {
      Object srcBase = srcImpl.heapBase().orElse(null);
      long srcAddr = srcImpl.isNative() ? (srcImpl.address() + srcOffset)
          : (srcImpl.arrayBaseOffset() + srcImpl.baseOffset() + srcOffset);
      UNSAFE.copyMemory(srcBase, srcAddr, dstArray, arrOffset, totalBytes);
    } else {
      if (dstArray instanceof short[]) {
        short[] arr = (short[]) dstArray;
        for (int i = 0; i < elementCount; i++) {
          arr[dstIndex + i] = src.get((ValueLayout.OfShort) srcLayout, srcOffset + (long) i * sz);
        }
      } else if (dstArray instanceof char[]) {
        char[] arr = (char[]) dstArray;
        for (int i = 0; i < elementCount; i++) {
          arr[dstIndex + i] = src.get((ValueLayout.OfChar) srcLayout, srcOffset + (long) i * sz);
        }
      } else if (dstArray instanceof int[]) {
        int[] arr = (int[]) dstArray;
        for (int i = 0; i < elementCount; i++) {
          arr[dstIndex + i] = src.get((ValueLayout.OfInt) srcLayout, srcOffset + (long) i * sz);
        }
      } else if (dstArray instanceof long[]) {
        long[] arr = (long[]) dstArray;
        for (int i = 0; i < elementCount; i++) {
          arr[dstIndex + i] = src.get((ValueLayout.OfLong) srcLayout, srcOffset + (long) i * sz);
        }
      } else if (dstArray instanceof float[]) {
        float[] arr = (float[]) dstArray;
        for (int i = 0; i < elementCount; i++) {
          arr[dstIndex + i] = src.get((ValueLayout.OfFloat) srcLayout, srcOffset + (long) i * sz);
        }
      } else if (dstArray instanceof double[]) {
        double[] arr = (double[]) dstArray;
        for (int i = 0; i < elementCount; i++) {
          arr[dstIndex + i] = src.get((ValueLayout.OfDouble) srcLayout, srcOffset + (long) i * sz);
        }
      }
    }
  }

  static void copyFromArray(Object srcArray, int srcIndex,
                            MemorySegment dst, ValueLayout dstLayout, long dstOffset, int elementCount) {
    Objects.requireNonNull(srcArray, "srcArray");
    Objects.requireNonNull(dst, "dst");
    Objects.requireNonNull(dstLayout, "dstLayout");
    if (srcIndex < 0 || elementCount < 0) {
      throw new IndexOutOfBoundsException();
    }
    long sz = dstLayout.byteSize();
    long totalBytes = Math.multiplyExact((long) elementCount, sz);
    AbstractMemorySegment dstImpl = (AbstractMemorySegment) dst;
    dstImpl.checkWrite();
    dstImpl.checkBounds(dstOffset, totalBytes);

    int baseOffset = UNSAFE.arrayBaseOffset(srcArray.getClass());
    int scale = UNSAFE.arrayIndexScale(srcArray.getClass());
    if (scale != (int) sz) {
      throw new IllegalArgumentException("Array element size mismatch");
    }
    long arrOffset = baseOffset + (long) srcIndex * scale;

    if (dstLayout.order() == ByteOrder.nativeOrder()) {
      Object dstBase = dstImpl.heapBase().orElse(null);
      long dstAddr = dstImpl.isNative() ? (dstImpl.address() + dstOffset)
          : (dstImpl.arrayBaseOffset() + dstImpl.baseOffset() + dstOffset);
      UNSAFE.copyMemory(srcArray, arrOffset, dstBase, dstAddr, totalBytes);
    } else {
      if (srcArray instanceof short[]) {
        short[] arr = (short[]) srcArray;
        for (int i = 0; i < elementCount; i++) {
          dst.set((ValueLayout.OfShort) dstLayout, dstOffset + (long) i * sz, arr[srcIndex + i]);
        }
      } else if (srcArray instanceof char[]) {
        char[] arr = (char[]) srcArray;
        for (int i = 0; i < elementCount; i++) {
          dst.set((ValueLayout.OfChar) dstLayout, dstOffset + (long) i * sz, arr[srcIndex + i]);
        }
      } else if (srcArray instanceof int[]) {
        int[] arr = (int[]) srcArray;
        for (int i = 0; i < elementCount; i++) {
          dst.set((ValueLayout.OfInt) dstLayout, dstOffset + (long) i * sz, arr[srcIndex + i]);
        }
      } else if (srcArray instanceof long[]) {
        long[] arr = (long[]) srcArray;
        for (int i = 0; i < elementCount; i++) {
          dst.set((ValueLayout.OfLong) dstLayout, dstOffset + (long) i * sz, arr[srcIndex + i]);
        }
      } else if (srcArray instanceof float[]) {
        float[] arr = (float[]) srcArray;
        for (int i = 0; i < elementCount; i++) {
          dst.set((ValueLayout.OfFloat) dstLayout, dstOffset + (long) i * sz, arr[srcIndex + i]);
        }
      } else if (srcArray instanceof double[]) {
        double[] arr = (double[]) srcArray;
        for (int i = 0; i < elementCount; i++) {
          dst.set((ValueLayout.OfDouble) dstLayout, dstOffset + (long) i * sz, arr[srcIndex + i]);
        }
      }
    }
  }

  static long mismatchRange(MemorySegment src, long srcFromOffset, long srcToOffset,
                            MemorySegment dst, long dstFromOffset, long dstToOffset) {
    Objects.requireNonNull(src, "src");
    Objects.requireNonNull(dst, "dst");
    AbstractMemorySegment srcImpl = (AbstractMemorySegment) src;
    AbstractMemorySegment dstImpl = (AbstractMemorySegment) dst;
    srcImpl.checkAccess();
    dstImpl.checkAccess();
    long srcLen = srcToOffset - srcFromOffset;
    long dstLen = dstToOffset - dstFromOffset;
    if (srcLen < 0 || dstLen < 0) {
      throw new IllegalArgumentException("Negative slice length");
    }
    srcImpl.checkBounds(srcFromOffset, srcLen);
    dstImpl.checkBounds(dstFromOffset, dstLen);

    long minLen = Math.min(srcLen, dstLen);
    for (long i = 0; i < minLen; i++) {
      byte b1 = src.get(ValueLayout.JAVA_BYTE, srcFromOffset + i);
      byte b2 = dst.get(ValueLayout.JAVA_BYTE, dstFromOffset + i);
      if (b1 != b2) {
        return i;
      }
    }
    return srcLen != dstLen ? minLen : -1L;
  }
}
