/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.nio;

import java.util.Objects;

class ByteBufferAsFloatBuffer extends FloatBuffer {
  private final ByteBuffer bb;
  private final int byteOffset;
  private final boolean bigEndian;

  ByteBufferAsFloatBuffer(ByteBuffer bb, int mark, int pos, int lim, int cap, int byteOffset, boolean bigEndian,
      boolean ro) {
    super(mark, pos, lim, cap);
    this.bb = bb;
    this.byteOffset = byteOffset;
    this.bigEndian = bigEndian;
    this.isReadOnly = ro;
  }

  @Override
  String bufferName() {
    return "java.nio.ByteBufferAsFloatBuffer" + (isReadOnly ? "R" : "") + (bigEndian ? "B" : "L");
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
  public FloatBuffer slice() {
    int pos = position();
    int lim = limit();
    int rem = pos <= lim ? lim - pos : 0;
    return new ByteBufferAsFloatBuffer(bb, -1, 0, rem, rem, ix(pos), bigEndian, isReadOnly);
  }

  @Override
  public FloatBuffer slice(int index, int length) {
    Objects.checkFromIndexSize(index, length, limit());
    return new ByteBufferAsFloatBuffer(bb, -1, 0, length, length, ix(index), bigEndian, isReadOnly);
  }

  @Override
  public FloatBuffer duplicate() {
    return new ByteBufferAsFloatBuffer(bb, markValue(), position(), limit(), capacity(), byteOffset, bigEndian,
        isReadOnly);
  }

  @Override
  public FloatBuffer asReadOnlyBuffer() {
    return new ByteBufferAsFloatBuffer(bb, markValue(), position(), limit(), capacity(), byteOffset, bigEndian, true);
  }

  @Override
  public float get() {
    return Float.intBitsToFloat(bb.rawGetInt(ix(nextGetIndex()), bigEndian));
  }

  @Override
  public float get(int index) {
    return Float.intBitsToFloat(bb.rawGetInt(ix(checkIndex(index)), bigEndian));
  }

  @Override
  public FloatBuffer put(float x) {
    if (isReadOnly) {
      throw new ReadOnlyBufferException();
    }
    bb.rawPutInt(ix(nextPutIndex()), Float.floatToRawIntBits(x), bigEndian);
    return this;
  }

  @Override
  public FloatBuffer put(int index, float x) {
    if (isReadOnly) {
      throw new ReadOnlyBufferException();
    }
    bb.rawPutInt(ix(checkIndex(index)), Float.floatToRawIntBits(x), bigEndian);
    return this;
  }

  @Override
  public FloatBuffer compact() {
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
