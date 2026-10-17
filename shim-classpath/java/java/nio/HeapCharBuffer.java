/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.nio;

import java.util.Objects;

class HeapCharBuffer extends CharBuffer {
  HeapCharBuffer(int cap, int lim) {
    super(-1, 0, lim, cap, new char[cap], 0);
  }

  HeapCharBuffer(char[] buf, int off, int len) {
    super(-1, off, off + len, buf.length, buf, 0);
  }

  HeapCharBuffer(char[] buf, int mark, int pos, int lim, int cap, int off, boolean ro) {
    super(mark, pos, lim, cap, buf, off);
    this.isReadOnly = ro;
  }

  @Override
  String bufferName() {
    return "java.nio.HeapCharBuffer" + (isReadOnly ? "R" : "");
  }

  @Override
  public boolean isReadOnly() {
    return isReadOnly;
  }

  @Override
  public boolean isDirect() {
    return false;
  }

  @Override
  public ByteOrder order() {
    return ByteOrder.nativeOrder();
  }

  private int ix(int i) {
    return i + offset;
  }

  @Override
  public CharBuffer slice() {
    int pos = position();
    int lim = limit();
    int rem = pos <= lim ? lim - pos : 0;
    return new HeapCharBuffer(hb, -1, 0, rem, rem, pos + offset, isReadOnly);
  }

  @Override
  public CharBuffer slice(int index, int length) {
    Objects.checkFromIndexSize(index, length, limit());
    return new HeapCharBuffer(hb, -1, 0, length, length, index + offset, isReadOnly);
  }

  @Override
  public CharBuffer duplicate() {
    return new HeapCharBuffer(hb, markValue(), position(), limit(), capacity(), offset, isReadOnly);
  }

  @Override
  public CharBuffer asReadOnlyBuffer() {
    return new HeapCharBuffer(hb, markValue(), position(), limit(), capacity(), offset, true);
  }

  @Override
  public char get() {
    return hb[ix(nextGetIndex())];
  }

  @Override
  public char get(int index) {
    return hb[ix(checkIndex(index))];
  }

  @Override
  public CharBuffer get(char[] dst, int offset, int length) {
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
  public CharBuffer put(char x) {
    if (isReadOnly) {
      throw new ReadOnlyBufferException();
    }
    hb[ix(nextPutIndex())] = x;
    return this;
  }

  @Override
  public CharBuffer put(int index, char x) {
    if (isReadOnly) {
      throw new ReadOnlyBufferException();
    }
    hb[ix(checkIndex(index))] = x;
    return this;
  }

  @Override
  public CharBuffer put(char[] src, int offset, int length) {
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
  public CharBuffer put(CharBuffer src) {
    if (src instanceof HeapCharBuffer) {
      if (src == this) {
        throw createSameBufferException();
      }
      if (isReadOnly) {
        throw new ReadOnlyBufferException();
      }
      HeapCharBuffer sb = (HeapCharBuffer) src;
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
  public CharBuffer compact() {
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
  String toString(int start, int end) {
    try {
      return new String(hb, start + offset, end - start);
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
    return new HeapCharBuffer(hb, -1, pos + start, pos + end, capacity(), offset, isReadOnly);
  }
}
