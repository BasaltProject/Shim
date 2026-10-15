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

/* Copyright (c) 2008-2015, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.nio;

public abstract class Buffer {
  protected int capacity;
  protected int position;
  protected int limit;
  protected int mark = -1;
  protected boolean readonly;

  public final int limit() {
    return limit;
  }

  public final int remaining() {
    return limit - position;
  }

  public final int position() {
    return position;
  }

  public final int capacity() {
    return capacity;
  }

  public Buffer limit(int newLimit) {
    if (newLimit < 0 || newLimit > capacity) {
      throw new IllegalArgumentException();
    }
    limit = newLimit;
    if (position > limit) position = limit;
    if (mark > limit) mark = -1;
    return this;
  }

  public Buffer position(int newPosition) {
    if (newPosition < 0 || newPosition > limit) {
      throw new IllegalArgumentException();
    }
    position = newPosition;
    if (mark > position) mark = -1;
    return this;
  }

  public Buffer mark() {
    mark = position;
    return this;
  }

  public Buffer reset() {
    int m = mark;
    if (m < 0) {
      throw new InvalidMarkException();
    }
    position = m;
    return this;
  }

  public final boolean hasRemaining() {
    return remaining() > 0;
  }

  public Buffer clear() {
    position = 0;
    limit = capacity;
    mark = -1;
    return this;
  }

  public Buffer flip() {
    limit = position;
    position = 0;
    mark = -1;
    return this;
  }

  public Buffer rewind() {
    position = 0;
    mark = -1;
    return this;
  }

  public boolean isReadOnly() {
    return readonly;
  }

  public boolean hasArray() {
    return false;
  }

  public Object array() {
    throw new UnsupportedOperationException();
  }

  public int arrayOffset() {
    throw new UnsupportedOperationException();
  }

  public abstract boolean isDirect();

  public abstract Buffer slice();

  public abstract Buffer slice(int index, int length);

  public abstract Buffer duplicate();
}

