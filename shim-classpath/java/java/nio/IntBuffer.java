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

public abstract class IntBuffer extends Buffer implements Comparable<IntBuffer> {

  private final boolean readOnly;

  protected IntBuffer(boolean readOnly) {
    this.readOnly = readOnly;
    this.readonly = readOnly;
  }

  public static IntBuffer allocate(int capacity) {
    if (capacity < 0) throw new IllegalArgumentException("capacity < 0: " + capacity);
    return new ArrayIntBuffer(new int[capacity], 0, capacity, false);
  }

  public static IntBuffer wrap(int[] array, int offset, int length) {
    Objects.checkFromIndexSize(offset, length, array.length);
    return new ArrayIntBuffer(array, offset, length, false);
  }

  public static IntBuffer wrap(int[] array) {
    return wrap(array, 0, array.length);
  }

  public abstract IntBuffer slice();

  public abstract IntBuffer slice(int index, int length);

  public abstract IntBuffer duplicate();

  public abstract IntBuffer asReadOnlyBuffer();

  public abstract int get();

  public abstract IntBuffer put(int i);

  public abstract int get(int index);

  public abstract IntBuffer put(int index, int i);

  public IntBuffer get(int[] dst, int offset, int length) {
    Objects.checkFromIndexSize(offset, length, dst.length);
    checkGet(position, length, false);
    for (int i = 0; i < length; i++) {
      dst[offset + i] = get();
    }
    return this;
  }

  public IntBuffer get(int[] dst) {
    return get(dst, 0, dst.length);
  }

  public IntBuffer put(IntBuffer src) {
    if (src == this) throw new IllegalArgumentException();
    if (readOnly) throw new ReadOnlyBufferException();
    int n = src.remaining();
    checkPut(position, n, false);
    for (int i = 0; i < n; i++) {
      put(src.get());
    }
    return this;
  }

  public IntBuffer put(int[] src, int offset, int length) {
    Objects.checkFromIndexSize(offset, length, src.length);
    checkPut(position, length, false);
    for (int i = 0; i < length; i++) {
      put(src[offset + i]);
    }
    return this;
  }

  public final IntBuffer put(int[] src) {
    return put(src, 0, src.length);
  }

  public boolean hasArray() {
    return false;
  }

  public int[] array() {
    throw new UnsupportedOperationException();
  }

  public int arrayOffset() {
    throw new UnsupportedOperationException();
  }

  public abstract IntBuffer compact();

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
  public IntBuffer position(int newPosition) {
    super.position(newPosition);
    return this;
  }

  @Override
  public IntBuffer limit(int newLimit) {
    super.limit(newLimit);
    return this;
  }

  @Override
  public IntBuffer mark() {
    super.mark();
    return this;
  }

  @Override
  public IntBuffer reset() {
    super.reset();
    return this;
  }

  @Override
  public IntBuffer clear() {
    super.clear();
    return this;
  }

  @Override
  public IntBuffer flip() {
    super.flip();
    return this;
  }

  @Override
  public IntBuffer rewind() {
    super.rewind();
    return this;
  }

  @Override
  public int hashCode() {
    int h = 1;
    int p = position();
    for (int i = limit() - 1; i >= p; i--) {
      h = 31 * h + get(i);
    }
    return h;
  }

  @Override
  public boolean equals(Object ob) {
    if (this == ob) return true;
    if (!(ob instanceof IntBuffer)) return false;
    IntBuffer that = (IntBuffer) ob;
    if (this.remaining() != that.remaining()) return false;
    int p = this.position();
    for (int i = this.limit() - 1, j = that.limit() - 1; i >= p; i--, j--) {
      if (this.get(i) != that.get(j)) return false;
    }
    return true;
  }

  @Override
  public int compareTo(IntBuffer that) {
    int n = this.position() + Math.min(this.remaining(), that.remaining());
    for (int i = this.position(), j = that.position(); i < n; i++, j++) {
      int cmp = Integer.compare(this.get(i), that.get(j));
      if (cmp != 0) return cmp;
    }
    return this.remaining() - that.remaining();
  }

  public int mismatch(IntBuffer that) {
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

  private static class ArrayIntBuffer extends IntBuffer {
    private final int[] array;
    private final int offset;

    ArrayIntBuffer(int[] array, int offset, int length, boolean readOnly) {
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
    public int[] array() {
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
    public int get() {
      checkGet(position, 1, false);
      return array[offset + position++];
    }

    @Override
    public IntBuffer put(int i) {
      checkPut(position, 1, false);
      array[offset + position++] = i;
      return this;
    }

    @Override
    public int get(int index) {
      checkGet(index, 1, true);
      return array[offset + index];
    }

    @Override
    public IntBuffer put(int index, int i) {
      checkPut(index, 1, true);
      array[offset + index] = i;
      return this;
    }

    @Override
    public IntBuffer slice() {
      return new ArrayIntBuffer(array, offset + position, remaining(), isReadOnly());
    }

    @Override
    public IntBuffer slice(int index, int length) {
      Objects.checkFromIndexSize(index, length, limit);
      return new ArrayIntBuffer(array, offset + index, length, isReadOnly());
    }

    @Override
    public IntBuffer duplicate() {
      ArrayIntBuffer b = new ArrayIntBuffer(array, offset, capacity, isReadOnly());
      b.limit = this.limit;
      b.position = this.position;
      b.mark = this.mark;
      return b;
    }

    @Override
    public IntBuffer asReadOnlyBuffer() {
      ArrayIntBuffer b = new ArrayIntBuffer(array, offset, capacity, true);
      b.limit = this.limit;
      b.position = this.position;
      b.mark = this.mark;
      return b;
    }

    @Override
    public IntBuffer compact() {
      if (isReadOnly()) throw new ReadOnlyBufferException();
      System.arraycopy(array, offset + position, array, offset, remaining());
      position = remaining();
      limit = capacity;
      mark = -1;
      return this;
    }
  }
}
