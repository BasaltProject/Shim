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

public final class StringBuffer extends AbstractStringBuilder
    implements java.io.Serializable, Comparable<StringBuffer>, CharSequence {
  private static final long serialVersionUID = 3388685877147921107L;
  private static final java.io.ObjectStreamField[] serialPersistentFields = {
    new java.io.ObjectStreamField("value", char[].class),
    new java.io.ObjectStreamField("count", Integer.TYPE),
    new java.io.ObjectStreamField("shared", Boolean.TYPE)
  };

  public StringBuffer() {
    super(16);
  }

  public StringBuffer(int capacity) {
    super(capacity);
  }

  public StringBuffer(String str) {
    super(str);
  }

  public StringBuffer(CharSequence seq) {
    super(seq);
  }

  @Override
  public synchronized StringBuffer append(Object obj) {
    super.append(obj);
    return this;
  }

  @Override
  public synchronized StringBuffer append(String str) {
    super.append(str);
    return this;
  }

  @Override
  public synchronized StringBuffer append(StringBuffer sb) {
    super.append(sb);
    return this;
  }

  @Override
  public synchronized StringBuffer append(CharSequence s) {
    super.append(s);
    return this;
  }

  @Override
  public synchronized StringBuffer append(CharSequence s, int start, int end) {
    super.append(s, start, end);
    return this;
  }

  @Override
  public synchronized StringBuffer append(char[] str) {
    super.append(str);
    return this;
  }

  @Override
  public synchronized StringBuffer append(char[] str, int offset, int len) {
    super.append(str, offset, len);
    return this;
  }

  @Override
  public synchronized StringBuffer append(boolean b) {
    super.append(b);
    return this;
  }

  @Override
  public synchronized StringBuffer append(char c) {
    super.append(c);
    return this;
  }

  @Override
  public synchronized StringBuffer append(int i) {
    super.append(i);
    return this;
  }

  @Override
  public synchronized StringBuffer append(long lng) {
    super.append(lng);
    return this;
  }

  @Override
  public synchronized StringBuffer append(float f) {
    super.append(f);
    return this;
  }

  @Override
  public synchronized StringBuffer append(double d) {
    super.append(d);
    return this;
  }

  @Override
  public synchronized StringBuffer appendCodePoint(int codePoint) {
    super.appendCodePoint(codePoint);
    return this;
  }

  @Override
  public synchronized StringBuffer delete(int start, int end) {
    super.delete(start, end);
    return this;
  }

  @Override
  public synchronized StringBuffer deleteCharAt(int index) {
    super.deleteCharAt(index);
    return this;
  }

  @Override
  public synchronized StringBuffer replace(int start, int end, String str) {
    super.replace(start, end, str);
    return this;
  }

  @Override
  public synchronized StringBuffer insert(int index, char[] str, int offset, int len) {
    super.insert(index, str, offset, len);
    return this;
  }

  @Override
  public synchronized StringBuffer insert(int offset, Object obj) {
    super.insert(offset, obj);
    return this;
  }

  @Override
  public synchronized StringBuffer insert(int offset, String str) {
    super.insert(offset, str);
    return this;
  }

  @Override
  public synchronized StringBuffer insert(int offset, char[] str) {
    super.insert(offset, str);
    return this;
  }

  @Override
  public synchronized StringBuffer insert(int dstOffset, CharSequence s) {
    super.insert(dstOffset, s);
    return this;
  }

  @Override
  public synchronized StringBuffer insert(int dstOffset, CharSequence s, int start, int end) {
    super.insert(dstOffset, s, start, end);
    return this;
  }

  @Override
  public synchronized StringBuffer insert(int offset, boolean b) {
    super.insert(offset, b);
    return this;
  }

  @Override
  public synchronized StringBuffer insert(int offset, char c) {
    super.insert(offset, c);
    return this;
  }

  @Override
  public synchronized StringBuffer insert(int offset, int i) {
    super.insert(offset, i);
    return this;
  }

  @Override
  public synchronized StringBuffer insert(int offset, long l) {
    super.insert(offset, l);
    return this;
  }

  @Override
  public synchronized StringBuffer insert(int offset, float f) {
    super.insert(offset, f);
    return this;
  }

  @Override
  public synchronized StringBuffer insert(int offset, double d) {
    super.insert(offset, d);
    return this;
  }

  @Override
  public synchronized int indexOf(String str) {
    return super.indexOf(str);
  }

  @Override
  public synchronized int indexOf(String str, int fromIndex) {
    return super.indexOf(str, fromIndex);
  }

  @Override
  public synchronized int lastIndexOf(String str) {
    return super.lastIndexOf(str);
  }

  @Override
  public synchronized int lastIndexOf(String str, int fromIndex) {
    return super.lastIndexOf(str, fromIndex);
  }

  @Override
  public synchronized StringBuffer reverse() {
    super.reverse();
    return this;
  }

  @Override
  public synchronized StringBuffer repeat(int codePoint, int count) {
    super.repeat(codePoint, count);
    return this;
  }

  @Override
  public synchronized StringBuffer repeat(CharSequence cs, int count) {
    super.repeat(cs, count);
    return this;
  }

  @Override
  public synchronized int compareTo(StringBuffer another) {
    return compareToImpl(another);
  }

  @Override
  public synchronized String toString() {
    return new String(value, 0, count);
  }


  @Override
  public synchronized int length() {
    return super.length();
  }

  @Override
  public synchronized int capacity() {
    return super.capacity();
  }

  @Override
  public synchronized void ensureCapacity(int minimumCapacity) {
    super.ensureCapacity(minimumCapacity);
  }

  @Override
  public synchronized void trimToSize() {
    super.trimToSize();
  }

  @Override
  public synchronized void setLength(int newLength) {
    super.setLength(newLength);
  }

  @Override
  public synchronized char charAt(int index) {
    return super.charAt(index);
  }

  @Override
  public synchronized int codePointAt(int index) {
    return super.codePointAt(index);
  }

  @Override
  public synchronized int codePointBefore(int index) {
    return super.codePointBefore(index);
  }

  @Override
  public synchronized int codePointCount(int beginIndex, int endIndex) {
    return super.codePointCount(beginIndex, endIndex);
  }

  @Override
  public synchronized int offsetByCodePoints(int index, int codePointOffset) {
    return super.offsetByCodePoints(index, codePointOffset);
  }

  @Override
  public synchronized void getChars(int srcBegin, int srcEnd, char[] dst, int dstBegin) {
    super.getChars(srcBegin, srcEnd, dst, dstBegin);
  }

  @Override
  public synchronized void setCharAt(int index, char ch) {
    super.setCharAt(index, ch);
  }

  @Override
  public synchronized String substring(int start) {
    return super.substring(start);
  }

  @Override
  public synchronized CharSequence subSequence(int start, int end) {
    return super.substring(start, end);
  }

  @Override
  public synchronized String substring(int start, int end) {
    return super.substring(start, end);
  }

  private synchronized void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException {
    java.io.ObjectOutputStream.PutField fields = s.putFields();
    char[] val = new char[capacity()];
    System.arraycopy(value, 0, val, 0, count);
    fields.put("value", val);
    fields.put("count", count);
    fields.put("shared", false);
    s.writeFields();
  }

  private void readObject(java.io.ObjectInputStream s) throws java.io.IOException, ClassNotFoundException {
    java.io.ObjectInputStream.GetField fields = s.readFields();
    char[] val = (char[]) fields.get("value", null);
    int cnt = fields.get("count", 0);
    if (val == null || cnt < 0 || cnt > val.length) {
      throw new java.io.StreamCorruptedException("count value invalid");
    }
    value = val;
    count = cnt;
  }
}
