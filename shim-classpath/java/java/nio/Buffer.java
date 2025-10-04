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

public abstract class Buffer {
  int mark = -1;
  int position;
  int limit;
  int capacity;

  Buffer(int mark, int pos, int lim, int cap) {
    if (cap < 0) {
      throw createCapacityException(cap);
    }
    this.capacity = cap;
    limit(lim);
    position(pos);
    if (mark >= 0) {
      if (mark > pos) {
        throw new IllegalArgumentException("mark > position: (" + mark + " > " + pos + ")");
      }
      this.mark = mark;
    }
  }

  static IllegalArgumentException createSameBufferException() {
    return new IllegalArgumentException("The source buffer is this buffer");
  }

  static IllegalArgumentException createCapacityException(int capacity) {
    return new IllegalArgumentException("capacity < 0: (" + capacity + " < 0)");
  }

  String bufferName() {
    return getClass().getName();
  }

  public final int capacity() {
    return capacity;
  }

  public final int position() {
    return position;
  }

  public Buffer position(int newPosition) {
    if (newPosition > limit | newPosition < 0) {
      throw new IllegalArgumentException(newPosition > limit
          ? "newPosition > limit: (" + newPosition + " > " + limit + ")"
          : "newPosition < 0: (" + newPosition + " < 0)");
    }
    if (mark > newPosition) {
      mark = -1;
    }
    position = newPosition;
    return this;
  }

  public final int limit() {
    return limit;
  }

  public Buffer limit(int newLimit) {
    if (newLimit > capacity | newLimit < 0) {
      throw new IllegalArgumentException(newLimit > capacity
          ? "newLimit > capacity: (" + newLimit + " > " + capacity + ")"
          : "newLimit < 0: (" + newLimit + " < 0)");
    }
    limit = newLimit;
    if (position > newLimit) {
      position = newLimit;
    }
    if (mark > newLimit) {
      mark = -1;
    }
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

  public final int remaining() {
    int rem = limit - position;
    return rem > 0 ? rem : 0;
  }

  public final boolean hasRemaining() {
    return position < limit;
  }

  public abstract boolean isReadOnly();

  public abstract boolean hasArray();

  public abstract Object array();

  public abstract int arrayOffset();

  public abstract boolean isDirect();

  public abstract Buffer slice();

  public abstract Buffer slice(int index, int length);

  public abstract Buffer duplicate();

  final int nextGetIndex() {
    int p = position;
    if (p >= limit) {
      throw new BufferUnderflowException();
    }
    position = p + 1;
    return p;
  }

  final int nextGetIndex(int nb) {
    int p = position;
    if (limit - p < nb) {
      throw new BufferUnderflowException();
    }
    position = p + nb;
    return p;
  }

  final int nextPutIndex() {
    int p = position;
    if (p >= limit) {
      throw new BufferOverflowException();
    }
    position = p + 1;
    return p;
  }

  final int nextPutIndex(int nb) {
    int p = position;
    if (limit - p < nb) {
      throw new BufferOverflowException();
    }
    position = p + nb;
    return p;
  }

  final int checkIndex(int i) {
    if (i < 0 || i >= limit) {
      throw new IndexOutOfBoundsException();
    }
    return i;
  }

  final int checkIndex(int i, int nb) {
    if (i < 0 || nb > limit - i) {
      throw new IndexOutOfBoundsException();
    }
    return i;
  }

  final int markValue() {
    return mark;
  }

  final void discardMark() {
    mark = -1;
  }
}
