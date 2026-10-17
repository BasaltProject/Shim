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

public abstract class MappedByteBuffer extends ByteBuffer {

  MappedByteBuffer(int mark, int pos, int lim, int cap) {
    super(mark, pos, lim, cap);
  }

  @Override
  public abstract MappedByteBuffer slice();

  @Override
  public abstract MappedByteBuffer slice(int index, int length);

  @Override
  public abstract MappedByteBuffer duplicate();

  @Override
  public abstract MappedByteBuffer compact();

  public final boolean isLoaded() {
    return true;
  }

  public final MappedByteBuffer load() {
    return this;
  }

  public final MappedByteBuffer force() {
    if (this instanceof DirectByteBuffer) {
      ((DirectByteBuffer) this).forceMapped();
    }
    return this;
  }

  public final MappedByteBuffer force(int index, int length) {
    Objects.checkFromIndexSize(index, length, limit());
    if (this instanceof DirectByteBuffer) {
      ((DirectByteBuffer) this).forceMapped();
    }
    return this;
  }

  @Override
  public final MappedByteBuffer position(int newPosition) {
    super.position(newPosition);
    return this;
  }

  @Override
  public final MappedByteBuffer limit(int newLimit) {
    super.limit(newLimit);
    return this;
  }

  @Override
  public final MappedByteBuffer mark() {
    super.mark();
    return this;
  }

  @Override
  public final MappedByteBuffer reset() {
    super.reset();
    return this;
  }

  @Override
  public final MappedByteBuffer clear() {
    super.clear();
    return this;
  }

  @Override
  public final MappedByteBuffer flip() {
    super.flip();
    return this;
  }

  @Override
  public final MappedByteBuffer rewind() {
    super.rewind();
    return this;
  }
}
