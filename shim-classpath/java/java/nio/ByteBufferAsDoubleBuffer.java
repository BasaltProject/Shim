/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.nio;

import java.util.Objects;

class ByteBufferAsDoubleBuffer extends DoubleBuffer {
  private final ByteBuffer bb;
  private final int byteOffset;
  private final boolean bigEndian;

  ByteBufferAsDoubleBuffer(ByteBuffer bb, int mark, int pos, int lim, int cap, int byteOffset, boolean bigEndian,
      boolean ro) {
    super(mark, pos, lim, cap);
    this.bb = bb;
    this.byteOffset = byteOffset;
    this.bigEndian = bigEndian;
    this.isReadOnly = ro;
  }

  @Override
  String bufferName() {
    return "java.nio.ByteBufferAsDoubleBuffer" + (isReadOnly ? "R" : "") + (bigEndian ? "B" : "L");
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
  public DoubleBuffer slice() {
    int pos = position();
    int lim = limit();
    int rem = pos <= lim ? lim - pos : 0;
    return new ByteBufferAsDoubleBuffer(bb, -1, 0, rem, rem, ix(pos), bigEndian, isReadOnly);
  }

  @Override
  public DoubleBuffer slice(int index, int length) {
    Objects.checkFromIndexSize(index, length, limit());
    return new ByteBufferAsDoubleBuffer(bb, -1, 0, length, length, ix(index), bigEndian, isReadOnly);
  }

  @Override
  public DoubleBuffer duplicate() {
    return new ByteBufferAsDoubleBuffer(bb, markValue(), position(), limit(), capacity(), byteOffset, bigEndian,
        isReadOnly);
  }

  @Override
  public DoubleBuffer asReadOnlyBuffer() {
    return new ByteBufferAsDoubleBuffer(bb, markValue(), position(), limit(), capacity(), byteOffset, bigEndian, true);
  }

  @Override
  public double get() {
    return Double.longBitsToDouble(bb.rawGetLong(ix(nextGetIndex()), bigEndian));
  }

  @Override
  public double get(int index) {
    return Double.longBitsToDouble(bb.rawGetLong(ix(checkIndex(index)), bigEndian));
  }

  @Override
  public DoubleBuffer put(double x) {
    if (isReadOnly) {
      throw new ReadOnlyBufferException();
    }
    bb.rawPutLong(ix(nextPutIndex()), Double.doubleToRawLongBits(x), bigEndian);
    return this;
  }

  @Override
  public DoubleBuffer put(int index, double x) {
    if (isReadOnly) {
      throw new ReadOnlyBufferException();
    }
    bb.rawPutLong(ix(checkIndex(index)), Double.doubleToRawLongBits(x), bigEndian);
    return this;
  }

  @Override
  public DoubleBuffer compact() {
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
