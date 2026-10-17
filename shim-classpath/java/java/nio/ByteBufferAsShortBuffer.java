/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.nio;

import java.util.Objects;

class ByteBufferAsShortBuffer extends ShortBuffer {
  private final ByteBuffer bb;
  private final int byteOffset;
  private final boolean bigEndian;

  ByteBufferAsShortBuffer(ByteBuffer bb, int mark, int pos, int lim, int cap, int byteOffset, boolean bigEndian,
      boolean ro) {
    super(mark, pos, lim, cap);
    this.bb = bb;
    this.byteOffset = byteOffset;
    this.bigEndian = bigEndian;
    this.isReadOnly = ro;
  }

  @Override
  String bufferName() {
    return "java.nio.ByteBufferAsShortBuffer" + (isReadOnly ? "R" : "") + (bigEndian ? "B" : "L");
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
  public ShortBuffer slice() {
    int pos = position();
    int lim = limit();
    int rem = pos <= lim ? lim - pos : 0;
    return new ByteBufferAsShortBuffer(bb, -1, 0, rem, rem, ix(pos), bigEndian, isReadOnly);
  }

  @Override
  public ShortBuffer slice(int index, int length) {
    Objects.checkFromIndexSize(index, length, limit());
    return new ByteBufferAsShortBuffer(bb, -1, 0, length, length, ix(index), bigEndian, isReadOnly);
  }

  @Override
  public ShortBuffer duplicate() {
    return new ByteBufferAsShortBuffer(bb, markValue(), position(), limit(), capacity(), byteOffset, bigEndian,
        isReadOnly);
  }

  @Override
  public ShortBuffer asReadOnlyBuffer() {
    return new ByteBufferAsShortBuffer(bb, markValue(), position(), limit(), capacity(), byteOffset, bigEndian, true);
  }

  @Override
  public short get() {
    return bb.rawGetShort(ix(nextGetIndex()), bigEndian);
  }

  @Override
  public short get(int index) {
    return bb.rawGetShort(ix(checkIndex(index)), bigEndian);
  }

  @Override
  public ShortBuffer put(short x) {
    if (isReadOnly) {
      throw new ReadOnlyBufferException();
    }
    bb.rawPutShort(ix(nextPutIndex()), x, bigEndian);
    return this;
  }

  @Override
  public ShortBuffer put(int index, short x) {
    if (isReadOnly) {
      throw new ReadOnlyBufferException();
    }
    bb.rawPutShort(ix(checkIndex(index)), x, bigEndian);
    return this;
  }

  @Override
  public ShortBuffer compact() {
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
}
