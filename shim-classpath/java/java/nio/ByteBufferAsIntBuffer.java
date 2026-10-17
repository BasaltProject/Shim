/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.nio;

import java.util.Objects;

class ByteBufferAsIntBuffer extends IntBuffer {
  private final ByteBuffer bb;
  private final int byteOffset;
  private final boolean bigEndian;

  ByteBufferAsIntBuffer(ByteBuffer bb, int mark, int pos, int lim, int cap, int byteOffset, boolean bigEndian,
      boolean ro) {
    super(mark, pos, lim, cap);
    this.bb = bb;
    this.byteOffset = byteOffset;
    this.bigEndian = bigEndian;
    this.isReadOnly = ro;
  }

  @Override
  String bufferName() {
    return "java.nio.ByteBufferAsIntBuffer" + (isReadOnly ? "R" : "") + (bigEndian ? "B" : "L");
  }

  private int ix(int i) {
    return (i << 2) + byteOffset;
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
  public IntBuffer slice() {
    int pos = position();
    int lim = limit();
    int rem = pos <= lim ? lim - pos : 0;
    return new ByteBufferAsIntBuffer(bb, -1, 0, rem, rem, ix(pos), bigEndian, isReadOnly);
  }

  @Override
  public IntBuffer slice(int index, int length) {
    Objects.checkFromIndexSize(index, length, limit());
    return new ByteBufferAsIntBuffer(bb, -1, 0, length, length, ix(index), bigEndian, isReadOnly);
  }

  @Override
  public IntBuffer duplicate() {
    return new ByteBufferAsIntBuffer(bb, markValue(), position(), limit(), capacity(), byteOffset, bigEndian,
        isReadOnly);
  }

  @Override
  public IntBuffer asReadOnlyBuffer() {
    return new ByteBufferAsIntBuffer(bb, markValue(), position(), limit(), capacity(), byteOffset, bigEndian, true);
  }

  @Override
  public int get() {
    return bb.rawGetInt(ix(nextGetIndex()), bigEndian);
  }

  @Override
  public int get(int index) {
    return bb.rawGetInt(ix(checkIndex(index)), bigEndian);
  }

  @Override
  public IntBuffer put(int x) {
    if (isReadOnly) {
      throw new ReadOnlyBufferException();
    }
    bb.rawPutInt(ix(nextPutIndex()), x, bigEndian);
    return this;
  }

  @Override
  public IntBuffer put(int index, int x) {
    if (isReadOnly) {
      throw new ReadOnlyBufferException();
    }
    bb.rawPutInt(ix(checkIndex(index)), x, bigEndian);
    return this;
  }

  @Override
  public IntBuffer compact() {
    if (isReadOnly) {
      throw new ReadOnlyBufferException();
    }
    int pos = position();
    int lim = limit();
    int rem = pos <= lim ? lim - pos : 0;
    for (int i = 0; i < rem; i++) {
      bb.rawPutInt(ix(i), bb.rawGetInt(ix(pos + i), bigEndian), bigEndian);
    }
    position(rem);
    limit(capacity());
    discardMark();
    return this;
  }
}
