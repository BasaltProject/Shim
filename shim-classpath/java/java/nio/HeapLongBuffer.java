/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.nio;

import java.util.Objects;

class HeapLongBuffer extends LongBuffer {
  HeapLongBuffer(int cap, int lim) {
    super(-1, 0, lim, cap, new long[cap], 0);
  }

  HeapLongBuffer(long[] buf, int off, int len) {
    super(-1, off, off + len, buf.length, buf, 0);
  }

  HeapLongBuffer(long[] buf, int mark, int pos, int lim, int cap, int off, boolean ro) {
    super(mark, pos, lim, cap, buf, off);
    this.isReadOnly = ro;
  }

  @Override
  String bufferName() {
    return "java.nio.HeapLongBuffer" + (isReadOnly ? "R" : "");
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
  public LongBuffer slice() {
    int pos = position();
    int lim = limit();
    int rem = pos <= lim ? lim - pos : 0;
    return new HeapLongBuffer(hb, -1, 0, rem, rem, pos + offset, isReadOnly);
  }

  @Override
  public LongBuffer slice(int index, int length) {
    Objects.checkFromIndexSize(index, length, limit());
    return new HeapLongBuffer(hb, -1, 0, length, length, index + offset, isReadOnly);
  }

  @Override
  public LongBuffer duplicate() {
    return new HeapLongBuffer(hb, markValue(), position(), limit(), capacity(), offset, isReadOnly);
  }

  @Override
  public LongBuffer asReadOnlyBuffer() {
    return new HeapLongBuffer(hb, markValue(), position(), limit(), capacity(), offset, true);
  }

  @Override
  public long get() {
    return hb[ix(nextGetIndex())];
  }

  @Override
  public long get(int index) {
    return hb[ix(checkIndex(index))];
  }

  @Override
  public LongBuffer get(long[] dst, int offset, int length) {
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
  public LongBuffer put(long x) {
    if (isReadOnly) {
      throw new ReadOnlyBufferException();
    }
    hb[ix(nextPutIndex())] = x;
    return this;
  }

  @Override
  public LongBuffer put(int index, long x) {
    if (isReadOnly) {
      throw new ReadOnlyBufferException();
    }
    hb[ix(checkIndex(index))] = x;
    return this;
  }

  @Override
  public LongBuffer put(long[] src, int offset, int length) {
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
  public LongBuffer put(LongBuffer src) {
    if (src instanceof HeapLongBuffer) {
      if (src == this) {
        throw createSameBufferException();
      }
      if (isReadOnly) {
        throw new ReadOnlyBufferException();
      }
      HeapLongBuffer sb = (HeapLongBuffer) src;
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
  public LongBuffer compact() {
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
}
