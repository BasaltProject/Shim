/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.nio;

class StringCharBuffer extends CharBuffer {
  private final CharSequence str;

  StringCharBuffer(CharSequence s, int start, int end) {
    super(-1, start, end, s.length());
    int n = s.length();
    if (start < 0 || start > n || end < start || end > n) {
      throw new IndexOutOfBoundsException();
    }
    this.str = s;
    this.isReadOnly = true;
  }

  private StringCharBuffer(CharSequence s, int mark, int pos, int limit, int cap, int offset) {
    super(mark, pos, limit, cap, null, offset);
    this.str = s;
    this.isReadOnly = true;
  }

  @Override
  String bufferName() {
    return "java.nio.StringCharBuffer";
  }

  @Override
  public boolean isReadOnly() {
    return true;
  }

  @Override
  public boolean isDirect() {
    return false;
  }

  @Override
  public ByteOrder order() {
    return ByteOrder.nativeOrder();
  }

  @Override
  public CharBuffer slice() {
    int pos = position();
    int lim = limit();
    int rem = pos <= lim ? lim - pos : 0;
    return new StringCharBuffer(str, -1, 0, rem, rem, offset + pos);
  }

  @Override
  public CharBuffer slice(int index, int length) {
    java.util.Objects.checkFromIndexSize(index, length, limit());
    return new StringCharBuffer(str, -1, 0, length, length, offset + index);
  }

  @Override
  public CharBuffer duplicate() {
    return new StringCharBuffer(str, markValue(), position(), limit(), capacity(), offset);
  }

  @Override
  public CharBuffer asReadOnlyBuffer() {
    return duplicate();
  }

  @Override
  public char get() {
    return str.charAt(nextGetIndex() + offset);
  }

  @Override
  public char get(int index) {
    return str.charAt(checkIndex(index) + offset);
  }

  @Override
  public CharBuffer put(char c) {
    throw new ReadOnlyBufferException();
  }

  @Override
  public CharBuffer put(int index, char c) {
    throw new ReadOnlyBufferException();
  }

  @Override
  public CharBuffer compact() {
    throw new ReadOnlyBufferException();
  }

  @Override
  String toString(int start, int end) {
    return str.subSequence(start + offset, end + offset).toString();
  }

  @Override
  public CharBuffer subSequence(int start, int end) {
    int pos = position();
    if (start < 0 || end > remaining() || start > end) {
      throw new IndexOutOfBoundsException();
    }
    return new StringCharBuffer(str, -1, pos + start, pos + end, capacity(), offset);
  }
}
