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

class HeapByteBuffer extends ByteBuffer {
  HeapByteBuffer(int cap, int lim) {
    super(-1, 0, lim, cap, new byte[cap], 0);
  }

  HeapByteBuffer(byte[] buf, int off, int len) {
    super(-1, off, off + len, buf.length, buf, 0);
  }

  HeapByteBuffer(byte[] buf, int mark, int pos, int lim, int cap, int off, boolean ro) {
    super(mark, pos, lim, cap, buf, off);
    this.isReadOnly = ro;
  }

  @Override
  String bufferName() {
    return "java.nio.HeapByteBuffer" + (isReadOnly ? "R" : "");
  }

  @Override
  public boolean isReadOnly() {
    return isReadOnly;
  }

  @Override
  public boolean isDirect() {
    return false;
  }

  private int ix(int i) {
    return i + offset;
  }

  @Override
  public ByteBuffer slice() {
    int pos = position();
    int lim = limit();
    int rem = pos <= lim ? lim - pos : 0;
    return new HeapByteBuffer(hb, -1, 0, rem, rem, pos + offset, isReadOnly);
  }

  @Override
  public ByteBuffer slice(int index, int length) {
    Objects.checkFromIndexSize(index, length, limit());
    return new HeapByteBuffer(hb, -1, 0, length, length, index + offset, isReadOnly);
  }

  @Override
  public ByteBuffer duplicate() {
    return new HeapByteBuffer(hb, markValue(), position(), limit(), capacity(), offset, isReadOnly);
  }

  @Override
  public ByteBuffer asReadOnlyBuffer() {
    return new HeapByteBuffer(hb, markValue(), position(), limit(), capacity(), offset, true);
  }

  @Override
  byte doGet(int index) {
    return hb[ix(index)];
  }

  @Override
  void doPut(int index, byte b) {
    hb[ix(index)] = b;
  }

  @Override
  public byte get() {
    return hb[ix(nextGetIndex())];
  }

  @Override
  public byte get(int index) {
    return hb[ix(checkIndex(index))];
  }

  @Override
  public ByteBuffer put(byte b) {
    if (isReadOnly) {
      throw new ReadOnlyBufferException();
    }
    hb[ix(nextPutIndex())] = b;
    return this;
  }

  @Override
  public ByteBuffer put(int index, byte b) {
    if (isReadOnly) {
      throw new ReadOnlyBufferException();
    }
    hb[ix(checkIndex(index))] = b;
    return this;
  }

  @Override
  public ByteBuffer get(byte[] dst, int offset, int length) {
    Objects.checkFromIndexSize(offset, length, dst.length);
    int pos = position();
    if (length > limit() - pos) {
      throw new BufferUnderflowException();
    }
    System.arraycopy(hb, ix(pos), dst, offset, length);
    position(pos + length);
    return this;
  }

  @Override
  public ByteBuffer put(byte[] src, int offset, int length) {
    if (isReadOnly) {
      throw new ReadOnlyBufferException();
    }
    Objects.checkFromIndexSize(offset, length, src.length);
    int pos = position();
    if (length > limit() - pos) {
      throw new BufferOverflowException();
    }
    System.arraycopy(src, offset, hb, ix(pos), length);
    position(pos + length);
    return this;
  }

  @Override
  public ByteBuffer put(ByteBuffer src) {
    if (src instanceof HeapByteBuffer) {
      if (src == this) {
        throw createSameBufferException();
      }
      if (isReadOnly) {
        throw new ReadOnlyBufferException();
      }
      HeapByteBuffer sb = (HeapByteBuffer) src;
      int srcPos = sb.position();
      int srcLim = sb.limit();
      int srcRem = srcPos <= srcLim ? srcLim - srcPos : 0;
      int pos = position();
      int lim = limit();
      int rem = pos <= lim ? lim - pos : 0;
      if (srcRem > rem) {
        throw new BufferOverflowException();
      }
      System.arraycopy(sb.hb, sb.ix(srcPos), hb, ix(pos), srcRem);
      sb.position(srcPos + srcRem);
      position(pos + srcRem);
      return this;
    }
    return super.put(src);
  }

  @Override
  public ByteBuffer compact() {
    if (isReadOnly) {
      throw new ReadOnlyBufferException();
    }
    int pos = position();
    int lim = limit();
    int rem = pos <= lim ? lim - pos : 0;
    System.arraycopy(hb, ix(pos), hb, ix(0), rem);
    position(rem);
    limit(capacity());
    discardMark();
    return this;
  }

  @Override
  public char getChar() {
    return getChar0();
  }

  @Override
  public char getChar(int index) {
    return getChar0(index);
  }

  @Override
  public ByteBuffer putChar(char value) {
    return putChar0(value);
  }

  @Override
  public ByteBuffer putChar(int index, char value) {
    return putChar0(index, value);
  }

  @Override
  public short getShort() {
    return getShort0();
  }

  @Override
  public short getShort(int index) {
    return getShort0(index);
  }

  @Override
  public ByteBuffer putShort(short value) {
    return putShort0(value);
  }

  @Override
  public ByteBuffer putShort(int index, short value) {
    return putShort0(index, value);
  }

  @Override
  public int getInt() {
    return getInt0();
  }

  @Override
  public int getInt(int index) {
    return getInt0(index);
  }

  @Override
  public ByteBuffer putInt(int value) {
    return putInt0(value);
  }

  @Override
  public ByteBuffer putInt(int index, int value) {
    return putInt0(index, value);
  }

  @Override
  public long getLong() {
    return getLong0();
  }

  @Override
  public long getLong(int index) {
    return getLong0(index);
  }

  @Override
  public ByteBuffer putLong(long value) {
    return putLong0(value);
  }

  @Override
  public ByteBuffer putLong(int index, long value) {
    return putLong0(index, value);
  }

  @Override
  public float getFloat() {
    return getFloat0();
  }

  @Override
  public float getFloat(int index) {
    return getFloat0(index);
  }

  @Override
  public ByteBuffer putFloat(float value) {
    return putFloat0(value);
  }

  @Override
  public ByteBuffer putFloat(int index, float value) {
    return putFloat0(index, value);
  }

  @Override
  public double getDouble() {
    return getDouble0();
  }

  @Override
  public double getDouble(int index) {
    return getDouble0(index);
  }

  @Override
  public ByteBuffer putDouble(double value) {
    return putDouble0(value);
  }

  @Override
  public ByteBuffer putDouble(int index, double value) {
    return putDouble0(index, value);
  }

  @Override
  public CharBuffer asCharBuffer() {
    return asCharBuffer0();
  }

  @Override
  public ShortBuffer asShortBuffer() {
    return asShortBuffer0();
  }

  @Override
  public IntBuffer asIntBuffer() {
    return asIntBuffer0();
  }

  @Override
  public LongBuffer asLongBuffer() {
    return asLongBuffer0();
  }

  @Override
  public FloatBuffer asFloatBuffer() {
    return asFloatBuffer0();
  }

  @Override
  public DoubleBuffer asDoubleBuffer() {
    return asDoubleBuffer0();
  }
}
