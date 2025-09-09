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

public abstract class CharBuffer
  extends Buffer
  implements Comparable<CharBuffer>, CharSequence, Appendable, Readable
{
  private final boolean readOnly;

  protected CharBuffer(boolean readOnly) {
    this.readOnly = readOnly;
  }

  public static CharBuffer allocate(int capacity) {
    return new ArrayCharBuffer(new char[capacity], 0, capacity, false);
  }

  public static CharBuffer wrap(char[] array) {
    return wrap(array, 0, array.length);
  }

  public static CharBuffer wrap(char[] array, int offset, int length) {
    return new ArrayCharBuffer(array, offset, length, false);
  }

  public static CharBuffer wrap(CharSequence csq) {
    return wrap(csq, 0, csq.length());
  }

  public static CharBuffer wrap(CharSequence csq, int start, int end) {
    if (csq == null) throw new NullPointerException();
    char[] array = new char[end - start];
    for (int i = start; i < end; ++i) {
      array[i - start] = csq.charAt(i);
    }
    return new ArrayCharBuffer(array, 0, array.length, true);
  }

  public abstract CharBuffer asReadOnlyBuffer();

  public abstract CharBuffer slice();

  public abstract CharBuffer slice(int index, int length);

  public abstract CharBuffer duplicate();

  public abstract CharBuffer subSequence(int start, int end);

  protected abstract void doPut(int offset, char value);

  public abstract CharBuffer put(char[] src, int offset, int length);

  protected abstract char doGet(int offset);

  public abstract CharBuffer get(char[] dst, int offset, int length);

  public boolean hasArray() {
    return false;
  }

  public CharBuffer compact() {
    int remaining = remaining();

    if (position != 0) {
      CharBuffer b = slice();
      position = 0;
      put(b);
    }

    position = remaining;
    limit(capacity());
    
    return this;
  }

  public CharBuffer put(CharBuffer src) {
    if (src.hasArray()) {
      checkPut(position, src.remaining(), false);

      put(src.array(), src.arrayOffset() + src.position, src.remaining());
      src.position(src.position() + src.remaining());

      return this;
    } else {
      char[] buffer = new char[src.remaining()];
      src.get(buffer);
      return put(buffer);
    }
  }

  public int compareTo(CharBuffer o) {
    int end = (remaining() < o.remaining() ? remaining() : o.remaining());

    for (int i = 0; i < end; ++i) {
      int d = get(position + i) - o.get(o.position + i);
      if (d != 0) {
        return d;
      }
    }
    return remaining() - o.remaining();
  }

  public boolean equals(Object o) {
    return o instanceof CharBuffer && compareTo((CharBuffer) o) == 0;
  }

  public char[] array() {
    throw new UnsupportedOperationException();
  }

  public int arrayOffset() {
    throw new UnsupportedOperationException();
  }

  public CharBuffer put(int offset, char val) {
    checkPut(offset, 1, true);
    doPut(offset, val);
    return this;
  }

  public CharBuffer put(char val) {
    put(position, val);
    ++ position;
    return this;
  }

  public CharBuffer put(char[] arr) {
    return put(arr, 0, arr.length);
  }

  public char get() {
    checkGet(position, 1, false);
    return doGet(position++);
  }

  public char get(int position) {
    checkGet(position, 1, true);
    return doGet(position);
  }

  public CharBuffer get(char[] dst) {
    return get(dst, 0, dst.length);
  }

  protected void checkPut(int position, int amount, boolean absolute) {
    if (readOnly) {
      throw new ReadOnlyBufferException();
    }

    if (position < 0 || position+amount > limit) {
      throw absolute
        ? new IndexOutOfBoundsException()
        : new BufferOverflowException();
    }
  }

  protected void checkGet(int position, int amount, boolean absolute) {
    if (amount > limit-position) {
      throw absolute
        ? new IndexOutOfBoundsException()
        : new BufferUnderflowException();
    }
  }

  @Override
  public CharBuffer position(int newPosition) {
    super.position(newPosition);
    return this;
  }

  @Override
  public CharBuffer limit(int newLimit) {
    super.limit(newLimit);
    return this;
  }

  @Override
  public CharBuffer mark() {
    super.mark();
    return this;
  }

  @Override
  public CharBuffer reset() {
    super.reset();
    return this;
  }

  @Override
  public CharBuffer clear() {
    super.clear();
    return this;
  }

  @Override
  public CharBuffer flip() {
    super.flip();
    return this;
  }

  @Override
  public CharBuffer rewind() {
    super.rewind();
    return this;
  }

  public final int length() {
    return remaining();
  }

  public final boolean isEmpty() {
    return remaining() == 0;
  }

  public final char charAt(int index) {
    if (index < 0 || index >= remaining()) {
      throw new IndexOutOfBoundsException();
    }
    return get(position + index);
  }

  public CharBuffer append(CharSequence csq) {
    if (csq == null) return put("null");
    return put(csq.toString());
  }

  public CharBuffer append(CharSequence csq, int start, int end) {
    CharSequence s = (csq == null ? "null" : csq).subSequence(start, end);
    return put(s.toString());
  }

  public CharBuffer append(char c) {
    return put(c);
  }

  public int read(CharBuffer cb) {
    int targetRemaining = cb.remaining();
    int remaining = remaining();
    if (remaining == 0) return -1;
    int n = Math.min(remaining, targetRemaining);
    int lim = limit();
    limit(position() + n);
    cb.put(this);
    limit(lim);
    return n;
  }

  public CharBuffer put(String src, int start, int end) {
    int length = end - start;
    checkPut(position, length, false);
    for (int i = start; i < end; ++i) {
      put(src.charAt(i));
    }
    return this;
  }

  public final CharBuffer put(String src) {
    return put(src, 0, src.length());
  }

  public int mismatch(CharBuffer that) {
    int length = Math.min(this.remaining(), that.remaining());
    int p1 = this.position();
    int p2 = that.position();
    for (int i = 0; i < length; ++i) {
      if (this.get(p1 + i) != that.get(p2 + i)) {
        return i;
      }
    }
    return this.remaining() != that.remaining() ? length : -1;
  }

  public CharBuffer get(int index, char[] dst, int offset, int length) {
    java.util.Objects.checkFromIndexSize(index, length, limit());
    java.util.Objects.checkFromIndexSize(offset, length, dst.length);
    for (int i = 0; i < length; ++i) {
      dst[offset + i] = get(index + i);
    }
    return this;
  }

  public CharBuffer get(int index, char[] dst) {
    return get(index, dst, 0, dst.length);
  }

  public CharBuffer put(int index, char[] src, int offset, int length) {
    if (readOnly) throw new ReadOnlyBufferException();
    java.util.Objects.checkFromIndexSize(index, length, limit());
    java.util.Objects.checkFromIndexSize(offset, length, src.length);
    for (int i = 0; i < length; ++i) {
      put(index + i, src[offset + i]);
    }
    return this;
  }

  public CharBuffer put(int index, char[] src) {
    return put(index, src, 0, src.length);
  }
}
