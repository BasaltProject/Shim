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

public abstract class ByteBuffer extends Buffer implements Comparable<ByteBuffer> {
  final byte[] hb;
  final int offset;
  boolean isReadOnly;
  boolean bigEndian = true;

  ByteBuffer(int mark, int pos, int lim, int cap, byte[] hb, int offset) {
    super(mark, pos, lim, cap);
    this.hb = hb;
    this.offset = offset;
  }

  ByteBuffer(int mark, int pos, int lim, int cap) {
    this(mark, pos, lim, cap, null, 0);
  }

  public static ByteBuffer allocateDirect(int capacity) {
    if (capacity < 0) {
      throw createCapacityException(capacity);
    }
    return FixedArrayByteBuffer.make(capacity);
  }

  public static ByteBuffer allocate(int capacity) {
    if (capacity < 0) {
      throw createCapacityException(capacity);
    }
    return new HeapByteBuffer(capacity, capacity);
  }

  public static ByteBuffer wrap(byte[] array, int offset, int length) {
    try {
      return new HeapByteBuffer(array, offset, length);
    } catch (IllegalArgumentException x) {
      throw new IndexOutOfBoundsException();
    }
  }

  public static ByteBuffer wrap(byte[] array) {
    return wrap(array, 0, array.length);
  }

  @Override
  public abstract ByteBuffer slice();

  @Override
  public abstract ByteBuffer slice(int index, int length);

  @Override
  public abstract ByteBuffer duplicate();

  public abstract ByteBuffer asReadOnlyBuffer();

  public abstract byte get();

  public abstract ByteBuffer put(byte b);

  public abstract byte get(int index);

  public abstract ByteBuffer put(int index, byte b);

  abstract byte doGet(int index);

  abstract void doPut(int index, byte b);

  public ByteBuffer get(byte[] dst, int offset, int length) {
    Objects.checkFromIndexSize(offset, length, dst.length);
    int pos = position();
    if (length > limit() - pos) {
      throw new BufferUnderflowException();
    }
    for (int i = 0; i < length; i++) {
      dst[offset + i] = doGet(pos + i);
    }
    position(pos + length);
    return this;
  }

  public ByteBuffer get(byte[] dst) {
    return get(dst, 0, dst.length);
  }

  public ByteBuffer get(int index, byte[] dst, int offset, int length) {
    Objects.checkFromIndexSize(index, length, limit());
    Objects.checkFromIndexSize(offset, length, dst.length);
    for (int i = 0; i < length; i++) {
      dst[offset + i] = doGet(index + i);
    }
    return this;
  }

  public ByteBuffer get(int index, byte[] dst) {
    return get(index, dst, 0, dst.length);
  }

  public ByteBuffer put(ByteBuffer src) {
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
    byte[] tmp = new byte[srcRem];
    for (int i = 0; i < srcRem; i++) {
      tmp[i] = src.doGet(srcPos + i);
    }
    for (int i = 0; i < srcRem; i++) {
      doPut(pos + i, tmp[i]);
    }
    src.position(srcPos + srcRem);
    position(pos + srcRem);
    return this;
  }

  public ByteBuffer put(int index, ByteBuffer src, int offset, int length) {
    Objects.checkFromIndexSize(index, length, limit());
    Objects.checkFromIndexSize(offset, length, src.limit());
    if (isReadOnly()) {
      throw new ReadOnlyBufferException();
    }
    byte[] tmp = new byte[length];
    for (int i = 0; i < length; i++) {
      tmp[i] = src.doGet(offset + i);
    }
    for (int i = 0; i < length; i++) {
      doPut(index + i, tmp[i]);
    }
    return this;
  }

  public ByteBuffer put(byte[] src, int offset, int length) {
    if (isReadOnly()) {
      throw new ReadOnlyBufferException();
    }
    Objects.checkFromIndexSize(offset, length, src.length);
    int pos = position();
    if (length > limit() - pos) {
      throw new BufferOverflowException();
    }
    for (int i = 0; i < length; i++) {
      doPut(pos + i, src[offset + i]);
    }
    position(pos + length);
    return this;
  }

  public final ByteBuffer put(byte[] src) {
    return put(src, 0, src.length);
  }

  public ByteBuffer put(int index, byte[] src, int offset, int length) {
    if (isReadOnly()) {
      throw new ReadOnlyBufferException();
    }
    Objects.checkFromIndexSize(index, length, limit());
    Objects.checkFromIndexSize(offset, length, src.length);
    for (int i = 0; i < length; i++) {
      doPut(index + i, src[offset + i]);
    }
    return this;
  }

  public ByteBuffer put(int index, byte[] src) {
    return put(index, src, 0, src.length);
  }

  @Override
  public final boolean hasArray() {
    return hb != null && !isReadOnly;
  }

  @Override
  public final byte[] array() {
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

  public abstract ByteBuffer compact();

  @Override
  public abstract boolean isDirect();

  @Override
  public String toString() {
    return bufferName() + "[pos=" + position() + " lim=" + limit() + " cap=" + capacity() + "]";
  }

  @Override
  public int hashCode() {
    int h = 1;
    int p = position();
    for (int i = limit() - 1; i >= p; i--) {
      h = 31 * h + (int) doGet(i);
    }
    return h;
  }

  @Override
  public boolean equals(Object ob) {
    if (this == ob) {
      return true;
    }
    if (!(ob instanceof ByteBuffer)) {
      return false;
    }
    ByteBuffer that = (ByteBuffer) ob;
    int thisPos = this.position();
    int thisRem = this.limit() - thisPos;
    int thatPos = that.position();
    int thatRem = that.limit() - thatPos;
    if (thisRem < 0 || thisRem != thatRem) {
      return false;
    }
    return firstMismatch(this, thisPos, that, thatPos, thisRem) < 0;
  }

  @Override
  public int compareTo(ByteBuffer that) {
    int thisPos = this.position();
    int thisRem = this.limit() - thisPos;
    int thatPos = that.position();
    int thatRem = that.limit() - thatPos;
    int length = Math.min(thisRem, thatRem);
    if (length < 0) {
      return -1;
    }
    int i = firstMismatch(this, thisPos, that, thatPos, length);
    if (i >= 0) {
      return Byte.compare(this.doGet(thisPos + i), that.doGet(thatPos + i));
    }
    return thisRem - thatRem;
  }

  public int mismatch(ByteBuffer that) {
    int thisPos = this.position();
    int thisRem = this.limit() - thisPos;
    int thatPos = that.position();
    int thatRem = that.limit() - thatPos;
    int length = Math.min(thisRem, thatRem);
    if (length < 0) {
      return -1;
    }
    int r = firstMismatch(this, thisPos, that, thatPos, length);
    return (r == -1 && thisRem != thatRem) ? length : r;
  }

  private static int firstMismatch(ByteBuffer a, int aPos, ByteBuffer b, int bPos, int length) {
    for (int i = 0; i < length; i++) {
      if (a.doGet(aPos + i) != b.doGet(bPos + i)) {
        return i;
      }
    }
    return -1;
  }

  public final ByteOrder order() {
    return bigEndian ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN;
  }

  public final ByteBuffer order(ByteOrder bo) {
    bigEndian = bo == ByteOrder.BIG_ENDIAN;
    return this;
  }

  public final int alignmentOffset(int index, int unitSize) {
    if (index < 0) {
      throw new IllegalArgumentException("Index less than zero: " + index);
    }
    if (unitSize < 1 || (unitSize & (unitSize - 1)) != 0) {
      throw new IllegalArgumentException("Unit size not a power of two: " + unitSize);
    }
    if (unitSize > 1 && !isDirect()) {
      throw new UnsupportedOperationException("Unit size unsupported for non-direct buffers: " + unitSize);
    }
    return unitSize == 1 ? 0 : (int) ((directAddress() + index) & (unitSize - 1));
  }

  long directAddress() {
    return 0;
  }

  public final ByteBuffer alignedSlice(int unitSize) {
    int pos = position();
    int lim = limit();
    int posMod = alignmentOffset(pos, unitSize);
    int limMod = alignmentOffset(lim, unitSize);
    int alignedPos = posMod > 0 ? pos + (unitSize - posMod) : pos;
    int alignedLim = lim - limMod;
    if (alignedPos > lim || alignedLim < pos) {
      alignedPos = alignedLim = pos;
    }
    return slice(alignedPos, alignedLim - alignedPos);
  }

  final short rawGetShort(int i, boolean be) {
    int a = doGet(i) & 0xFF;
    int b = doGet(i + 1) & 0xFF;
    return (short) (be ? (a << 8) | b : (b << 8) | a);
  }

  final void rawPutShort(int i, short v, boolean be) {
    if (be) {
      doPut(i, (byte) (v >> 8));
      doPut(i + 1, (byte) v);
    } else {
      doPut(i, (byte) v);
      doPut(i + 1, (byte) (v >> 8));
    }
  }

  final int rawGetInt(int i, boolean be) {
    if (be) {
      return ((doGet(i) & 0xFF) << 24) | ((doGet(i + 1) & 0xFF) << 16) | ((doGet(i + 2) & 0xFF) << 8)
          | (doGet(i + 3) & 0xFF);
    }
    return (doGet(i) & 0xFF) | ((doGet(i + 1) & 0xFF) << 8) | ((doGet(i + 2) & 0xFF) << 16)
        | ((doGet(i + 3) & 0xFF) << 24);
  }

  final void rawPutInt(int i, int v, boolean be) {
    if (be) {
      doPut(i, (byte) (v >> 24));
      doPut(i + 1, (byte) (v >> 16));
      doPut(i + 2, (byte) (v >> 8));
      doPut(i + 3, (byte) v);
    } else {
      doPut(i, (byte) v);
      doPut(i + 1, (byte) (v >> 8));
      doPut(i + 2, (byte) (v >> 16));
      doPut(i + 3, (byte) (v >> 24));
    }
  }

  final long rawGetLong(int i, boolean be) {
    long hi = rawGetInt(be ? i : i + 4, be) & 0xFFFFFFFFL;
    long lo = rawGetInt(be ? i + 4 : i, be) & 0xFFFFFFFFL;
    return (hi << 32) | lo;
  }

  final void rawPutLong(int i, long v, boolean be) {
    rawPutInt(be ? i : i + 4, (int) (v >>> 32), be);
    rawPutInt(be ? i + 4 : i, (int) v, be);
  }

  private void checkWritable() {
    if (isReadOnly()) {
      throw new ReadOnlyBufferException();
    }
  }

  public char getChar() {
    return (char) rawGetShort(nextGetIndex(2), bigEndian);
  }

  public char getChar(int index) {
    return (char) rawGetShort(checkIndex(index, 2), bigEndian);
  }

  public ByteBuffer putChar(char value) {
    checkWritable();
    rawPutShort(nextPutIndex(2), (short) value, bigEndian);
    return this;
  }

  public ByteBuffer putChar(int index, char value) {
    checkWritable();
    rawPutShort(checkIndex(index, 2), (short) value, bigEndian);
    return this;
  }

  public short getShort() {
    return rawGetShort(nextGetIndex(2), bigEndian);
  }

  public short getShort(int index) {
    return rawGetShort(checkIndex(index, 2), bigEndian);
  }

  public ByteBuffer putShort(short value) {
    checkWritable();
    rawPutShort(nextPutIndex(2), value, bigEndian);
    return this;
  }

  public ByteBuffer putShort(int index, short value) {
    checkWritable();
    rawPutShort(checkIndex(index, 2), value, bigEndian);
    return this;
  }

  public int getInt() {
    return rawGetInt(nextGetIndex(4), bigEndian);
  }

  public int getInt(int index) {
    return rawGetInt(checkIndex(index, 4), bigEndian);
  }

  public ByteBuffer putInt(int value) {
    checkWritable();
    rawPutInt(nextPutIndex(4), value, bigEndian);
    return this;
  }

  public ByteBuffer putInt(int index, int value) {
    checkWritable();
    rawPutInt(checkIndex(index, 4), value, bigEndian);
    return this;
  }

  public long getLong() {
    return rawGetLong(nextGetIndex(8), bigEndian);
  }

  public long getLong(int index) {
    return rawGetLong(checkIndex(index, 8), bigEndian);
  }

  public ByteBuffer putLong(long value) {
    checkWritable();
    rawPutLong(nextPutIndex(8), value, bigEndian);
    return this;
  }

  public ByteBuffer putLong(int index, long value) {
    checkWritable();
    rawPutLong(checkIndex(index, 8), value, bigEndian);
    return this;
  }

  public float getFloat() {
    return Float.intBitsToFloat(getInt());
  }

  public float getFloat(int index) {
    return Float.intBitsToFloat(getInt(index));
  }

  public ByteBuffer putFloat(float value) {
    return putInt(Float.floatToRawIntBits(value));
  }

  public ByteBuffer putFloat(int index, float value) {
    return putInt(index, Float.floatToRawIntBits(value));
  }

  public double getDouble() {
    return Double.longBitsToDouble(getLong());
  }

  public double getDouble(int index) {
    return Double.longBitsToDouble(getLong(index));
  }

  public ByteBuffer putDouble(double value) {
    return putLong(Double.doubleToRawLongBits(value));
  }

  public ByteBuffer putDouble(int index, double value) {
    return putLong(index, Double.doubleToRawLongBits(value));
  }

  public CharBuffer asCharBuffer() {
    int pos = position();
    int lim = limit();
    int rem = pos <= lim ? lim - pos : 0;
    int size = rem >> 1;
    return new ByteBufferAsCharBuffer(this, -1, 0, size, size, pos, bigEndian, isReadOnly());
  }

  public ShortBuffer asShortBuffer() {
    int pos = position();
    int lim = limit();
    int rem = pos <= lim ? lim - pos : 0;
    int size = rem >> 1;
    return new ByteBufferAsShortBuffer(this, -1, 0, size, size, pos, bigEndian, isReadOnly());
  }

  public IntBuffer asIntBuffer() {
    int pos = position();
    int lim = limit();
    int rem = pos <= lim ? lim - pos : 0;
    int size = rem >> 2;
    return new ByteBufferAsIntBuffer(this, -1, 0, size, size, pos, bigEndian, isReadOnly());
  }

  public LongBuffer asLongBuffer() {
    int pos = position();
    int lim = limit();
    int rem = pos <= lim ? lim - pos : 0;
    int size = rem >> 3;
    return new ByteBufferAsLongBuffer(this, -1, 0, size, size, pos, bigEndian, isReadOnly());
  }

  public FloatBuffer asFloatBuffer() {
    int pos = position();
    int lim = limit();
    int rem = pos <= lim ? lim - pos : 0;
    int size = rem >> 2;
    return new ByteBufferAsFloatBuffer(this, -1, 0, size, size, pos, bigEndian, isReadOnly());
  }

  public DoubleBuffer asDoubleBuffer() {
    int pos = position();
    int lim = limit();
    int rem = pos <= lim ? lim - pos : 0;
    int size = rem >> 3;
    return new ByteBufferAsDoubleBuffer(this, -1, 0, size, size, pos, bigEndian, isReadOnly());
  }
}
