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

public abstract class ShortBuffer extends Buffer implements Comparable<ShortBuffer> {
  final short[] hb;
  final int offset;
  boolean isReadOnly;

  ShortBuffer(int mark, int pos, int lim, int cap, short[] hb, int offset) {
    super(mark, pos, lim, cap);
    this.hb = hb;
    this.offset = offset;
  }

  ShortBuffer(int mark, int pos, int lim, int cap) {
    this(mark, pos, lim, cap, null, 0);
  }

  public static ShortBuffer allocate(int capacity) {
    if (capacity < 0) {
      throw createCapacityException(capacity);
    }
    return new HeapShortBuffer(capacity, capacity);
  }

  public static ShortBuffer wrap(short[] array, int offset, int length) {
    try {
      return new HeapShortBuffer(array, offset, length);
    } catch (IllegalArgumentException x) {
      throw new IndexOutOfBoundsException();
    }
  }

  public static ShortBuffer wrap(short[] array) {
    return wrap(array, 0, array.length);
  }

  @Override
  public abstract ShortBuffer slice();

  @Override
  public abstract ShortBuffer slice(int index, int length);

  @Override
  public abstract ShortBuffer duplicate();

  public abstract ShortBuffer asReadOnlyBuffer();

  public abstract short get();

  public abstract ShortBuffer put(short x);

  public abstract short get(int index);

  public abstract ShortBuffer put(int index, short x);

  public ShortBuffer get(short[] dst, int offset, int length) {
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

  public ShortBuffer get(short[] dst) {
    return get(dst, 0, dst.length);
  }

  public ShortBuffer get(int index, short[] dst, int offset, int length) {
    Objects.checkFromIndexSize(index, length, limit());
    Objects.checkFromIndexSize(offset, length, dst.length);
    for (int i = offset, j = index; i < offset + length; i++, j++) {
      dst[i] = get(j);
    }
    return this;
  }

  public ShortBuffer get(int index, short[] dst) {
    return get(index, dst, 0, dst.length);
  }

  public ShortBuffer put(ShortBuffer src) {
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
    short[] tmp = new short[srcRem];
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

  public ShortBuffer put(int index, ShortBuffer src, int offset, int length) {
    Objects.checkFromIndexSize(index, length, limit());
    Objects.checkFromIndexSize(offset, length, src.limit());
    if (isReadOnly()) {
      throw new ReadOnlyBufferException();
    }
    short[] tmp = new short[length];
    for (int i = 0; i < length; i++) {
      tmp[i] = src.get(offset + i);
    }
    for (int i = 0; i < length; i++) {
      put(index + i, tmp[i]);
    }
    return this;
  }

  public ShortBuffer put(short[] src, int offset, int length) {
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

  public final ShortBuffer put(short[] src) {
    return put(src, 0, src.length);
  }

  public ShortBuffer put(int index, short[] src, int offset, int length) {
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

  public ShortBuffer put(int index, short[] src) {
    return put(index, src, 0, src.length);
  }

  @Override
  public final boolean hasArray() {
    return hb != null && !isReadOnly;
  }

  @Override
  public final short[] array() {
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
  public final ShortBuffer position(int newPosition) {
    super.position(newPosition);
    return this;
  }

  @Override
  public final ShortBuffer limit(int newLimit) {
    super.limit(newLimit);
    return this;
  }

  @Override
  public final ShortBuffer mark() {
    super.mark();
    return this;
  }

  @Override
  public final ShortBuffer reset() {
    super.reset();
    return this;
  }

  @Override
  public final ShortBuffer clear() {
    super.clear();
    return this;
  }

  @Override
  public final ShortBuffer flip() {
    super.flip();
    return this;
  }

  @Override
  public final ShortBuffer rewind() {
    super.rewind();
    return this;
  }

  public abstract ShortBuffer compact();

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

  private static boolean same(short x, short y) {
    return x == y;
  }

  private static int compare(short x, short y) {
    return Short.compare(x, y);
  }

  private static int mismatch(ShortBuffer a, int aPos, ShortBuffer b, int bPos, int length) {
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
    if (!(ob instanceof ShortBuffer)) {
      return false;
    }
    ShortBuffer that = (ShortBuffer) ob;
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
  public int compareTo(ShortBuffer that) {
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

  public int mismatch(ShortBuffer that) {
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
