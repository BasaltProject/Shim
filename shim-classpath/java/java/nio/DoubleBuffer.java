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

public abstract class DoubleBuffer extends Buffer implements Comparable<DoubleBuffer> {

  private final boolean readOnly;

  protected DoubleBuffer(boolean readOnly) {
    this.readOnly = readOnly;
    this.readonly = readOnly;
  }

  public static DoubleBuffer allocate(int capacity) {
    if (capacity < 0) throw new IllegalArgumentException("capacity < 0: " + capacity);
    return new ArrayDoubleBuffer(new double[capacity], 0, capacity, false);
  }

  public static DoubleBuffer wrap(double[] array, int offset, int length) {
    Objects.checkFromIndexSize(offset, length, array.length);
    return new ArrayDoubleBuffer(array, offset, length, false);
  }

  public static DoubleBuffer wrap(double[] array) {
    return wrap(array, 0, array.length);
  }

  public abstract DoubleBuffer slice();

  public abstract DoubleBuffer slice(int index, int length);

  public abstract DoubleBuffer duplicate();

  public abstract DoubleBuffer asReadOnlyBuffer();

  public abstract double get();

  public abstract DoubleBuffer put(double d);

  public abstract double get(int index);

  public abstract DoubleBuffer put(int index, double d);

  public DoubleBuffer get(double[] dst, int offset, int length) {
    Objects.checkFromIndexSize(offset, length, dst.length);
    checkGet(position, length, false);
    for (int i = 0; i < length; i++) {
      dst[offset + i] = get();
    }
    return this;
  }

  public DoubleBuffer get(double[] dst) {
    return get(dst, 0, dst.length);
  }

  public DoubleBuffer put(DoubleBuffer src) {
    if (src == this) throw new IllegalArgumentException();
    if (readOnly) throw new ReadOnlyBufferException();
    int n = src.remaining();
    checkPut(position, n, false);
    for (int i = 0; i < n; i++) {
      put(src.get());
    }
    return this;
  }

  public DoubleBuffer put(double[] src, int offset, int length) {
    Objects.checkFromIndexSize(offset, length, src.length);
    checkPut(position, length, false);
    for (int i = 0; i < length; i++) {
      put(src[offset + i]);
    }
    return this;
  }

  public final DoubleBuffer put(double[] src) {
    return put(src, 0, src.length);
  }

  public boolean hasArray() {
    return false;
  }

  public double[] array() {
    throw new UnsupportedOperationException();
  }

  public int arrayOffset() {
    throw new UnsupportedOperationException();
  }

  public abstract DoubleBuffer compact();

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
  public DoubleBuffer position(int newPosition) {
    super.position(newPosition);
    return this;
  }

  @Override
  public DoubleBuffer limit(int newLimit) {
    super.limit(newLimit);
    return this;
  }

  @Override
  public DoubleBuffer mark() {
    super.mark();
    return this;
  }

  @Override
  public DoubleBuffer reset() {
    super.reset();
    return this;
  }

  @Override
  public DoubleBuffer clear() {
    super.clear();
    return this;
  }

  @Override
  public DoubleBuffer flip() {
    super.flip();
    return this;
  }

  @Override
  public DoubleBuffer rewind() {
    super.rewind();
    return this;
  }

  @Override
  public int hashCode() {
    int h = 1;
    int p = position();
    for (int i = limit() - 1; i >= p; i--) {
      long v = Double.doubleToLongBits(get(i));
      h = 31 * h + (int) (v ^ (v >>> 32));
    }
    return h;
  }

  @Override
  public boolean equals(Object ob) {
    if (this == ob) return true;
    if (!(ob instanceof DoubleBuffer)) return false;
    DoubleBuffer that = (DoubleBuffer) ob;
    if (this.remaining() != that.remaining()) return false;
    int p = this.position();
    for (int i = this.limit() - 1, j = that.limit() - 1; i >= p; i--, j--) {
      if (Double.compare(this.get(i), that.get(j)) != 0) return false;
    }
    return true;
  }

  @Override
  public int compareTo(DoubleBuffer that) {
    int n = this.position() + Math.min(this.remaining(), that.remaining());
    for (int i = this.position(), j = that.position(); i < n; i++, j++) {
      int cmp = Double.compare(this.get(i), that.get(j));
      if (cmp != 0) return cmp;
    }
    return this.remaining() - that.remaining();
  }

  public int mismatch(DoubleBuffer that) {
    int length = Math.min(this.remaining(), that.remaining());
    int pThis = this.position();
    int pThat = that.position();
    for (int i = 0; i < length; i++) {
      if (Double.compare(this.get(pThis + i), that.get(pThat + i)) != 0) {
        return i;
      }
    }
    return (this.remaining() != that.remaining()) ? length : -1;
  }

  @Override
  public String toString() {
    return getClass().getName() + "[pos=" + position() + " lim=" + limit() + " cap=" + capacity() + "]";
  }

  private static class ArrayDoubleBuffer extends DoubleBuffer {
    private final double[] array;
    private final int offset;

    ArrayDoubleBuffer(double[] array, int offset, int length, boolean readOnly) {
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
    public double[] array() {
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
    public double get() {
      checkGet(position, 1, false);
      return array[offset + position++];
    }

    @Override
    public DoubleBuffer put(double d) {
      checkPut(position, 1, false);
      array[offset + position++] = d;
      return this;
    }

    @Override
    public double get(int index) {
      checkGet(index, 1, true);
      return array[offset + index];
    }

    @Override
    public DoubleBuffer put(int index, double d) {
      checkPut(index, 1, true);
      array[offset + index] = d;
      return this;
    }

    @Override
    public DoubleBuffer slice() {
      return new ArrayDoubleBuffer(array, offset + position, remaining(), isReadOnly());
    }

    @Override
    public DoubleBuffer slice(int index, int length) {
      Objects.checkFromIndexSize(index, length, limit);
      return new ArrayDoubleBuffer(array, offset + index, length, isReadOnly());
    }

    @Override
    public DoubleBuffer duplicate() {
      ArrayDoubleBuffer b = new ArrayDoubleBuffer(array, offset, capacity, isReadOnly());
      b.limit = this.limit;
      b.position = this.position;
      b.mark = this.mark;
      return b;
    }

    @Override
    public DoubleBuffer asReadOnlyBuffer() {
      ArrayDoubleBuffer b = new ArrayDoubleBuffer(array, offset, capacity, true);
      b.limit = this.limit;
      b.position = this.position;
      b.mark = this.mark;
      return b;
    }

    @Override
    public DoubleBuffer compact() {
      if (isReadOnly()) throw new ReadOnlyBufferException();
      System.arraycopy(array, offset + position, array, offset, remaining());
      position = remaining();
      limit = capacity;
      mark = -1;
      return this;
    }
  }
}
