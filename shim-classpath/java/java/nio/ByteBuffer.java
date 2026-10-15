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

/* Copyright (c) 2008-2015, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.nio;

import java.util.Objects;

public abstract class ByteBuffer
  extends Buffer
  implements Comparable<ByteBuffer>
{

  protected ByteBuffer(boolean readOnly) {
    this.readonly = readOnly;
  }

  public static ByteBuffer allocate(int capacity) {
    return new ArrayByteBuffer(new byte[capacity], 0, capacity, false);
  }

  public static ByteBuffer allocateDirect(int capacity) {
    return FixedArrayByteBuffer.make(capacity);
  }

  public static ByteBuffer wrap(byte[] array) {
    return wrap(array, 0, array.length);
  }

  public static ByteBuffer wrap(byte[] array, int offset, int length) {
    return new ArrayByteBuffer(array, offset, length, false);
  }

  public abstract ByteBuffer asReadOnlyBuffer();

  public abstract ByteBuffer slice();

  public abstract ByteBuffer slice(int index, int length);

  public abstract ByteBuffer duplicate();

  public abstract boolean isDirect();

  protected abstract void doPut(int offset, byte val);

  public abstract ByteBuffer put(byte[] arr, int offset, int len);

  protected abstract byte doGet(int offset);

  public abstract ByteBuffer get(byte[] dst, int offset, int length);

  public boolean hasArray() {
    return false;
  }

  public ByteBuffer compact() {
    int remaining = remaining();

    if (position != 0) {
      ByteBuffer b = slice();
      position = 0;
      put(b);
    }

    position = remaining;
    limit(capacity());
    
    return this;
  }

  public ByteBuffer put(ByteBuffer src) {
    if (src.hasArray()) {
      checkPut(position, src.remaining(), false);

      put(src.array(), src.arrayOffset() + src.position, src.remaining());
      src.position(src.position() + src.remaining());

      return this;
    } else {
      byte[] buffer = new byte[src.remaining()];
      src.get(buffer);
      return put(buffer);
    }
  }

  public int compareTo(ByteBuffer o) {
    int end = (remaining() < o.remaining() ? remaining() : o.remaining());

    for (int i = 0; i < end; ++i) {
      int d = get(position + i) - o.get(o.position + i);
      if (d != 0) {
        return d;
      }
    }
    return remaining() - o.remaining();
  }

  public boolean equals(Object o) {
    return o instanceof ByteBuffer && compareTo((ByteBuffer) o) == 0;
  }

  public byte[] array() {
    throw new UnsupportedOperationException();
  }

  public int arrayOffset() {
    throw new UnsupportedOperationException();
  }

  public ByteBuffer put(int offset, byte val) {
    checkPut(offset, 1, true);
    doPut(offset, val);
    return this;
  }

  public ByteBuffer put(byte val) {
    checkPut(position, 1, false);
    doPut(position, val);
    ++ position;
    return this;
  }

  public ByteBuffer put(byte[] arr) {
    return put(arr, 0, arr.length);
  }
  
  private void rawPutLong(int position, long val) {
    doPut(position    , (byte) ((val >> 56) & 0xff));
    doPut(position + 1, (byte) ((val >> 48) & 0xff));
    doPut(position + 2, (byte) ((val >> 40) & 0xff));
    doPut(position + 3, (byte) ((val >> 32) & 0xff));
    doPut(position + 4, (byte) ((val >> 24) & 0xff));
    doPut(position + 5, (byte) ((val >> 16) & 0xff));
    doPut(position + 6, (byte) ((val >>  8) & 0xff));
    doPut(position + 7, (byte) ((val      ) & 0xff));
  }

  private void rawPutInt(int position, int val) {
    doPut(position    , (byte) ((val >> 24) & 0xff));
    doPut(position + 1, (byte) ((val >> 16) & 0xff));
    doPut(position + 2, (byte) ((val >>  8) & 0xff));
    doPut(position + 3, (byte) ((val      ) & 0xff));
  }

  private void rawPutShort(int position, short val) {
    doPut(position    , (byte) ((val >> 8) & 0xff));
    doPut(position + 1, (byte) ((val     ) & 0xff));
  }
  
  public ByteBuffer putDouble(int position, double val) {
    return putLong(position, Double.doubleToRawLongBits(val));
  }
  
  public ByteBuffer putFloat(int position, float val) {
    return putInt(position, Float.floatToRawIntBits(val));
  }

  public ByteBuffer putLong(int position, long val) {
    checkPut(position, 8, true);

    rawPutLong(position, val);

    return this;
  }

  public ByteBuffer putInt(int position, int val) {
    checkPut(position, 4, true);

    rawPutInt(position, val);

    return this;
  }

  public ByteBuffer putShort(int position, short val) {
    checkPut(position, 2, true);

    rawPutShort(position, val);

    return this;
  }
  
  public ByteBuffer putDouble(double val) {
    return putLong(Double.doubleToRawLongBits(val));
  }
  
  public ByteBuffer putFloat(float val) {
    return putInt(Float.floatToRawIntBits(val));
  }

  public ByteBuffer putLong(long val) {
    checkPut(position, 8, false);

    rawPutLong(position, val);
    position += 8;
    return this;
  }

  public ByteBuffer putInt(int val) {
    checkPut(position, 4, false);

    rawPutInt(position, val);
    position += 4;
    return this;
  }

  public ByteBuffer putShort(short val) {
    checkPut(position, 2, false);

    rawPutShort(position, val);
    position += 2;
    return this;
  }

  public byte get() {
    checkGet(position, 1, false);
    return doGet(position++);
  }

  public byte get(int position) {
    checkGet(position, 1, true);
    return doGet(position);
  }

  public ByteBuffer get(byte[] dst) {
    return get(dst, 0, dst.length);
  }
  
  public double getDouble(int position) {
    return Double.longBitsToDouble(getLong(position));
  }
  
  public float getFloat(int position) {
    return Float.intBitsToFloat(getInt(position));
  }

  public long getLong(int position) {
    checkGet(position, 8, true);

    return rawGetLong(position);
  }

  public int getInt(int position) {
    checkGet(position, 4, true);

    return rawGetInt(position);
  }

  public short getShort(int position) {
    checkGet(position, 2, true);

    return rawGetShort(position);
  }

  private long rawGetLong(int position) {
    return (((long) (doGet(position    ) & 0xFF)) << 56)
      |    (((long) (doGet(position + 1) & 0xFF)) << 48)
      |    (((long) (doGet(position + 2) & 0xFF)) << 40)
      |    (((long) (doGet(position + 3) & 0xFF)) << 32)
      |    (((long) (doGet(position + 4) & 0xFF)) << 24)
      |    (((long) (doGet(position + 5) & 0xFF)) << 16)
      |    (((long) (doGet(position + 6) & 0xFF)) <<  8)
      |    (((long) (doGet(position + 7) & 0xFF))      );
  }

  private int rawGetInt(int position) {
    return (((int) (doGet(position    ) & 0xFF)) << 24)
      |    (((int) (doGet(position + 1) & 0xFF)) << 16)
      |    (((int) (doGet(position + 2) & 0xFF)) <<  8)
      |    (((int) (doGet(position + 3) & 0xFF))      );
  }

  private short rawGetShort(int position) {
    return (short) ((  ((int) (doGet(position    ) & 0xFF)) << 8)
                    | (((int) (doGet(position + 1) & 0xFF))     ));
  }
  
  public double getDouble() {
    return Double.longBitsToDouble(getLong());
  }
  
  public float getFloat() {
    return Float.intBitsToFloat(getInt());
  }
  
  public long getLong() {
    checkGet(position, 8, false);

    long r = rawGetLong(position);
    position += 8;
    return r;
  }

  public int getInt() {
    checkGet(position, 4, false);

    int r = rawGetInt(position);
    position += 4;
    return r;
  }

  public short getShort() {
    checkGet(position, 2, false);

    short r = rawGetShort(position);
    position += 2;
    return r;
  }

  protected void checkPut(int position, int amount, boolean absolute) {
    if (isReadOnly()) {
      throw new ReadOnlyBufferException();
    }

    if (position < 0 || position+amount > limit) {
      throw absolute
        ? new IndexOutOfBoundsException()
        : new BufferOverflowException();
    }
  }

  protected void checkGet(int position, int amount, boolean absolute) {
    if (amount > limit-position) {
      throw absolute
        ? new IndexOutOfBoundsException()
        : new BufferUnderflowException();
    }
  }

  public ByteBuffer order(ByteOrder order) {
    if (order != ByteOrder.BIG_ENDIAN) throw new UnsupportedOperationException();
    return this;
  }

  public ByteOrder order() {
    return ByteOrder.BIG_ENDIAN;
  }

  @Override
  public ByteBuffer position(int newPosition) {
    super.position(newPosition);
    return this;
  }

  @Override
  public ByteBuffer limit(int newLimit) {
    super.limit(newLimit);
    return this;
  }

  @Override
  public ByteBuffer mark() {
    super.mark();
    return this;
  }

  @Override
  public ByteBuffer reset() {
    super.reset();
    return this;
  }

  @Override
  public ByteBuffer clear() {
    super.clear();
    return this;
  }

  @Override
  public ByteBuffer flip() {
    super.flip();
    return this;
  }

  @Override
  public ByteBuffer rewind() {
    super.rewind();
    return this;
  }

  public int mismatch(ByteBuffer that) {
    int length = Math.min(this.remaining(), that.remaining());
    int p1 = this.position();
    int p2 = that.position();
    for (int i = 0; i < length; ++i) {
      if (this.get(p1 + i) != that.get(p2 + i)) {
        return i;
      }
    }
    return this.remaining() != that.remaining() ? length : -1;
  }

  public ByteBuffer get(int index, byte[] dst, int offset, int length) {
    java.util.Objects.checkFromIndexSize(index, length, limit());
    java.util.Objects.checkFromIndexSize(offset, length, dst.length);
    for (int i = 0; i < length; ++i) {
      dst[offset + i] = get(index + i);
    }
    return this;
  }

  public ByteBuffer get(int index, byte[] dst) {
    return get(index, dst, 0, dst.length);
  }

  public ByteBuffer put(int index, byte[] src, int offset, int length) {
    if (isReadOnly()) throw new ReadOnlyBufferException();
    java.util.Objects.checkFromIndexSize(index, length, limit());
    java.util.Objects.checkFromIndexSize(offset, length, src.length);
    for (int i = 0; i < length; ++i) {
      put(index + i, src[offset + i]);
    }
    return this;
  }

  public ByteBuffer put(int index, byte[] src) {
    return put(index, src, 0, src.length);
  }

  public ByteBuffer put(int index, ByteBuffer src, int offset, int length) {
    if (isReadOnly()) throw new ReadOnlyBufferException();
    java.util.Objects.checkFromIndexSize(index, length, limit());
    java.util.Objects.checkFromIndexSize(offset, length, src.limit());
    for (int i = 0; i < length; ++i) {
      put(index + i, src.get(offset + i));
    }
    return this;
  }

  public char getChar() {
    return (char) getShort();
  }

  public char getChar(int index) {
    return (char) getShort(index);
  }

  public ByteBuffer putChar(char value) {
    return putShort((short) value);
  }

  public ByteBuffer putChar(int index, char value) {
    return putShort(index, (short) value);
  }

  public final int alignmentOffset(int index, int unitSize) {
    if (index < 0) throw new IndexOutOfBoundsException("index < 0: " + index);
    if (unitSize <= 0 || (unitSize & (unitSize - 1)) != 0) {
      throw new IllegalArgumentException("unitSize not a power of 2: " + unitSize);
    }
    long base = (isDirect() && (this instanceof DirectByteBuffer))
      ? ((DirectByteBuffer) this).address() : 0;
    return (int) ((base + index) & (unitSize - 1));
  }

  public IntBuffer asIntBuffer() {
    int cap = remaining() / 4;
    return new ByteBufferAsIntBuffer(this, position, cap, readonly);
  }

  public LongBuffer asLongBuffer() {
    int cap = remaining() / 8;
    return new ByteBufferAsLongBuffer(this, position, cap, readonly);
  }

  public ShortBuffer asShortBuffer() {
    int cap = remaining() / 2;
    return new ByteBufferAsShortBuffer(this, position, cap, readonly);
  }

  public FloatBuffer asFloatBuffer() {
    int cap = remaining() / 4;
    return new ByteBufferAsFloatBuffer(this, position, cap, readonly);
  }

  public DoubleBuffer asDoubleBuffer() {
    int cap = remaining() / 8;
    return new ByteBufferAsDoubleBuffer(this, position, cap, readonly);
  }

  public CharBuffer asCharBuffer() {
    int cap = remaining() / 2;
    return new ByteBufferAsCharBuffer(this, position, cap, readonly);
  }

  private static class ByteBufferAsIntBuffer extends IntBuffer {
    private final ByteBuffer bb;
    private final int byteOffset;

    ByteBufferAsIntBuffer(ByteBuffer bb, int byteOffset, int capacity, boolean readOnly) {
      super(readOnly);
      this.bb = bb;
      this.byteOffset = byteOffset;
      this.capacity = capacity;
      this.limit = capacity;
      this.position = 0;
    }

    @Override
    public int get() {
      checkGet(position, 1, false);
      return bb.getInt(byteOffset + position++ * 4);
    }

    @Override
    public IntBuffer put(int i) {
      checkPut(position, 1, false);
      bb.putInt(byteOffset + position++ * 4, i);
      return this;
    }

    @Override
    public int get(int index) {
      checkGet(index, 1, true);
      return bb.getInt(byteOffset + index * 4);
    }

    @Override
    public IntBuffer put(int index, int i) {
      checkPut(index, 1, true);
      bb.putInt(byteOffset + index * 4, i);
      return this;
    }

    @Override
    public IntBuffer slice() {
      return new ByteBufferAsIntBuffer(bb, byteOffset + position * 4, remaining(), isReadOnly());
    }

    @Override
    public IntBuffer slice(int index, int length) {
      Objects.checkFromIndexSize(index, length, limit);
      return new ByteBufferAsIntBuffer(bb, byteOffset + index * 4, length, isReadOnly());
    }

    @Override
    public IntBuffer duplicate() {
      ByteBufferAsIntBuffer b = new ByteBufferAsIntBuffer(bb, byteOffset, capacity, isReadOnly());
      b.limit = limit;
      b.position = position;
      b.mark = mark;
      return b;
    }

    @Override
    public IntBuffer asReadOnlyBuffer() {
      ByteBufferAsIntBuffer b = new ByteBufferAsIntBuffer(bb.asReadOnlyBuffer(), byteOffset, capacity, true);
      b.limit = limit;
      b.position = position;
      b.mark = mark;
      return b;
    }

    @Override
    public IntBuffer compact() {
      if (isReadOnly()) throw new ReadOnlyBufferException();
      int rem = remaining();
      for (int i = 0; i < rem; i++) {
        put(i, get(position + i));
      }
      position = rem;
      limit = capacity;
      mark = -1;
      return this;
    }

    @Override
    public boolean isDirect() {
      return bb.isDirect();
    }

    @Override
    public ByteOrder order() {
      return bb.order();
    }
  }

  private static class ByteBufferAsLongBuffer extends LongBuffer {
    private final ByteBuffer bb;
    private final int byteOffset;

    ByteBufferAsLongBuffer(ByteBuffer bb, int byteOffset, int capacity, boolean readOnly) {
      super(readOnly);
      this.bb = bb;
      this.byteOffset = byteOffset;
      this.capacity = capacity;
      this.limit = capacity;
      this.position = 0;
    }

    @Override
    public long get() {
      checkGet(position, 1, false);
      return bb.getLong(byteOffset + position++ * 8);
    }

    @Override
    public LongBuffer put(long l) {
      checkPut(position, 1, false);
      bb.putLong(byteOffset + position++ * 8, l);
      return this;
    }

    @Override
    public long get(int index) {
      checkGet(index, 1, true);
      return bb.getLong(byteOffset + index * 8);
    }

    @Override
    public LongBuffer put(int index, long l) {
      checkPut(index, 1, true);
      bb.putLong(byteOffset + index * 8, l);
      return this;
    }

    @Override
    public LongBuffer slice() {
      return new ByteBufferAsLongBuffer(bb, byteOffset + position * 8, remaining(), isReadOnly());
    }

    @Override
    public LongBuffer slice(int index, int length) {
      Objects.checkFromIndexSize(index, length, limit);
      return new ByteBufferAsLongBuffer(bb, byteOffset + index * 8, length, isReadOnly());
    }

    @Override
    public LongBuffer duplicate() {
      ByteBufferAsLongBuffer b = new ByteBufferAsLongBuffer(bb, byteOffset, capacity, isReadOnly());
      b.limit = limit;
      b.position = position;
      b.mark = mark;
      return b;
    }

    @Override
    public LongBuffer asReadOnlyBuffer() {
      ByteBufferAsLongBuffer b = new ByteBufferAsLongBuffer(bb.asReadOnlyBuffer(), byteOffset, capacity, true);
      b.limit = limit;
      b.position = position;
      b.mark = mark;
      return b;
    }

    @Override
    public LongBuffer compact() {
      if (isReadOnly()) throw new ReadOnlyBufferException();
      int rem = remaining();
      for (int i = 0; i < rem; i++) {
        put(i, get(position + i));
      }
      position = rem;
      limit = capacity;
      mark = -1;
      return this;
    }

    @Override
    public boolean isDirect() {
      return bb.isDirect();
    }

    @Override
    public ByteOrder order() {
      return bb.order();
    }
  }

  private static class ByteBufferAsShortBuffer extends ShortBuffer {
    private final ByteBuffer bb;
    private final int byteOffset;

    ByteBufferAsShortBuffer(ByteBuffer bb, int byteOffset, int capacity, boolean readOnly) {
      super(readOnly);
      this.bb = bb;
      this.byteOffset = byteOffset;
      this.capacity = capacity;
      this.limit = capacity;
      this.position = 0;
    }

    @Override
    public short get() {
      checkGet(position, 1, false);
      return bb.getShort(byteOffset + position++ * 2);
    }

    @Override
    public ShortBuffer put(short s) {
      checkPut(position, 1, false);
      bb.putShort(byteOffset + position++ * 2, s);
      return this;
    }

    @Override
    public short get(int index) {
      checkGet(index, 1, true);
      return bb.getShort(byteOffset + index * 2);
    }

    @Override
    public ShortBuffer put(int index, short s) {
      checkPut(index, 1, true);
      bb.putShort(byteOffset + index * 2, s);
      return this;
    }

    @Override
    public ShortBuffer slice() {
      return new ByteBufferAsShortBuffer(bb, byteOffset + position * 2, remaining(), isReadOnly());
    }

    @Override
    public ShortBuffer slice(int index, int length) {
      Objects.checkFromIndexSize(index, length, limit);
      return new ByteBufferAsShortBuffer(bb, byteOffset + index * 2, length, isReadOnly());
    }

    @Override
    public ShortBuffer duplicate() {
      ByteBufferAsShortBuffer b = new ByteBufferAsShortBuffer(bb, byteOffset, capacity, isReadOnly());
      b.limit = limit;
      b.position = position;
      b.mark = mark;
      return b;
    }

    @Override
    public ShortBuffer asReadOnlyBuffer() {
      ByteBufferAsShortBuffer b = new ByteBufferAsShortBuffer(bb.asReadOnlyBuffer(), byteOffset, capacity, true);
      b.limit = limit;
      b.position = position;
      b.mark = mark;
      return b;
    }

    @Override
    public ShortBuffer compact() {
      if (isReadOnly()) throw new ReadOnlyBufferException();
      int rem = remaining();
      for (int i = 0; i < rem; i++) {
        put(i, get(position + i));
      }
      position = rem;
      limit = capacity;
      mark = -1;
      return this;
    }

    @Override
    public boolean isDirect() {
      return bb.isDirect();
    }

    @Override
    public ByteOrder order() {
      return bb.order();
    }
  }

  private static class ByteBufferAsFloatBuffer extends FloatBuffer {
    private final ByteBuffer bb;
    private final int byteOffset;

    ByteBufferAsFloatBuffer(ByteBuffer bb, int byteOffset, int capacity, boolean readOnly) {
      super(readOnly);
      this.bb = bb;
      this.byteOffset = byteOffset;
      this.capacity = capacity;
      this.limit = capacity;
      this.position = 0;
    }

    @Override
    public float get() {
      checkGet(position, 1, false);
      return bb.getFloat(byteOffset + position++ * 4);
    }

    @Override
    public FloatBuffer put(float f) {
      checkPut(position, 1, false);
      bb.putFloat(byteOffset + position++ * 4, f);
      return this;
    }

    @Override
    public float get(int index) {
      checkGet(index, 1, true);
      return bb.getFloat(byteOffset + index * 4);
    }

    @Override
    public FloatBuffer put(int index, float f) {
      checkPut(index, 1, true);
      bb.putFloat(byteOffset + index * 4, f);
      return this;
    }

    @Override
    public FloatBuffer slice() {
      return new ByteBufferAsFloatBuffer(bb, byteOffset + position * 4, remaining(), isReadOnly());
    }

    @Override
    public FloatBuffer slice(int index, int length) {
      Objects.checkFromIndexSize(index, length, limit);
      return new ByteBufferAsFloatBuffer(bb, byteOffset + index * 4, length, isReadOnly());
    }

    @Override
    public FloatBuffer duplicate() {
      ByteBufferAsFloatBuffer b = new ByteBufferAsFloatBuffer(bb, byteOffset, capacity, isReadOnly());
      b.limit = limit;
      b.position = position;
      b.mark = mark;
      return b;
    }

    @Override
    public FloatBuffer asReadOnlyBuffer() {
      ByteBufferAsFloatBuffer b = new ByteBufferAsFloatBuffer(bb.asReadOnlyBuffer(), byteOffset, capacity, true);
      b.limit = limit;
      b.position = position;
      b.mark = mark;
      return b;
    }

    @Override
    public FloatBuffer compact() {
      if (isReadOnly()) throw new ReadOnlyBufferException();
      int rem = remaining();
      for (int i = 0; i < rem; i++) {
        put(i, get(position + i));
      }
      position = rem;
      limit = capacity;
      mark = -1;
      return this;
    }

    @Override
    public boolean isDirect() {
      return bb.isDirect();
    }

    @Override
    public ByteOrder order() {
      return bb.order();
    }
  }

  private static class ByteBufferAsDoubleBuffer extends DoubleBuffer {
    private final ByteBuffer bb;
    private final int byteOffset;

    ByteBufferAsDoubleBuffer(ByteBuffer bb, int byteOffset, int capacity, boolean readOnly) {
      super(readOnly);
      this.bb = bb;
      this.byteOffset = byteOffset;
      this.capacity = capacity;
      this.limit = capacity;
      this.position = 0;
    }

    @Override
    public double get() {
      checkGet(position, 1, false);
      return bb.getDouble(byteOffset + position++ * 8);
    }

    @Override
    public DoubleBuffer put(double d) {
      checkPut(position, 1, false);
      bb.putDouble(byteOffset + position++ * 8, d);
      return this;
    }

    @Override
    public double get(int index) {
      checkGet(index, 1, true);
      return bb.getDouble(byteOffset + index * 8);
    }

    @Override
    public DoubleBuffer put(int index, double d) {
      checkPut(index, 1, true);
      bb.putDouble(byteOffset + index * 8, d);
      return this;
    }

    @Override
    public DoubleBuffer slice() {
      return new ByteBufferAsDoubleBuffer(bb, byteOffset + position * 8, remaining(), isReadOnly());
    }

    @Override
    public DoubleBuffer slice(int index, int length) {
      Objects.checkFromIndexSize(index, length, limit);
      return new ByteBufferAsDoubleBuffer(bb, byteOffset + index * 8, length, isReadOnly());
    }

    @Override
    public DoubleBuffer duplicate() {
      ByteBufferAsDoubleBuffer b = new ByteBufferAsDoubleBuffer(bb, byteOffset, capacity, isReadOnly());
      b.limit = limit;
      b.position = position;
      b.mark = mark;
      return b;
    }

    @Override
    public DoubleBuffer asReadOnlyBuffer() {
      ByteBufferAsDoubleBuffer b = new ByteBufferAsDoubleBuffer(bb.asReadOnlyBuffer(), byteOffset, capacity, true);
      b.limit = limit;
      b.position = position;
      b.mark = mark;
      return b;
    }

    @Override
    public DoubleBuffer compact() {
      if (isReadOnly()) throw new ReadOnlyBufferException();
      int rem = remaining();
      for (int i = 0; i < rem; i++) {
        put(i, get(position + i));
      }
      position = rem;
      limit = capacity;
      mark = -1;
      return this;
    }

    @Override
    public boolean isDirect() {
      return bb.isDirect();
    }

    @Override
    public ByteOrder order() {
      return bb.order();
    }
  }

  private static class ByteBufferAsCharBuffer extends CharBuffer {
    private final ByteBuffer bb;
    private final int byteOffset;

    ByteBufferAsCharBuffer(ByteBuffer bb, int byteOffset, int capacity, boolean readOnly) {
      super(readOnly);
      this.bb = bb;
      this.byteOffset = byteOffset;
      this.capacity = capacity;
      this.limit = capacity;
      this.position = 0;
    }

    @Override
    public CharBuffer asReadOnlyBuffer() {
      ByteBufferAsCharBuffer b = new ByteBufferAsCharBuffer(bb.asReadOnlyBuffer(), byteOffset, capacity, true);
      b.limit = limit;
      b.position = position;
      b.mark = mark;
      return b;
    }

    @Override
    public CharBuffer slice() {
      return new ByteBufferAsCharBuffer(bb, byteOffset + position * 2, remaining(), isReadOnly());
    }

    @Override
    public CharBuffer slice(int index, int length) {
      Objects.checkFromIndexSize(index, length, limit);
      return new ByteBufferAsCharBuffer(bb, byteOffset + index * 2, length, isReadOnly());
    }

    @Override
    public CharBuffer duplicate() {
      ByteBufferAsCharBuffer b = new ByteBufferAsCharBuffer(bb, byteOffset, capacity, isReadOnly());
      b.limit = limit;
      b.position = position;
      b.mark = mark;
      return b;
    }

    @Override
    public CharBuffer subSequence(int start, int end) {
      if (start < 0 || end < start || end > remaining()) throw new IndexOutOfBoundsException();
      return new ByteBufferAsCharBuffer(bb, byteOffset + (position + start) * 2, end - start, isReadOnly());
    }

    @Override
    protected void doPut(int offset, char value) {
      bb.putChar(byteOffset + offset * 2, value);
    }

    @Override
    public CharBuffer put(char[] src, int offset, int length) {
      checkPut(position, length, false);
      for (int i = 0; i < length; i++) {
        bb.putChar(byteOffset + (position + i) * 2, src[offset + i]);
      }
      position += length;
      return this;
    }

    @Override
    protected char doGet(int offset) {
      return bb.getChar(byteOffset + offset * 2);
    }

    @Override
    public CharBuffer get(char[] dst, int offset, int length) {
      checkGet(position, length, false);
      for (int i = 0; i < length; i++) {
        dst[offset + i] = bb.getChar(byteOffset + (position + i) * 2);
      }
      position += length;
      return this;
    }

    @Override
    public boolean isDirect() {
      return bb.isDirect();
    }

    @Override
    public String toString() {
      char[] chars = new char[remaining()];
      for (int i = 0; i < chars.length; i++) {
        chars[i] = doGet(position + i);
      }
      return new String(chars);
    }
  }
}
