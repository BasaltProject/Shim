/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.nio;

import java.util.Objects;

class ByteBufferAsLongBuffer extends LongBuffer {
  private final ByteBuffer bb;
  private final int byteOffset;
  private final boolean bigEndian;

  ByteBufferAsLongBuffer(ByteBuffer bb, int mark, int pos, int lim, int cap, int byteOffset, boolean bigEndian,
      boolean ro) {
    super(mark, pos, lim, cap);
    this.bb = bb;
    this.byteOffset = byteOffset;
    this.bigEndian = bigEndian;
    this.isReadOnly = ro;
  }

  @Override
  String bufferName() {
    return "java.nio.ByteBufferAsLongBuffer" + (isReadOnly ? "R" : "") + (bigEndian ? "B" : "L");
  }

  private int ix(int i) {
    return (i << 3) + byteOffset;
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
  public LongBuffer slice() {
    int pos = position();
    int lim = limit();
    int rem = pos <= lim ? lim - pos : 0;
    return new ByteBufferAsLongBuffer(bb, -1, 0, rem, rem, ix(pos), bigEndian, isReadOnly);
  }

  @Override
  public LongBuffer slice(int index, int length) {
    Objects.checkFromIndexSize(index, length, limit());
    return new ByteBufferAsLongBuffer(bb, -1, 0, length, length, ix(index), bigEndian, isReadOnly);
  }

  @Override
  public LongBuffer duplicate() {
    return new ByteBufferAsLongBuffer(bb, markValue(), position(), limit(), capacity(), byteOffset, bigEndian,
        isReadOnly);
  }

  @Override
  public LongBuffer asReadOnlyBuffer() {
    return new ByteBufferAsLongBuffer(bb, markValue(), position(), limit(), capacity(), byteOffset, bigEndian, true);
  }

  @Override
  public long get() {
    return bb.rawGetLong(ix(nextGetIndex()), bigEndian);
  }

  @Override
  public long get(int index) {
    return bb.rawGetLong(ix(checkIndex(index)), bigEndian);
  }

  @Override
  public LongBuffer put(long x) {
    if (isReadOnly) {
      throw new ReadOnlyBufferException();
    }
    bb.rawPutLong(ix(nextPutIndex()), x, bigEndian);
    return this;
  }

  @Override
  public LongBuffer put(int index, long x) {
    if (isReadOnly) {
      throw new ReadOnlyBufferException();
    }
    bb.rawPutLong(ix(checkIndex(index)), x, bigEndian);
    return this;
  }

  @Override
  public LongBuffer compact() {
    if (isReadOnly) {
      throw new ReadOnlyBufferException();
    }
    int pos = position();
    int lim = limit();
    int rem = pos <= lim ? lim - pos : 0;
    for (int i = 0; i < rem; i++) {
      bb.rawPutLong(ix(i), bb.rawGetLong(ix(pos + i), bigEndian), bigEndian);
    }
    position(rem);
    limit(capacity());
    discardMark();
    return this;
  }
}
