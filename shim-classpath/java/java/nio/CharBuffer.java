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

import java.io.IOException;
import java.util.Objects;

public abstract class CharBuffer extends Buffer implements Comparable<CharBuffer>, Appendable, CharSequence, Readable {
  final char[] hb;
  final int offset;
  boolean isReadOnly;

  CharBuffer(int mark, int pos, int lim, int cap, char[] hb, int offset) {
    super(mark, pos, lim, cap);
    this.hb = hb;
    this.offset = offset;
  }

  CharBuffer(int mark, int pos, int lim, int cap) {
    this(mark, pos, lim, cap, null, 0);
  }

  public static CharBuffer allocate(int capacity) {
    if (capacity < 0) {
      throw createCapacityException(capacity);
    }
    return new HeapCharBuffer(capacity, capacity);
  }

  public static CharBuffer wrap(char[] array, int offset, int length) {
    try {
      return new HeapCharBuffer(array, offset, length);
    } catch (IllegalArgumentException x) {
      throw new IndexOutOfBoundsException();
    }
  }

  public static CharBuffer wrap(char[] array) {
    return wrap(array, 0, array.length);
  }

  @Override
  public abstract CharBuffer slice();

  @Override
  public abstract CharBuffer slice(int index, int length);

  @Override
  public abstract CharBuffer duplicate();

  public abstract CharBuffer asReadOnlyBuffer();

  public abstract char get();

  public abstract CharBuffer put(char x);

  public abstract char get(int index);

  public abstract CharBuffer put(int index, char x);

  public CharBuffer get(char[] dst, int offset, int length) {
    Objects.checkFromIndexSize(offset, length, dst.length);
    int pos = position();
    if (length > limit() - pos) {
      throw new BufferUnderflowException();
    }
    for (int i = offset; i < offset + length; i++) {
      dst[i] = get();
    }
    return this;
  }

  public CharBuffer get(char[] dst) {
    return get(dst, 0, dst.length);
  }

  public CharBuffer get(int index, char[] dst, int offset, int length) {
    Objects.checkFromIndexSize(index, length, limit());
    Objects.checkFromIndexSize(offset, length, dst.length);
    for (int i = offset, j = index; i < offset + length; i++, j++) {
      dst[i] = get(j);
    }
    return this;
  }

  public CharBuffer get(int index, char[] dst) {
    return get(index, dst, 0, dst.length);
  }

  public CharBuffer put(CharBuffer src) {
    if (src == this) {
      throw createSameBufferException();
    }
    if (isReadOnly()) {
      throw new ReadOnlyBufferException();
    }
    int srcPos = src.position();
    int srcLim = src.limit();
    int srcRem = srcPos <= srcLim ? srcLim - srcPos : 0;
    int pos = position();
    int lim = limit();
    int rem = pos <= lim ? lim - pos : 0;
    if (srcRem > rem) {
      throw new BufferOverflowException();
    }
    char[] tmp = new char[srcRem];
    for (int i = 0; i < srcRem; i++) {
      tmp[i] = src.get(srcPos + i);
    }
    for (int i = 0; i < srcRem; i++) {
      put(pos + i, tmp[i]);
    }
    src.position(srcPos + srcRem);
    position(pos + srcRem);
    return this;
  }

  public CharBuffer put(int index, CharBuffer src, int offset, int length) {
    Objects.checkFromIndexSize(index, length, limit());
    Objects.checkFromIndexSize(offset, length, src.limit());
    if (isReadOnly()) {
      throw new ReadOnlyBufferException();
    }
    char[] tmp = new char[length];
    for (int i = 0; i < length; i++) {
      tmp[i] = src.get(offset + i);
    }
    for (int i = 0; i < length; i++) {
      put(index + i, tmp[i]);
    }
    return this;
  }

  public CharBuffer put(char[] src, int offset, int length) {
    if (isReadOnly()) {
      throw new ReadOnlyBufferException();
    }
    Objects.checkFromIndexSize(offset, length, src.length);
    int pos = position();
    if (length > limit() - pos) {
      throw new BufferOverflowException();
    }
    for (int i = offset; i < offset + length; i++) {
      put(src[i]);
    }
    return this;
  }

  public final CharBuffer put(char[] src) {
    return put(src, 0, src.length);
  }

  public CharBuffer put(int index, char[] src, int offset, int length) {
    if (isReadOnly()) {
      throw new ReadOnlyBufferException();
    }
    Objects.checkFromIndexSize(index, length, limit());
    Objects.checkFromIndexSize(offset, length, src.length);
    for (int i = offset, j = index; i < offset + length; i++, j++) {
      put(j, src[i]);
    }
    return this;
  }

  public CharBuffer put(int index, char[] src) {
    return put(index, src, 0, src.length);
  }

  @Override
  public final boolean hasArray() {
    return hb != null && !isReadOnly;
  }

  @Override
  public final char[] array() {
    if (hb == null) {
      throw new UnsupportedOperationException();
    }
    if (isReadOnly) {
      throw new ReadOnlyBufferException();
    }
    return hb;
  }

  @Override
  public final int arrayOffset() {
    if (hb == null) {
      throw new UnsupportedOperationException();
    }
    if (isReadOnly) {
      throw new ReadOnlyBufferException();
    }
    return offset;
  }

  @Override
  public final CharBuffer position(int newPosition) {
    super.position(newPosition);
    return this;
  }

  @Override
  public final CharBuffer limit(int newLimit) {
    super.limit(newLimit);
    return this;
  }

  @Override
  public final CharBuffer mark() {
    super.mark();
    return this;
  }

  @Override
  public final CharBuffer reset() {
    super.reset();
    return this;
  }

  @Override
  public final CharBuffer clear() {
    super.clear();
    return this;
  }

  @Override
  public final CharBuffer flip() {
    super.flip();
    return this;
  }

  @Override
  public final CharBuffer rewind() {
    super.rewind();
    return this;
  }

  public abstract CharBuffer compact();

  @Override
  public abstract boolean isDirect();

  public abstract ByteOrder order();

  @Override
  public int hashCode() {
    int h = 1;
    int p = position();
    for (int i = limit() - 1; i >= p; i--) {
      h = 31 * h + (int) get(i);
    }
    return h;
  }

  private static boolean same(char x, char y) {
    return x == y;
  }

  private static int compare(char x, char y) {
    return Character.compare(x, y);
  }

  private static int mismatch(CharBuffer a, int aPos, CharBuffer b, int bPos, int length) {
    for (int i = 0; i < length; i++) {
      if (!same(a.get(aPos + i), b.get(bPos + i))) {
        return i;
      }
    }
    return -1;
  }

  @Override
  public boolean equals(Object ob) {
    if (this == ob) {
      return true;
    }
    if (!(ob instanceof CharBuffer)) {
      return false;
    }
    CharBuffer that = (CharBuffer) ob;
    int thisPos = this.position();
    int thisRem = this.limit() - thisPos;
    int thatPos = that.position();
    int thatRem = that.limit() - thatPos;
    if (thisRem < 0 || thisRem != thatRem) {
      return false;
    }
    return mismatch(this, thisPos, that, thatPos, thisRem) < 0;
  }

  @Override
  public int compareTo(CharBuffer that) {
    int thisPos = this.position();
    int thisRem = this.limit() - thisPos;
    int thatPos = that.position();
    int thatRem = that.limit() - thatPos;
    int length = Math.min(thisRem, thatRem);
    if (length < 0) {
      return -1;
    }
    int i = mismatch(this, thisPos, that, thatPos, length);
    if (i >= 0) {
      return compare(this.get(thisPos + i), that.get(thatPos + i));
    }
    return thisRem - thatRem;
  }

  public int mismatch(CharBuffer that) {
    int thisPos = this.position();
    int thisRem = this.limit() - thisPos;
    int thatPos = that.position();
    int thatRem = that.limit() - thatPos;
    int length = Math.min(thisRem, thatRem);
    if (length < 0) {
      return -1;
    }
    int r = mismatch(this, thisPos, that, thatPos, length);
    return (r == -1 && thisRem != thatRem) ? length : r;
  }

  public static CharBuffer wrap(CharSequence csq, int start, int end) {
    try {
      return new StringCharBuffer(csq, start, end);
    } catch (IllegalArgumentException x) {
      throw new IndexOutOfBoundsException();
    }
  }

  public static CharBuffer wrap(CharSequence csq) {
    return wrap(csq, 0, csq.length());
  }

  public int read(CharBuffer target) throws IOException {
    int targetRemaining = target.remaining();
    int remaining = remaining();
    if (remaining == 0) {
      return -1;
    }
    int n = Math.min(remaining, targetRemaining);
    int limit = limit();
    if (targetRemaining < remaining) {
      limit(position() + n);
    }
    try {
      if (n > 0) {
        target.put(this);
      }
    } finally {
      limit(limit);
    }
    return n;
  }

  public final CharBuffer put(String src, int start, int end) {
    Objects.checkFromToIndex(start, end, src.length());
    if (isReadOnly()) {
      throw new ReadOnlyBufferException();
    }
    int length = end - start;
    int pos = position();
    int lim = limit();
    int rem = pos <= lim ? lim - pos : 0;
    if (length > rem) {
      throw new BufferOverflowException();
    }
    for (int i = start; i < end; i++) {
      put(src.charAt(i));
    }
    return this;
  }

  public final CharBuffer put(String src) {
    return put(src, 0, src.length());
  }

  @Override
  public String toString() {
    return toString(position(), limit());
  }

  abstract String toString(int start, int end);

  @Override
  public final int length() {
    return remaining();
  }

  @Override
  public final boolean isEmpty() {
    return remaining() == 0;
  }

  @Override
  public final char charAt(int index) {
    return get(position() + checkIndex(index, 1));
  }

  @Override
  public void getChars(int srcBegin, int srcEnd, char[] dst, int dstBegin) {
    Objects.checkFromToIndex(srcBegin, srcEnd, length());
    Objects.checkFromIndexSize(dstBegin, srcEnd - srcBegin, dst.length);
    get(position() + srcBegin, dst, dstBegin, srcEnd - srcBegin);
  }

  @Override
  public abstract CharBuffer subSequence(int start, int end);

  @Override
  public CharBuffer append(CharSequence csq) {
    if (csq == null) {
      return put("null");
    }
    return put(csq.toString());
  }

  @Override
  public CharBuffer append(CharSequence csq, int start, int end) {
    CharSequence cs = csq == null ? "null" : csq;
    return put(cs.subSequence(start, end).toString());
  }

  @Override
  public CharBuffer append(char c) {
    return put(c);
  }
}
