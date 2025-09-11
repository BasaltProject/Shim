/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.nio;

import java.util.Objects;

public abstract class LongBuffer extends Buffer implements Comparable<LongBuffer> {

  private final boolean readOnly;

  protected LongBuffer(boolean readOnly) {
    this.readOnly = readOnly;
    this.readonly = readOnly;
  }

  public static LongBuffer allocate(int capacity) {
    if (capacity < 0) throw new IllegalArgumentException("capacity < 0: " + capacity);
    return new ArrayLongBuffer(new long[capacity], 0, capacity, false);
  }

  public static LongBuffer wrap(long[] array, int offset, int length) {
    Objects.checkFromIndexSize(offset, length, array.length);
    return new ArrayLongBuffer(array, offset, length, false);
  }

  public static LongBuffer wrap(long[] array) {
    return wrap(array, 0, array.length);
  }

  public abstract LongBuffer slice();

  public abstract LongBuffer slice(int index, int length);

  public abstract LongBuffer duplicate();

  public abstract LongBuffer asReadOnlyBuffer();

  public abstract long get();

  public abstract LongBuffer put(long l);

  public abstract long get(int index);

  public abstract LongBuffer put(int index, long l);

  public LongBuffer get(long[] dst, int offset, int length) {
    Objects.checkFromIndexSize(offset, length, dst.length);
    checkGet(position, length, false);
    for (int i = 0; i < length; i++) {
      dst[offset + i] = get();
    }
    return this;
  }

  public LongBuffer get(long[] dst) {
    return get(dst, 0, dst.length);
  }

  public LongBuffer put(LongBuffer src) {
    if (src == this) throw new IllegalArgumentException();
    if (readOnly) throw new ReadOnlyBufferException();
    int n = src.remaining();
    checkPut(position, n, false);
    for (int i = 0; i < n; i++) {
      put(src.get());
    }
    return this;
  }

  public LongBuffer put(long[] src, int offset, int length) {
    Objects.checkFromIndexSize(offset, length, src.length);
    checkPut(position, length, false);
    for (int i = 0; i < length; i++) {
      put(src[offset + i]);
    }
    return this;
  }

  public final LongBuffer put(long[] src) {
    return put(src, 0, src.length);
  }

  public boolean hasArray() {
    return false;
  }

  public long[] array() {
    throw new UnsupportedOperationException();
  }

  public int arrayOffset() {
    throw new UnsupportedOperationException();
  }

  public abstract LongBuffer compact();

  public abstract boolean isDirect();

  public boolean isReadOnly() {
    return readOnly;
  }

  public ByteOrder order() {
    return ByteOrder.BIG_ENDIAN;
  }

  protected void checkPut(int pos, int amount, boolean absolute) {
    if (readOnly) {
      throw new ReadOnlyBufferException();
    }
    if (pos < 0 || pos + amount > limit) {
      throw absolute ? new IndexOutOfBoundsException() : new BufferOverflowException();
    }
  }

  protected void checkGet(int pos, int amount, boolean absolute) {
    if (amount > limit - pos || pos < 0) {
      throw absolute ? new IndexOutOfBoundsException() : new BufferUnderflowException();
    }
  }

  @Override
  public LongBuffer position(int newPosition) {
    super.position(newPosition);
    return this;
  }

  @Override
  public LongBuffer limit(int newLimit) {
    super.limit(newLimit);
    return this;
  }

  @Override
  public LongBuffer mark() {
    super.mark();
    return this;
  }

  @Override
  public LongBuffer reset() {
    super.reset();
    return this;
  }

  @Override
  public LongBuffer clear() {
    super.clear();
    return this;
  }

  @Override
  public LongBuffer flip() {
    super.flip();
    return this;
  }

  @Override
  public LongBuffer rewind() {
    super.rewind();
    return this;
  }

  @Override
  public int hashCode() {
    int h = 1;
    int p = position();
    for (int i = limit() - 1; i >= p; i--) {
      long v = get(i);
      h = 31 * h + (int) (v ^ (v >>> 32));
    }
    return h;
  }

  @Override
  public boolean equals(Object ob) {
    if (this == ob) return true;
    if (!(ob instanceof LongBuffer)) return false;
    LongBuffer that = (LongBuffer) ob;
    if (this.remaining() != that.remaining()) return false;
    int p = this.position();
    for (int i = this.limit() - 1, j = that.limit() - 1; i >= p; i--, j--) {
      if (this.get(i) != that.get(j)) return false;
    }
    return true;
  }

  @Override
  public int compareTo(LongBuffer that) {
    int n = this.position() + Math.min(this.remaining(), that.remaining());
    for (int i = this.position(), j = that.position(); i < n; i++, j++) {
      int cmp = Long.compare(this.get(i), that.get(j));
      if (cmp != 0) return cmp;
    }
    return this.remaining() - that.remaining();
  }

  public int mismatch(LongBuffer that) {
    int length = Math.min(this.remaining(), that.remaining());
    int pThis = this.position();
    int pThat = that.position();
    for (int i = 0; i < length; i++) {
      if (this.get(pThis + i) != that.get(pThat + i)) {
        return i;
      }
    }
    return (this.remaining() != that.remaining()) ? length : -1;
  }

  @Override
  public String toString() {
    return getClass().getName() + "[pos=" + position() + " lim=" + limit() + " cap=" + capacity() + "]";
  }

  private static class ArrayLongBuffer extends LongBuffer {
    private final long[] array;
    private final int offset;

    ArrayLongBuffer(long[] array, int offset, int length, boolean readOnly) {
      super(readOnly);
      this.array = array;
      this.offset = offset;
      this.capacity = length;
      this.limit = length;
      this.position = 0;
    }

    @Override
    public boolean hasArray() {
      return true;
    }

    @Override
    public long[] array() {
      if (isReadOnly()) throw new ReadOnlyBufferException();
      return array;
    }

    @Override
    public int arrayOffset() {
      if (isReadOnly()) throw new ReadOnlyBufferException();
      return offset;
    }

    @Override
    public boolean isDirect() {
      return false;
    }

    @Override
    public long get() {
      checkGet(position, 1, false);
      return array[offset + position++];
    }

    @Override
    public LongBuffer put(long l) {
      checkPut(position, 1, false);
      array[offset + position++] = l;
      return this;
    }

    @Override
    public long get(int index) {
      checkGet(index, 1, true);
      return array[offset + index];
    }

    @Override
    public LongBuffer put(int index, long l) {
      checkPut(index, 1, true);
      array[offset + index] = l;
      return this;
    }

    @Override
    public LongBuffer slice() {
      return new ArrayLongBuffer(array, offset + position, remaining(), isReadOnly());
    }

    @Override
    public LongBuffer slice(int index, int length) {
      Objects.checkFromIndexSize(index, length, limit);
      return new ArrayLongBuffer(array, offset + index, length, isReadOnly());
    }

    @Override
    public LongBuffer duplicate() {
      ArrayLongBuffer b = new ArrayLongBuffer(array, offset, capacity, isReadOnly());
      b.limit = this.limit;
      b.position = this.position;
      b.mark = this.mark;
      return b;
    }

    @Override
    public LongBuffer asReadOnlyBuffer() {
      ArrayLongBuffer b = new ArrayLongBuffer(array, offset, capacity, true);
      b.limit = this.limit;
      b.position = this.position;
      b.mark = this.mark;
      return b;
    }

    @Override
    public LongBuffer compact() {
      if (isReadOnly()) throw new ReadOnlyBufferException();
      System.arraycopy(array, offset + position, array, offset, remaining());
      position = remaining();
      limit = capacity;
      mark = -1;
      return this;
    }
  }
}
