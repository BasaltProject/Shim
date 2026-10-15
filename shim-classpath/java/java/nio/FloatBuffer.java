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

public abstract class FloatBuffer extends Buffer implements Comparable<FloatBuffer> {

  private final boolean readOnly;

  protected FloatBuffer(boolean readOnly) {
    this.readOnly = readOnly;
    this.readonly = readOnly;
  }

  public static FloatBuffer allocate(int capacity) {
    if (capacity < 0) throw new IllegalArgumentException("capacity < 0: " + capacity);
    return new ArrayFloatBuffer(new float[capacity], 0, capacity, false);
  }

  public static FloatBuffer wrap(float[] array, int offset, int length) {
    Objects.checkFromIndexSize(offset, length, array.length);
    return new ArrayFloatBuffer(array, offset, length, false);
  }

  public static FloatBuffer wrap(float[] array) {
    return wrap(array, 0, array.length);
  }

  public abstract FloatBuffer slice();

  public abstract FloatBuffer slice(int index, int length);

  public abstract FloatBuffer duplicate();

  public abstract FloatBuffer asReadOnlyBuffer();

  public abstract float get();

  public abstract FloatBuffer put(float f);

  public abstract float get(int index);

  public abstract FloatBuffer put(int index, float f);

  public FloatBuffer get(float[] dst, int offset, int length) {
    Objects.checkFromIndexSize(offset, length, dst.length);
    checkGet(position, length, false);
    for (int i = 0; i < length; i++) {
      dst[offset + i] = get();
    }
    return this;
  }

  public FloatBuffer get(float[] dst) {
    return get(dst, 0, dst.length);
  }

  public FloatBuffer put(FloatBuffer src) {
    if (src == this) throw new IllegalArgumentException();
    if (readOnly) throw new ReadOnlyBufferException();
    int n = src.remaining();
    checkPut(position, n, false);
    for (int i = 0; i < n; i++) {
      put(src.get());
    }
    return this;
  }

  public FloatBuffer put(float[] src, int offset, int length) {
    Objects.checkFromIndexSize(offset, length, src.length);
    checkPut(position, length, false);
    for (int i = 0; i < length; i++) {
      put(src[offset + i]);
    }
    return this;
  }

  public final FloatBuffer put(float[] src) {
    return put(src, 0, src.length);
  }

  public boolean hasArray() {
    return false;
  }

  public float[] array() {
    throw new UnsupportedOperationException();
  }

  public int arrayOffset() {
    throw new UnsupportedOperationException();
  }

  public abstract FloatBuffer compact();

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
  public FloatBuffer position(int newPosition) {
    super.position(newPosition);
    return this;
  }

  @Override
  public FloatBuffer limit(int newLimit) {
    super.limit(newLimit);
    return this;
  }

  @Override
  public FloatBuffer mark() {
    super.mark();
    return this;
  }

  @Override
  public FloatBuffer reset() {
    super.reset();
    return this;
  }

  @Override
  public FloatBuffer clear() {
    super.clear();
    return this;
  }

  @Override
  public FloatBuffer flip() {
    super.flip();
    return this;
  }

  @Override
  public FloatBuffer rewind() {
    super.rewind();
    return this;
  }

  @Override
  public int hashCode() {
    int h = 1;
    int p = position();
    for (int i = limit() - 1; i >= p; i--) {
      h = 31 * h + Float.floatToIntBits(get(i));
    }
    return h;
  }

  @Override
  public boolean equals(Object ob) {
    if (this == ob) return true;
    if (!(ob instanceof FloatBuffer)) return false;
    FloatBuffer that = (FloatBuffer) ob;
    if (this.remaining() != that.remaining()) return false;
    int p = this.position();
    for (int i = this.limit() - 1, j = that.limit() - 1; i >= p; i--, j--) {
      if (Float.compare(this.get(i), that.get(j)) != 0) return false;
    }
    return true;
  }

  @Override
  public int compareTo(FloatBuffer that) {
    int n = this.position() + Math.min(this.remaining(), that.remaining());
    for (int i = this.position(), j = that.position(); i < n; i++, j++) {
      int cmp = Float.compare(this.get(i), that.get(j));
      if (cmp != 0) return cmp;
    }
    return this.remaining() - that.remaining();
  }

  public int mismatch(FloatBuffer that) {
    int length = Math.min(this.remaining(), that.remaining());
    int pThis = this.position();
    int pThat = that.position();
    for (int i = 0; i < length; i++) {
      if (Float.compare(this.get(pThis + i), that.get(pThat + i)) != 0) {
        return i;
      }
    }
    return (this.remaining() != that.remaining()) ? length : -1;
  }

  @Override
  public String toString() {
    return getClass().getName() + "[pos=" + position() + " lim=" + limit() + " cap=" + capacity() + "]";
  }

  private static class ArrayFloatBuffer extends FloatBuffer {
    private final float[] array;
    private final int offset;

    ArrayFloatBuffer(float[] array, int offset, int length, boolean readOnly) {
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
    public float[] array() {
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
    public float get() {
      checkGet(position, 1, false);
      return array[offset + position++];
    }

    @Override
    public FloatBuffer put(float f) {
      checkPut(position, 1, false);
      array[offset + position++] = f;
      return this;
    }

    @Override
    public float get(int index) {
      checkGet(index, 1, true);
      return array[offset + index];
    }

    @Override
    public FloatBuffer put(int index, float f) {
      checkPut(index, 1, true);
      array[offset + index] = f;
      return this;
    }

    @Override
    public FloatBuffer slice() {
      return new ArrayFloatBuffer(array, offset + position, remaining(), isReadOnly());
    }

    @Override
    public FloatBuffer slice(int index, int length) {
      Objects.checkFromIndexSize(index, length, limit);
      return new ArrayFloatBuffer(array, offset + index, length, isReadOnly());
    }

    @Override
    public FloatBuffer duplicate() {
      ArrayFloatBuffer b = new ArrayFloatBuffer(array, offset, capacity, isReadOnly());
      b.limit = this.limit;
      b.position = this.position;
      b.mark = this.mark;
      return b;
    }

    @Override
    public FloatBuffer asReadOnlyBuffer() {
      ArrayFloatBuffer b = new ArrayFloatBuffer(array, offset, capacity, true);
      b.limit = this.limit;
      b.position = this.position;
      b.mark = this.mark;
      return b;
    }

    @Override
    public FloatBuffer compact() {
      if (isReadOnly()) throw new ReadOnlyBufferException();
      System.arraycopy(array, offset + position, array, offset, remaining());
      position = remaining();
      limit = capacity;
      mark = -1;
      return this;
    }
  }
}
