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

public abstract class LongBuffer extends Buffer implements Comparable<LongBuffer> {
  final long[] hb;
  final int offset;
  boolean isReadOnly;

  LongBuffer(int mark, int pos, int lim, int cap, long[] hb, int offset) {
    super(mark, pos, lim, cap);
    this.hb = hb;
    this.offset = offset;
  }

  LongBuffer(int mark, int pos, int lim, int cap) {
    this(mark, pos, lim, cap, null, 0);
  }

  public static LongBuffer allocate(int capacity) {
    if (capacity < 0) {
      throw createCapacityException(capacity);
    }
    return new HeapLongBuffer(capacity, capacity);
  }

  public static LongBuffer wrap(long[] array, int offset, int length) {
    try {
      return new HeapLongBuffer(array, offset, length);
    } catch (IllegalArgumentException x) {
      throw new IndexOutOfBoundsException();
    }
  }

  public static LongBuffer wrap(long[] array) {
    return wrap(array, 0, array.length);
  }

  @Override
  public abstract LongBuffer slice();

  @Override
  public abstract LongBuffer slice(int index, int length);

  @Override
  public abstract LongBuffer duplicate();

  public abstract LongBuffer asReadOnlyBuffer();

  public abstract long get();

  public abstract LongBuffer put(long x);

  public abstract long get(int index);

  public abstract LongBuffer put(int index, long x);

  public LongBuffer get(long[] dst, int offset, int length) {
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

  public LongBuffer get(long[] dst) {
    return get(dst, 0, dst.length);
  }

  public LongBuffer get(int index, long[] dst, int offset, int length) {
    Objects.checkFromIndexSize(index, length, limit());
    Objects.checkFromIndexSize(offset, length, dst.length);
    for (int i = offset, j = index; i < offset + length; i++, j++) {
      dst[i] = get(j);
    }
    return this;
  }

  public LongBuffer get(int index, long[] dst) {
    return get(index, dst, 0, dst.length);
  }

  public LongBuffer put(LongBuffer src) {
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
    long[] tmp = new long[srcRem];
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

  public LongBuffer put(int index, LongBuffer src, int offset, int length) {
    Objects.checkFromIndexSize(index, length, limit());
    Objects.checkFromIndexSize(offset, length, src.limit());
    if (isReadOnly()) {
      throw new ReadOnlyBufferException();
    }
    long[] tmp = new long[length];
    for (int i = 0; i < length; i++) {
      tmp[i] = src.get(offset + i);
    }
    for (int i = 0; i < length; i++) {
      put(index + i, tmp[i]);
    }
    return this;
  }

  public LongBuffer put(long[] src, int offset, int length) {
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

  public final LongBuffer put(long[] src) {
    return put(src, 0, src.length);
  }

  public LongBuffer put(int index, long[] src, int offset, int length) {
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

  public LongBuffer put(int index, long[] src) {
    return put(index, src, 0, src.length);
  }

  @Override
  public final boolean hasArray() {
    return hb != null && !isReadOnly;
  }

  @Override
  public final long[] array() {
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
  public final LongBuffer position(int newPosition) {
    super.position(newPosition);
    return this;
  }

  @Override
  public final LongBuffer limit(int newLimit) {
    super.limit(newLimit);
    return this;
  }

  @Override
  public final LongBuffer mark() {
    super.mark();
    return this;
  }

  @Override
  public final LongBuffer reset() {
    super.reset();
    return this;
  }

  @Override
  public final LongBuffer clear() {
    super.clear();
    return this;
  }

  @Override
  public final LongBuffer flip() {
    super.flip();
    return this;
  }

  @Override
  public final LongBuffer rewind() {
    super.rewind();
    return this;
  }

  public abstract LongBuffer compact();

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

  private static boolean same(long x, long y) {
    return x == y;
  }

  private static int compare(long x, long y) {
    return Long.compare(x, y);
  }

  private static int mismatch(LongBuffer a, int aPos, LongBuffer b, int bPos, int length) {
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
    if (!(ob instanceof LongBuffer)) {
      return false;
    }
    LongBuffer that = (LongBuffer) ob;
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
  public int compareTo(LongBuffer that) {
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

  public int mismatch(LongBuffer that) {
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
