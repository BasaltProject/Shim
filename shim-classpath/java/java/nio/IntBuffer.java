/*
 * Based on Avian (https://github.com/ReadyTalk/avian).
 * Original work Copyright (c) 2008-2015, Avian Contributors.
 * Original work licensed under ISC; see LICENSE.txt:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE.txt
 *
 * Modifications Copyright (c) 2026 BasaltMC.
 * Modifications licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: ISC AND MIT
 */


package java.nio;

import java.util.Objects;

public abstract class IntBuffer extends Buffer implements Comparable<IntBuffer> {
  final int[] hb;
  final int offset;
  boolean isReadOnly;

  IntBuffer(int mark, int pos, int lim, int cap, int[] hb, int offset) {
    super(mark, pos, lim, cap);
    this.hb = hb;
    this.offset = offset;
  }

  IntBuffer(int mark, int pos, int lim, int cap) {
    this(mark, pos, lim, cap, null, 0);
  }

  public static IntBuffer allocate(int capacity) {
    if (capacity < 0) {
      throw createCapacityException(capacity);
    }
    return new HeapIntBuffer(capacity, capacity);
  }

  public static IntBuffer wrap(int[] array, int offset, int length) {
    try {
      return new HeapIntBuffer(array, offset, length);
    } catch (IllegalArgumentException x) {
      throw new IndexOutOfBoundsException();
    }
  }

  public static IntBuffer wrap(int[] array) {
    return wrap(array, 0, array.length);
  }

  @Override
  public abstract IntBuffer slice();

  @Override
  public abstract IntBuffer slice(int index, int length);

  @Override
  public abstract IntBuffer duplicate();

  public abstract IntBuffer asReadOnlyBuffer();

  public abstract int get();

  public abstract IntBuffer put(int x);

  public abstract int get(int index);

  public abstract IntBuffer put(int index, int x);

  public IntBuffer get(int[] dst, int offset, int length) {
    Objects.checkFromIndexSize(offset, length, dst.length);
    int pos = position();
    if (length > limit() - pos) {
      throw new BufferUnderflowException();
    }
    for (int i = offset; i < offset + length; i++) {
      dst[i] = get();
    }
    return this;
  }

  public IntBuffer get(int[] dst) {
    return get(dst, 0, dst.length);
  }

  public IntBuffer get(int index, int[] dst, int offset, int length) {
    Objects.checkFromIndexSize(index, length, limit());
    Objects.checkFromIndexSize(offset, length, dst.length);
    for (int i = offset, j = index; i < offset + length; i++, j++) {
      dst[i] = get(j);
    }
    return this;
  }

  public IntBuffer get(int index, int[] dst) {
    return get(index, dst, 0, dst.length);
  }

  public IntBuffer put(IntBuffer src) {
    if (src == this) {
      throw createSameBufferException();
    }
    if (isReadOnly()) {
      throw new ReadOnlyBufferException();
    }
    int srcPos = src.position();
    int srcLim = src.limit();
    int srcRem = srcPos <= srcLim ? srcLim - srcPos : 0;
    int pos = position();
    int lim = limit();
    int rem = pos <= lim ? lim - pos : 0;
    if (srcRem > rem) {
      throw new BufferOverflowException();
    }
    int[] tmp = new int[srcRem];
    for (int i = 0; i < srcRem; i++) {
      tmp[i] = src.get(srcPos + i);
    }
    for (int i = 0; i < srcRem; i++) {
      put(pos + i, tmp[i]);
    }
    src.position(srcPos + srcRem);
    position(pos + srcRem);
    return this;
  }

  public IntBuffer put(int index, IntBuffer src, int offset, int length) {
    Objects.checkFromIndexSize(index, length, limit());
    Objects.checkFromIndexSize(offset, length, src.limit());
    if (isReadOnly()) {
      throw new ReadOnlyBufferException();
    }
    int[] tmp = new int[length];
    for (int i = 0; i < length; i++) {
      tmp[i] = src.get(offset + i);
    }
    for (int i = 0; i < length; i++) {
      put(index + i, tmp[i]);
    }
    return this;
  }

  public IntBuffer put(int[] src, int offset, int length) {
    if (isReadOnly()) {
      throw new ReadOnlyBufferException();
    }
    Objects.checkFromIndexSize(offset, length, src.length);
    int pos = position();
    if (length > limit() - pos) {
      throw new BufferOverflowException();
    }
    for (int i = offset; i < offset + length; i++) {
      put(src[i]);
    }
    return this;
  }

  public final IntBuffer put(int[] src) {
    return put(src, 0, src.length);
  }

  public IntBuffer put(int index, int[] src, int offset, int length) {
    if (isReadOnly()) {
      throw new ReadOnlyBufferException();
    }
    Objects.checkFromIndexSize(index, length, limit());
    Objects.checkFromIndexSize(offset, length, src.length);
    for (int i = offset, j = index; i < offset + length; i++, j++) {
      put(j, src[i]);
    }
    return this;
  }

  public IntBuffer put(int index, int[] src) {
    return put(index, src, 0, src.length);
  }

  @Override
  public final boolean hasArray() {
    return hb != null && !isReadOnly;
  }

  @Override
  public final int[] array() {
    if (hb == null) {
      throw new UnsupportedOperationException();
    }
    if (isReadOnly) {
      throw new ReadOnlyBufferException();
    }
    return hb;
  }

  @Override
  public final int arrayOffset() {
    if (hb == null) {
      throw new UnsupportedOperationException();
    }
    if (isReadOnly) {
      throw new ReadOnlyBufferException();
    }
    return offset;
  }

  @Override
  public final IntBuffer position(int newPosition) {
    super.position(newPosition);
    return this;
  }

  @Override
  public final IntBuffer limit(int newLimit) {
    super.limit(newLimit);
    return this;
  }

  @Override
  public final IntBuffer mark() {
    super.mark();
    return this;
  }

  @Override
  public final IntBuffer reset() {
    super.reset();
    return this;
  }

  @Override
  public final IntBuffer clear() {
    super.clear();
    return this;
  }

  @Override
  public final IntBuffer flip() {
    super.flip();
    return this;
  }

  @Override
  public final IntBuffer rewind() {
    super.rewind();
    return this;
  }

  public abstract IntBuffer compact();

  @Override
  public abstract boolean isDirect();

  public abstract ByteOrder order();

  @Override
  public int hashCode() {
    int h = 1;
    int p = position();
    for (int i = limit() - 1; i >= p; i--) {
      h = 31 * h + (int) get(i);
    }
    return h;
  }

  private static boolean same(int x, int y) {
    return x == y;
  }

  private static int compare(int x, int y) {
    return Integer.compare(x, y);
  }

  private static int mismatch(IntBuffer a, int aPos, IntBuffer b, int bPos, int length) {
    for (int i = 0; i < length; i++) {
      if (!same(a.get(aPos + i), b.get(bPos + i))) {
        return i;
      }
    }
    return -1;
  }

  @Override
  public boolean equals(Object ob) {
    if (this == ob) {
      return true;
    }
    if (!(ob instanceof IntBuffer)) {
      return false;
    }
    IntBuffer that = (IntBuffer) ob;
    int thisPos = this.position();
    int thisRem = this.limit() - thisPos;
    int thatPos = that.position();
    int thatRem = that.limit() - thatPos;
    if (thisRem < 0 || thisRem != thatRem) {
      return false;
    }
    return mismatch(this, thisPos, that, thatPos, thisRem) < 0;
  }

  @Override
  public int compareTo(IntBuffer that) {
    int thisPos = this.position();
    int thisRem = this.limit() - thisPos;
    int thatPos = that.position();
    int thatRem = that.limit() - thatPos;
    int length = Math.min(thisRem, thatRem);
    if (length < 0) {
      return -1;
    }
    int i = mismatch(this, thisPos, that, thatPos, length);
    if (i >= 0) {
      return compare(this.get(thisPos + i), that.get(thatPos + i));
    }
    return thisRem - thatRem;
  }

  public int mismatch(IntBuffer that) {
    int thisPos = this.position();
    int thisRem = this.limit() - thisPos;
    int thatPos = that.position();
    int thatRem = that.limit() - thatPos;
    int length = Math.min(thisRem, thatRem);
    if (length < 0) {
      return -1;
    }
    int r = mismatch(this, thisPos, that, thatPos, length);
    return (r == -1 && thisRem != thatRem) ? length : r;
  }

  @Override
  public String toString() {
    return bufferName() + "[pos=" + position() + " lim=" + limit() + " cap=" + capacity() + "]";
  }
}
