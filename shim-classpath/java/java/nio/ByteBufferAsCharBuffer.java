/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.nio;

import java.util.Objects;

class ByteBufferAsCharBuffer extends CharBuffer {
  private final ByteBuffer bb;
  private final int byteOffset;
  private final boolean bigEndian;

  ByteBufferAsCharBuffer(ByteBuffer bb, int mark, int pos, int lim, int cap, int byteOffset, boolean bigEndian,
      boolean ro) {
    super(mark, pos, lim, cap);
    this.bb = bb;
    this.byteOffset = byteOffset;
    this.bigEndian = bigEndian;
    this.isReadOnly = ro;
  }

  @Override
  String bufferName() {
    return "java.nio.ByteBufferAsCharBuffer" + (isReadOnly ? "R" : "") + (bigEndian ? "B" : "L");
  }

  private int ix(int i) {
    return (i << 1) + byteOffset;
  }

  @Override
  public boolean isReadOnly() {
    return isReadOnly;
  }

  @Override
  public boolean isDirect() {
    return bb.isDirect();
  }

  @Override
  public ByteOrder order() {
    return bigEndian ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN;
  }

  @Override
  public CharBuffer slice() {
    int pos = position();
    int lim = limit();
    int rem = pos <= lim ? lim - pos : 0;
    return new ByteBufferAsCharBuffer(bb, -1, 0, rem, rem, ix(pos), bigEndian, isReadOnly);
  }

  @Override
  public CharBuffer slice(int index, int length) {
    Objects.checkFromIndexSize(index, length, limit());
    return new ByteBufferAsCharBuffer(bb, -1, 0, length, length, ix(index), bigEndian, isReadOnly);
  }

  @Override
  public CharBuffer duplicate() {
    return new ByteBufferAsCharBuffer(bb, markValue(), position(), limit(), capacity(), byteOffset, bigEndian,
        isReadOnly);
  }

  @Override
  public CharBuffer asReadOnlyBuffer() {
    return new ByteBufferAsCharBuffer(bb, markValue(), position(), limit(), capacity(), byteOffset, bigEndian, true);
  }

  @Override
  public char get() {
    return (char) bb.rawGetShort(ix(nextGetIndex()), bigEndian);
  }

  @Override
  public char get(int index) {
    return (char) bb.rawGetShort(ix(checkIndex(index)), bigEndian);
  }

  @Override
  public CharBuffer put(char x) {
    if (isReadOnly) {
      throw new ReadOnlyBufferException();
    }
    bb.rawPutShort(ix(nextPutIndex()), (short) x, bigEndian);
    return this;
  }

  @Override
  public CharBuffer put(int index, char x) {
    if (isReadOnly) {
      throw new ReadOnlyBufferException();
    }
    bb.rawPutShort(ix(checkIndex(index)), (short) x, bigEndian);
    return this;
  }

  @Override
  public CharBuffer compact() {
    if (isReadOnly) {
      throw new ReadOnlyBufferException();
    }
    int pos = position();
    int lim = limit();
    int rem = pos <= lim ? lim - pos : 0;
    for (int i = 0; i < rem; i++) {
      bb.rawPutShort(ix(i), bb.rawGetShort(ix(pos + i), bigEndian), bigEndian);
    }
    position(rem);
    limit(capacity());
    discardMark();
    return this;
  }

  @Override
  String toString(int start, int end) {
    try {
      int len = end - start;
      char[] ca = new char[len];
      CharBuffer cb = CharBuffer.wrap(ca);
      CharBuffer db = this.duplicate();
      db.position(start);
      db.limit(end);
      cb.put(db);
      return new String(ca);
    } catch (StringIndexOutOfBoundsException x) {
      throw new IndexOutOfBoundsException();
    }
  }

  @Override
  public CharBuffer subSequence(int start, int end) {
    int pos = position();
    int lim = limit();
    pos = pos <= lim ? pos : lim;
    int len = lim - pos;
    if (start < 0 || end > len || start > end) {
      throw new IndexOutOfBoundsException();
    }
    return new ByteBufferAsCharBuffer(bb, -1, pos + start, pos + end, capacity(), byteOffset, bigEndian,
        isReadOnly);
  }
}
