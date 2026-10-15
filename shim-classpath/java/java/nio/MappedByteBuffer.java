/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.nio;

import java.util.Objects;

public abstract class MappedByteBuffer extends ByteBuffer {

  protected MappedByteBuffer(boolean readOnly) {
    super(readOnly);
  }

  public final boolean isLoaded() {
    return true;
  }

  public final MappedByteBuffer load() {
    return this;
  }

  public final MappedByteBuffer force() {
    return this;
  }

  public final MappedByteBuffer force(int index, int length) {
    Objects.checkFromIndexSize(index, length, limit());
    return this;
  }

  @Override
  public MappedByteBuffer position(int newPosition) {
    super.position(newPosition);
    return this;
  }

  @Override
  public MappedByteBuffer limit(int newLimit) {
    super.limit(newLimit);
    return this;
  }

  @Override
  public MappedByteBuffer mark() {
    super.mark();
    return this;
  }

  @Override
  public MappedByteBuffer reset() {
    super.reset();
    return this;
  }

  @Override
  public MappedByteBuffer clear() {
    super.clear();
    return this;
  }

  @Override
  public MappedByteBuffer flip() {
    super.flip();
    return this;
  }

  @Override
  public MappedByteBuffer rewind() {
    super.rewind();
    return this;
  }
}
