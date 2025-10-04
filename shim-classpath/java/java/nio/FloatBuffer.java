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

public abstract class FloatBuffer extends Buffer implements Comparable<FloatBuffer> {
  final float[] hb;
  final int offset;
  boolean isReadOnly;

  FloatBuffer(int mark, int pos, int lim, int cap, float[] hb, int offset) {
    super(mark, pos, lim, cap);
    this.hb = hb;
    this.offset = offset;
  }

  FloatBuffer(int mark, int pos, int lim, int cap) {
    this(mark, pos, lim, cap, null, 0);
  }

  public static FloatBuffer allocate(int capacity) {
    if (capacity < 0) {
      throw createCapacityException(capacity);
    }
    return new HeapFloatBuffer(capacity, capacity);
  }

  public static FloatBuffer wrap(float[] array, int offset, int length) {
    try {
      return new HeapFloatBuffer(array, offset, length);
    } catch (IllegalArgumentException x) {
      throw new IndexOutOfBoundsException();
    }
  }

  public static FloatBuffer wrap(float[] array) {
    return wrap(array, 0, array.length);
  }

  @Override
  public abstract FloatBuffer slice();

  @Override
  public abstract FloatBuffer slice(int index, int length);

  @Override
  public abstract FloatBuffer duplicate();

  public abstract FloatBuffer asReadOnlyBuffer();

  public abstract float get();

  public abstract FloatBuffer put(float x);

  public abstract float get(int index);

  public abstract FloatBuffer put(int index, float x);

  public FloatBuffer get(float[] dst, int offset, int length) {
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

  public FloatBuffer get(float[] dst) {
    return get(dst, 0, dst.length);
  }

  public FloatBuffer get(int index, float[] dst, int offset, int length) {
    Objects.checkFromIndexSize(index, length, limit());
    Objects.checkFromIndexSize(offset, length, dst.length);
    for (int i = offset, j = index; i < offset + length; i++, j++) {
      dst[i] = get(j);
    }
    return this;
  }

  public FloatBuffer get(int index, float[] dst) {
    return get(index, dst, 0, dst.length);
  }

  public FloatBuffer put(FloatBuffer src) {
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
    float[] tmp = new float[srcRem];
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

  public FloatBuffer put(int index, FloatBuffer src, int offset, int length) {
    Objects.checkFromIndexSize(index, length, limit());
    Objects.checkFromIndexSize(offset, length, src.limit());
    if (isReadOnly()) {
      throw new ReadOnlyBufferException();
    }
    float[] tmp = new float[length];
    for (int i = 0; i < length; i++) {
      tmp[i] = src.get(offset + i);
    }
    for (int i = 0; i < length; i++) {
      put(index + i, tmp[i]);
    }
    return this;
  }

  public FloatBuffer put(float[] src, int offset, int length) {
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

  public final FloatBuffer put(float[] src) {
    return put(src, 0, src.length);
  }

  public FloatBuffer put(int index, float[] src, int offset, int length) {
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

  public FloatBuffer put(int index, float[] src) {
    return put(index, src, 0, src.length);
  }

  @Override
  public final boolean hasArray() {
    return hb != null && !isReadOnly;
  }

  @Override
  public final float[] array() {
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
  public final FloatBuffer position(int newPosition) {
    super.position(newPosition);
    return this;
  }

  @Override
  public final FloatBuffer limit(int newLimit) {
    super.limit(newLimit);
    return this;
  }

  @Override
  public final FloatBuffer mark() {
    super.mark();
    return this;
  }

  @Override
  public final FloatBuffer reset() {
    super.reset();
    return this;
  }

  @Override
  public final FloatBuffer clear() {
    super.clear();
    return this;
  }

  @Override
  public final FloatBuffer flip() {
    super.flip();
    return this;
  }

  @Override
  public final FloatBuffer rewind() {
    super.rewind();
    return this;
  }

  public abstract FloatBuffer compact();

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

  private static boolean same(float x, float y) {
    return (x == y) || (Float.isNaN(x) && Float.isNaN(y));
  }

  private static int compare(float x, float y) {
    return (x < y) ? -1 : (x > y) ? +1 : (x == y) ? 0 : Float.isNaN(x) ? (Float.isNaN(y) ? 0 : +1) : -1;
  }

  private static int mismatch(FloatBuffer a, int aPos, FloatBuffer b, int bPos, int length) {
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
    if (!(ob instanceof FloatBuffer)) {
      return false;
    }
    FloatBuffer that = (FloatBuffer) ob;
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
  public int compareTo(FloatBuffer that) {
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

  public int mismatch(FloatBuffer that) {
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
