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

package java.lang;

public final class StringBuilder extends AbstractStringBuilder
    implements java.io.Serializable, Comparable<StringBuilder>, CharSequence {
  private static final long serialVersionUID = 4383685877147921099L;
  private static final java.io.ObjectStreamField[] serialPersistentFields = new java.io.ObjectStreamField[0];

  public StringBuilder() {
    super(16);
  }

  public StringBuilder(int capacity) {
    super(capacity);
  }

  public StringBuilder(String str) {
    super(str);
  }

  public StringBuilder(CharSequence seq) {
    super(seq);
  }

  @Override
  public StringBuilder append(Object obj) {
    super.append(obj);
    return this;
  }

  @Override
  public StringBuilder append(String str) {
    super.append(str);
    return this;
  }

  @Override
  public StringBuilder append(StringBuffer sb) {
    super.append(sb);
    return this;
  }

  @Override
  public StringBuilder append(CharSequence s) {
    super.append(s);
    return this;
  }

  @Override
  public StringBuilder append(CharSequence s, int start, int end) {
    super.append(s, start, end);
    return this;
  }

  @Override
  public StringBuilder append(char[] str) {
    super.append(str);
    return this;
  }

  @Override
  public StringBuilder append(char[] str, int offset, int len) {
    super.append(str, offset, len);
    return this;
  }

  @Override
  public StringBuilder append(boolean b) {
    super.append(b);
    return this;
  }

  @Override
  public StringBuilder append(char c) {
    super.append(c);
    return this;
  }

  @Override
  public StringBuilder append(int i) {
    super.append(i);
    return this;
  }

  @Override
  public StringBuilder append(long lng) {
    super.append(lng);
    return this;
  }

  @Override
  public StringBuilder append(float f) {
    super.append(f);
    return this;
  }

  @Override
  public StringBuilder append(double d) {
    super.append(d);
    return this;
  }

  @Override
  public StringBuilder appendCodePoint(int codePoint) {
    super.appendCodePoint(codePoint);
    return this;
  }

  @Override
  public StringBuilder delete(int start, int end) {
    super.delete(start, end);
    return this;
  }

  @Override
  public StringBuilder deleteCharAt(int index) {
    super.deleteCharAt(index);
    return this;
  }

  @Override
  public StringBuilder replace(int start, int end, String str) {
    super.replace(start, end, str);
    return this;
  }

  @Override
  public StringBuilder insert(int index, char[] str, int offset, int len) {
    super.insert(index, str, offset, len);
    return this;
  }

  @Override
  public StringBuilder insert(int offset, Object obj) {
    super.insert(offset, obj);
    return this;
  }

  @Override
  public StringBuilder insert(int offset, String str) {
    super.insert(offset, str);
    return this;
  }

  @Override
  public StringBuilder insert(int offset, char[] str) {
    super.insert(offset, str);
    return this;
  }

  @Override
  public StringBuilder insert(int dstOffset, CharSequence s) {
    super.insert(dstOffset, s);
    return this;
  }

  @Override
  public StringBuilder insert(int dstOffset, CharSequence s, int start, int end) {
    super.insert(dstOffset, s, start, end);
    return this;
  }

  @Override
  public StringBuilder insert(int offset, boolean b) {
    super.insert(offset, b);
    return this;
  }

  @Override
  public StringBuilder insert(int offset, char c) {
    super.insert(offset, c);
    return this;
  }

  @Override
  public StringBuilder insert(int offset, int i) {
    super.insert(offset, i);
    return this;
  }

  @Override
  public StringBuilder insert(int offset, long l) {
    super.insert(offset, l);
    return this;
  }

  @Override
  public StringBuilder insert(int offset, float f) {
    super.insert(offset, f);
    return this;
  }

  @Override
  public StringBuilder insert(int offset, double d) {
    super.insert(offset, d);
    return this;
  }

  @Override
  public int indexOf(String str) {
    return super.indexOf(str);
  }

  @Override
  public int indexOf(String str, int fromIndex) {
    return super.indexOf(str, fromIndex);
  }

  @Override
  public int lastIndexOf(String str) {
    return super.lastIndexOf(str);
  }

  @Override
  public int lastIndexOf(String str, int fromIndex) {
    return super.lastIndexOf(str, fromIndex);
  }

  @Override
  public StringBuilder reverse() {
    super.reverse();
    return this;
  }

  @Override
  public StringBuilder repeat(int codePoint, int count) {
    super.repeat(codePoint, count);
    return this;
  }

  @Override
  public StringBuilder repeat(CharSequence cs, int count) {
    super.repeat(cs, count);
    return this;
  }

  @Override
  public int compareTo(StringBuilder another) {
    return compareToImpl(another);
  }

  @Override
  public String toString() {
    return new String(value, 0, count);
  }

  private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException {
    s.defaultWriteObject();
    s.writeInt(count);
    char[] val = new char[capacity()];
    System.arraycopy(value, 0, val, 0, count);
    s.writeObject(val);
  }

  private void readObject(java.io.ObjectInputStream s) throws java.io.IOException, ClassNotFoundException {
    s.defaultReadObject();
    count = s.readInt();
    value = (char[]) s.readObject();
  }
}
