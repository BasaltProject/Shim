/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.text;

import java.text.CharacterIterator;

public class Segment implements Cloneable, CharacterIterator, CharSequence {
  public char[] array;
  public int offset;
  public int count;
  private boolean partial;
  private int pos;

  public Segment() { this(null, 0, 0); }

  public Segment(char[] array, int offset, int count) {
    this.array = array;
    this.offset = offset;
    this.count = count;
    this.partial = false;
  }

  public void setPartialReturn(boolean p) { partial = p; }

  public boolean isPartialReturn() { return partial; }

  public String toString() { return array != null ? new String(array, offset, count) : ""; }

  public char first() {
    pos = offset;
    return count != 0 ? array[pos] : DONE;
  }

  public char last() {
    pos = offset + count;
    if (count != 0) {
      pos -= 1;
      return array[pos];
    }
    return DONE;
  }

  public char current() { return count != 0 && pos < offset + count ? array[pos] : DONE; }

  public char next() {
    pos += 1;
    int end = offset + count;
    if (pos >= end) {
      pos = end;
      return DONE;
    }
    return current();
  }

  public char previous() {
    if (pos == offset) {
      return DONE;
    }
    pos -= 1;
    return current();
  }

  public char setIndex(int position) {
    int end = offset + count;
    if (position < offset || position > end) {
      throw new IllegalArgumentException("bad position: " + position);
    }
    pos = position;
    return position != end ? array[pos] : DONE;
  }

  public int getBeginIndex() { return offset; }

  public int getEndIndex() { return offset + count; }

  public int getIndex() { return pos; }

  public Object clone() {
    try {
      Segment s = (Segment) super.clone();
      s.array = array == null ? null : array.clone();
      return s;
    } catch (CloneNotSupportedException e) {
      throw new InternalError(e);
    }
  }

  public char charAt(int index) {
    if (index < 0 || index >= count) {
      throw new StringIndexOutOfBoundsException(index);
    }
    return array[offset + index];
  }

  public int length() { return count; }

  public CharSequence subSequence(int start, int end) {
    if (start < 0) {
      throw new StringIndexOutOfBoundsException(start);
    }
    if (end > count) {
      throw new StringIndexOutOfBoundsException(end);
    }
    if (start > end) {
      throw new StringIndexOutOfBoundsException(end - start);
    }
    Segment segment = new Segment();
    segment.array = this.array;
    segment.offset = this.offset + start;
    segment.count = end - start;
    return segment;
  }
}
