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

  public abstract char getChar();

  final char getChar0() {
    return (char) rawGetShort(nextGetIndex(2), bigEndian);
  }

  public abstract char getChar(int index);

  final char getChar0(int index) {
    return (char) rawGetShort(checkIndex(index, 2), bigEndian);
  }

  public abstract ByteBuffer putChar(char value);

  final ByteBuffer putChar0(char value) {
    checkWritable();
    rawPutShort(nextPutIndex(2), (short) value, bigEndian);
    return this;
  }

  public abstract ByteBuffer putChar(int index, char value);

  final ByteBuffer putChar0(int index, char value) {
    checkWritable();
    rawPutShort(checkIndex(index, 2), (short) value, bigEndian);
    return this;
  }

  public abstract short getShort();

  final short getShort0() {
    return rawGetShort(nextGetIndex(2), bigEndian);
  }

  public abstract short getShort(int index);

  final short getShort0(int index) {
    return rawGetShort(checkIndex(index, 2), bigEndian);
  }

  public abstract ByteBuffer putShort(short value);

  final ByteBuffer putShort0(short value) {
    checkWritable();
    rawPutShort(nextPutIndex(2), value, bigEndian);
    return this;
  }

  public abstract ByteBuffer putShort(int index, short value);

  final ByteBuffer putShort0(int index, short value) {
    checkWritable();
    rawPutShort(checkIndex(index, 2), value, bigEndian);
    return this;
  }

  public abstract int getInt();

  final int getInt0() {
    return rawGetInt(nextGetIndex(4), bigEndian);
  }

  public abstract int getInt(int index);

  final int getInt0(int index) {
    return rawGetInt(checkIndex(index, 4), bigEndian);
  }

  public abstract ByteBuffer putInt(int value);

  final ByteBuffer putInt0(int value) {
    checkWritable();
    rawPutInt(nextPutIndex(4), value, bigEndian);
    return this;
  }

  public abstract ByteBuffer putInt(int index, int value);

  final ByteBuffer putInt0(int index, int value) {
    checkWritable();
    rawPutInt(checkIndex(index, 4), value, bigEndian);
    return this;
  }

  public abstract long getLong();

  final long getLong0() {
    return rawGetLong(nextGetIndex(8), bigEndian);
  }

  public abstract long getLong(int index);

  final long getLong0(int index) {
    return rawGetLong(checkIndex(index, 8), bigEndian);
  }

  public abstract ByteBuffer putLong(long value);

  final ByteBuffer putLong0(long value) {
    checkWritable();
    rawPutLong(nextPutIndex(8), value, bigEndian);
    return this;
  }

  public abstract ByteBuffer putLong(int index, long value);

  final ByteBuffer putLong0(int index, long value) {
    checkWritable();
    rawPutLong(checkIndex(index, 8), value, bigEndian);
    return this;
  }

  public abstract float getFloat();

  final float getFloat0() {
    return Float.intBitsToFloat(getInt0());
  }

  public abstract float getFloat(int index);

  final float getFloat0(int index) {
    return Float.intBitsToFloat(getInt0(index));
  }

  public abstract ByteBuffer putFloat(float value);

  final ByteBuffer putFloat0(float value) {
    return putInt0(Float.floatToRawIntBits(value));
  }

  public abstract ByteBuffer putFloat(int index, float value);

  final ByteBuffer putFloat0(int index, float value) {
    return putInt0(index, Float.floatToRawIntBits(value));
  }

  public abstract double getDouble();

  final double getDouble0() {
    return Double.longBitsToDouble(getLong0());
  }

  public abstract double getDouble(int index);

  final double getDouble0(int index) {
    return Double.longBitsToDouble(getLong0(index));
  }

  public abstract ByteBuffer putDouble(double value);

  final ByteBuffer putDouble0(double value) {
    return putLong0(Double.doubleToRawLongBits(value));
  }

  public abstract ByteBuffer putDouble(int index, double value);

  final ByteBuffer putDouble0(int index, double value) {
    return putLong0(index, Double.doubleToRawLongBits(value));
  }

  public abstract CharBuffer asCharBuffer();

  final CharBuffer asCharBuffer0() {
    int pos = position();
    int lim = limit();
    int rem = pos <= lim ? lim - pos : 0;
    int size = rem >> 1;
    return new ByteBufferAsCharBuffer(this, -1, 0, size, size, pos, bigEndian, isReadOnly());
  }

  public abstract ShortBuffer asShortBuffer();

  final ShortBuffer asShortBuffer0() {
    int pos = position();
    int lim = limit();
    int rem = pos <= lim ? lim - pos : 0;
    int size = rem >> 1;
    return new ByteBufferAsShortBuffer(this, -1, 0, size, size, pos, bigEndian, isReadOnly());
  }

  public abstract IntBuffer asIntBuffer();

  final IntBuffer asIntBuffer0() {
    int pos = position();
    int lim = limit();
    int rem = pos <= lim ? lim - pos : 0;
    int size = rem >> 2;
    return new ByteBufferAsIntBuffer(this, -1, 0, size, size, pos, bigEndian, isReadOnly());
  }

  public abstract LongBuffer asLongBuffer();

  final LongBuffer asLongBuffer0() {
    int pos = position();
    int lim = limit();
    int rem = pos <= lim ? lim - pos : 0;
    int size = rem >> 3;
    return new ByteBufferAsLongBuffer(this, -1, 0, size, size, pos, bigEndian, isReadOnly());
  }

  public abstract FloatBuffer asFloatBuffer();

  final FloatBuffer asFloatBuffer0() {
    int pos = position();
    int lim = limit();
    int rem = pos <= lim ? lim - pos : 0;
    int size = rem >> 2;
    return new ByteBufferAsFloatBuffer(this, -1, 0, size, size, pos, bigEndian, isReadOnly());
  }

  public abstract DoubleBuffer asDoubleBuffer();

  final DoubleBuffer asDoubleBuffer0() {
    int pos = position();
    int lim = limit();
    int rem = pos <= lim ? lim - pos : 0;
    int size = rem >> 3;
    return new ByteBufferAsDoubleBuffer(this, -1, 0, size, size, pos, bigEndian, isReadOnly());
  }
}
