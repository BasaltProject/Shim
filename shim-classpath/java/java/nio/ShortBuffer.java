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

public abstract class ShortBuffer extends Buffer implements Comparable<ShortBuffer> {

  private final boolean readOnly;

  protected ShortBuffer(boolean readOnly) {
    this.readOnly = readOnly;
    this.readonly = readOnly;
  }

  public static ShortBuffer allocate(int capacity) {
    if (capacity < 0) throw new IllegalArgumentException("capacity < 0: " + capacity);
    return new ArrayShortBuffer(new short[capacity], 0, capacity, false);
  }

  public static ShortBuffer wrap(short[] array, int offset, int length) {
    Objects.checkFromIndexSize(offset, length, array.length);
    return new ArrayShortBuffer(array, offset, length, false);
  }

  public static ShortBuffer wrap(short[] array) {
    return wrap(array, 0, array.length);
  }

  public abstract ShortBuffer slice();

  public abstract ShortBuffer slice(int index, int length);

  public abstract ShortBuffer duplicate();

  public abstract ShortBuffer asReadOnlyBuffer();

  public abstract short get();

  public abstract ShortBuffer put(short s);

  public abstract short get(int index);

  public abstract ShortBuffer put(int index, short s);

  public ShortBuffer get(short[] dst, int offset, int length) {
    Objects.checkFromIndexSize(offset, length, dst.length);
    checkGet(position, length, false);
    for (int i = 0; i < length; i++) {
      dst[offset + i] = get();
    }
    return this;
  }

  public ShortBuffer get(short[] dst) {
    return get(dst, 0, dst.length);
  }

  public ShortBuffer put(ShortBuffer src) {
    if (src == this) throw new IllegalArgumentException();
    if (readOnly) throw new ReadOnlyBufferException();
    int n = src.remaining();
    checkPut(position, n, false);
    for (int i = 0; i < n; i++) {
      put(src.get());
    }
    return this;
  }

  public ShortBuffer put(short[] src, int offset, int length) {
    Objects.checkFromIndexSize(offset, length, src.length);
    checkPut(position, length, false);
    for (int i = 0; i < length; i++) {
      put(src[offset + i]);
    }
    return this;
  }

  public final ShortBuffer put(short[] src) {
    return put(src, 0, src.length);
  }

  public boolean hasArray() {
    return false;
  }

  public short[] array() {
    throw new UnsupportedOperationException();
  }

  public int arrayOffset() {
    throw new UnsupportedOperationException();
  }

  public abstract ShortBuffer compact();

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
  public ShortBuffer position(int newPosition) {
    super.position(newPosition);
    return this;
  }

  @Override
  public ShortBuffer limit(int newLimit) {
    super.limit(newLimit);
    return this;
  }

  @Override
  public ShortBuffer mark() {
    super.mark();
    return this;
  }

  @Override
  public ShortBuffer reset() {
    super.reset();
    return this;
  }

  @Override
  public ShortBuffer clear() {
    super.clear();
    return this;
  }

  @Override
  public ShortBuffer flip() {
    super.flip();
    return this;
  }

  @Override
  public ShortBuffer rewind() {
    super.rewind();
    return this;
  }

  @Override
  public int hashCode() {
    int h = 1;
    int p = position();
    for (int i = limit() - 1; i >= p; i--) {
      h = 31 * h + (int) get(i);
    }
    return h;
  }

  @Override
  public boolean equals(Object ob) {
    if (this == ob) return true;
    if (!(ob instanceof ShortBuffer)) return false;
    ShortBuffer that = (ShortBuffer) ob;
    if (this.remaining() != that.remaining()) return false;
    int p = this.position();
    for (int i = this.limit() - 1, j = that.limit() - 1; i >= p; i--, j--) {
      if (this.get(i) != that.get(j)) return false;
    }
    return true;
  }

  @Override
  public int compareTo(ShortBuffer that) {
    int n = this.position() + Math.min(this.remaining(), that.remaining());
    for (int i = this.position(), j = that.position(); i < n; i++, j++) {
      int cmp = Short.compare(this.get(i), that.get(j));
      if (cmp != 0) return cmp;
    }
    return this.remaining() - that.remaining();
  }

  public int mismatch(ShortBuffer that) {
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

  private static class ArrayShortBuffer extends ShortBuffer {
    private final short[] array;
    private final int offset;

    ArrayShortBuffer(short[] array, int offset, int length, boolean readOnly) {
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
    public short[] array() {
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
    public short get() {
      checkGet(position, 1, false);
      return array[offset + position++];
    }

    @Override
    public ShortBuffer put(short s) {
      checkPut(position, 1, false);
      array[offset + position++] = s;
      return this;
    }

    @Override
    public short get(int index) {
      checkGet(index, 1, true);
      return array[offset + index];
    }

    @Override
    public ShortBuffer put(int index, short s) {
      checkPut(index, 1, true);
      array[offset + index] = s;
      return this;
    }

    @Override
    public ShortBuffer slice() {
      return new ArrayShortBuffer(array, offset + position, remaining(), isReadOnly());
    }

    @Override
    public ShortBuffer slice(int index, int length) {
      Objects.checkFromIndexSize(index, length, limit);
      return new ArrayShortBuffer(array, offset + index, length, isReadOnly());
    }

    @Override
    public ShortBuffer duplicate() {
      ArrayShortBuffer b = new ArrayShortBuffer(array, offset, capacity, isReadOnly());
      b.limit = this.limit;
      b.position = this.position;
      b.mark = this.mark;
      return b;
    }

    @Override
    public ShortBuffer asReadOnlyBuffer() {
      ArrayShortBuffer b = new ArrayShortBuffer(array, offset, capacity, true);
      b.limit = this.limit;
      b.position = this.position;
      b.mark = this.mark;
      return b;
    }

    @Override
    public ShortBuffer compact() {
      if (isReadOnly()) throw new ReadOnlyBufferException();
      System.arraycopy(array, offset + position, array, offset, remaining());
      position = remaining();
      limit = capacity;
      mark = -1;
      return this;
    }
  }
}
