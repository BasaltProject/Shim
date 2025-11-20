/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang;

import java.util.Objects;

abstract class AbstractStringBuilder implements Appendable, CharSequence {
  char[] value;
  int count;

  AbstractStringBuilder() {
    value = new char[16];
  }

  AbstractStringBuilder(int capacity) {
    if (capacity < 0) {
      throw new NegativeArraySizeException(String.valueOf(capacity));
    }
    value = new char[capacity];
  }

  AbstractStringBuilder(String str) {
    int length = str.length();
    value = new char[length + 16];
    str.getChars(0, length, value, 0);
    count = length;
  }

  AbstractStringBuilder(CharSequence seq) {
    int length = seq.length();
    value = new char[length + 16];
    append(seq);
  }

  static String range(int from, int to, int length) {
    return "Range [" + from + ", " + to + ") out of bounds for length " + length;
  }

  static void checkIndex(int index, int length) {
    if (index < 0 || index >= length) {
      throw new StringIndexOutOfBoundsException("Index " + index + " out of bounds for length " + length);
    }
  }

  static void checkRangeSIOOBE(int start, int end, int length) {
    if (start < 0 || start > end || end > length) {
      throw new StringIndexOutOfBoundsException(range(start, end, length));
    }
  }

  static void checkRange(int start, int end, int length) {
    if (start < 0 || start > end || end > length) {
      throw new IndexOutOfBoundsException(range(start, end, length));
    }
  }

  static void checkOffset(int offset, int length) {
    if (offset < 0 || offset > length) {
      throw new StringIndexOutOfBoundsException(range(offset, length, length));
    }
  }

  @Override
  public int length() {
    return count;
  }

  public int capacity() {
    return value.length;
  }

  public void ensureCapacity(int minimumCapacity) {
    if (minimumCapacity > 0) {
      ensureCapacityInternal(minimumCapacity);
    }
  }

  private void ensureCapacityInternal(int minimumCapacity) {
    int oldCapacity = value.length;
    if (minimumCapacity - oldCapacity > 0) {
      value = java.util.Arrays.copyOf(value, newCapacity(minimumCapacity));
    }
  }

  private int newCapacity(int minCapacity) {
    int oldLength = value.length;
    int minGrowth = minCapacity - oldLength;
    int prefGrowth = oldLength + 2;
    long grown = (long) oldLength + Math.max(minGrowth, prefGrowth);
    if (grown > Integer.MAX_VALUE - 8) {
      if (minCapacity < 0 || minCapacity > Integer.MAX_VALUE - 8) {
        throw new OutOfMemoryError("Required length exceeds implementation limit");
      }
      return Math.max(minCapacity, Integer.MAX_VALUE - 8);
    }
    return (int) grown;
  }

  public void trimToSize() {
    if (count < value.length) {
      value = java.util.Arrays.copyOf(value, count);
    }
  }

  public void setLength(int newLength) {
    if (newLength < 0) {
      throw new StringIndexOutOfBoundsException(newLength);
    }
    ensureCapacityInternal(newLength);
    if (count < newLength) {
      java.util.Arrays.fill(value, count, newLength, '\0');
    }
    count = newLength;
  }

  @Override
  public char charAt(int index) {
    checkIndex(index, count);
    return value[index];
  }

  public int codePointAt(int index) {
    checkIndex(index, count);
    return Character.codePointAt(value, index, count);
  }

  public int codePointBefore(int index) {
    int i = index - 1;
    checkIndex(i, count);
    return Character.codePointBefore(value, index, 0);
  }

  public int codePointCount(int beginIndex, int endIndex) {
    checkRange(beginIndex, endIndex, count);
    return Character.codePointCount(value, beginIndex, endIndex - beginIndex);
  }

  public int offsetByCodePoints(int index, int codePointOffset) {
    if (index < 0 || index > count) {
      throw new IndexOutOfBoundsException();
    }
    int x = index;
    if (codePointOffset >= 0) {
      int i;
      for (i = 0; x < count && i < codePointOffset; i++) {
        if (Character.isHighSurrogate(value[x++]) && x < count && Character.isLowSurrogate(value[x])) {
          x++;
        }
      }
      if (i < codePointOffset) {
        throw new IndexOutOfBoundsException();
      }
    } else {
      int i;
      for (i = codePointOffset; x > 0 && i < 0; i++) {
        if (Character.isLowSurrogate(value[--x]) && x > 0 && Character.isHighSurrogate(value[x - 1])) {
          x--;
        }
      }
      if (i < 0) {
        throw new IndexOutOfBoundsException();
      }
    }
    return x;
  }

  public void getChars(int srcBegin, int srcEnd, char[] dst, int dstBegin) {
    checkRangeSIOOBE(srcBegin, srcEnd, count);
    int n = srcEnd - srcBegin;
    checkRange(dstBegin, dstBegin + n, dst.length);
    System.arraycopy(value, srcBegin, dst, dstBegin, n);
  }

  public void setCharAt(int index, char ch) {
    checkIndex(index, count);
    value[index] = ch;
  }

  public AbstractStringBuilder append(Object obj) {
    return append(String.valueOf(obj));
  }

  public AbstractStringBuilder append(String str) {
    if (str == null) {
      return appendNull();
    }
    int len = str.length();
    ensureCapacityInternal(count + len);
    str.getChars(0, len, value, count);
    count += len;
    return this;
  }

  public AbstractStringBuilder append(StringBuffer sb) {
    return append((CharSequence) sb);
  }

  AbstractStringBuilder append(AbstractStringBuilder asb) {
    if (asb == null) {
      return appendNull();
    }
    int len = asb.length();
    ensureCapacityInternal(count + len);
    System.arraycopy(asb.value, 0, value, count, len);
    count += len;
    return this;
  }

  @Override
  public AbstractStringBuilder append(CharSequence s) {
    if (s == null) {
      return appendNull();
    }
    if (s instanceof String) {
      return append((String) s);
    }
    if (s instanceof AbstractStringBuilder) {
      return append((AbstractStringBuilder) s);
    }
    return append(s, 0, s.length());
  }

  private AbstractStringBuilder appendNull() {
    ensureCapacityInternal(count + 4);
    value[count++] = 'n';
    value[count++] = 'u';
    value[count++] = 'l';
    value[count++] = 'l';
    return this;
  }

  @Override
  public AbstractStringBuilder append(CharSequence s, int start, int end) {
    if (s == null) {
      s = "null";
    }
    checkRange(start, end, s.length());
    int len = end - start;
    ensureCapacityInternal(count + len);
    if (s instanceof String) {
      ((String) s).getChars(start, end, value, count);
      count += len;
    } else {
      for (int i = start, j = count; i < end; i++, j++) {
        value[j] = s.charAt(i);
      }
      count += len;
    }
    return this;
  }

  public AbstractStringBuilder append(char[] str) {
    int len = str.length;
    ensureCapacityInternal(count + len);
    System.arraycopy(str, 0, value, count, len);
    count += len;
    return this;
  }

  public AbstractStringBuilder append(char[] str, int offset, int len) {
    int end = offset + len;
    checkRange(offset, end, str.length);
    ensureCapacityInternal(count + len);
    System.arraycopy(str, offset, value, count, len);
    count += len;
    return this;
  }

  public AbstractStringBuilder append(boolean b) {
    return append(b ? "true" : "false");
  }

  @Override
  public AbstractStringBuilder append(char c) {
    ensureCapacityInternal(count + 1);
    value[count++] = c;
    return this;
  }

  public AbstractStringBuilder append(int i) {
    return append(String.valueOf(i));
  }

  public AbstractStringBuilder append(long l) {
    return append(String.valueOf(l));
  }

  public AbstractStringBuilder append(float f) {
    return append(String.valueOf(f));
  }

  public AbstractStringBuilder append(double d) {
    return append(String.valueOf(d));
  }

  public AbstractStringBuilder delete(int start, int end) {
    int count = this.count;
    if (end > count) {
      end = count;
    }
    checkRangeSIOOBE(start, end, count);
    int len = end - start;
    if (len > 0) {
      System.arraycopy(value, start + len, value, start, count - end);
      this.count = count - len;
    }
    return this;
  }

  public AbstractStringBuilder appendCodePoint(int codePoint) {
    if (Character.isBmpCodePoint(codePoint)) {
      return append((char) codePoint);
    }
    if (!Character.isValidCodePoint(codePoint)) {
      throw new IllegalArgumentException(String.format("Not a valid Unicode code point: 0x%X", codePoint));
    }
    ensureCapacityInternal(count + 2);
    value[count++] = Character.highSurrogate(codePoint);
    value[count++] = Character.lowSurrogate(codePoint);
    return this;
  }

  public AbstractStringBuilder deleteCharAt(int index) {
    checkIndex(index, count);
    System.arraycopy(value, index + 1, value, index, count - index - 1);
    count--;
    return this;
  }

  public AbstractStringBuilder replace(int start, int end, String str) {
    int count = this.count;
    if (end > count) {
      end = count;
    }
    checkRangeSIOOBE(start, end, count);
    int len = str.length();
    int newCount = count + len - (end - start);
    ensureCapacityInternal(newCount);
    System.arraycopy(value, end, value, start + len, count - end);
    str.getChars(0, len, value, start);
    this.count = newCount;
    return this;
  }

  public String substring(int start) {
    return substring(start, count);
  }

  @Override
  public CharSequence subSequence(int start, int end) {
    return substring(start, end);
  }

  public String substring(int start, int end) {
    checkRangeSIOOBE(start, end, count);
    return new String(value, start, end - start);
  }

  public AbstractStringBuilder insert(int index, char[] str, int offset, int len) {
    checkOffset(index, count);
    checkRangeSIOOBE(offset, offset + len, str.length);
    ensureCapacityInternal(count + len);
    System.arraycopy(value, index, value, index + len, count - index);
    System.arraycopy(str, offset, value, index, len);
    count += len;
    return this;
  }

  public AbstractStringBuilder insert(int offset, Object obj) {
    return insert(offset, String.valueOf(obj));
  }

  public AbstractStringBuilder insert(int offset, String str) {
    checkOffset(offset, count);
    if (str == null) {
      str = "null";
    }
    int len = str.length();
    ensureCapacityInternal(count + len);
    System.arraycopy(value, offset, value, offset + len, count - offset);
    str.getChars(0, len, value, offset);
    count += len;
    return this;
  }

  public AbstractStringBuilder insert(int offset, char[] str) {
    checkOffset(offset, count);
    int len = str.length;
    ensureCapacityInternal(count + len);
    System.arraycopy(value, offset, value, offset + len, count - offset);
    System.arraycopy(str, 0, value, offset, len);
    count += len;
    return this;
  }

  public AbstractStringBuilder insert(int dstOffset, CharSequence s) {
    if (s == null) {
      s = "null";
    }
    if (s instanceof String) {
      return insert(dstOffset, (String) s);
    }
    return insert(dstOffset, s, 0, s.length());
  }

  public AbstractStringBuilder insert(int dstOffset, CharSequence s, int start, int end) {
    if (s == null) {
      s = "null";
    }
    checkOffset(dstOffset, count);
    checkRange(start, end, s.length());
    int len = end - start;
    ensureCapacityInternal(count + len);
    System.arraycopy(value, dstOffset, value, dstOffset + len, count - dstOffset);
    for (int i = start, j = dstOffset; i < end; i++, j++) {
      value[j] = s.charAt(i);
    }
    count += len;
    return this;
  }

  public AbstractStringBuilder insert(int offset, boolean b) {
    return insert(offset, String.valueOf(b));
  }

  public AbstractStringBuilder insert(int offset, char c) {
    checkOffset(offset, count);
    ensureCapacityInternal(count + 1);
    System.arraycopy(value, offset, value, offset + 1, count - offset);
    value[offset] = c;
    count += 1;
    return this;
  }

  public AbstractStringBuilder insert(int offset, int i) {
    return insert(offset, String.valueOf(i));
  }

  public AbstractStringBuilder insert(int offset, long l) {
    return insert(offset, String.valueOf(l));
  }

  public AbstractStringBuilder insert(int offset, float f) {
    return insert(offset, String.valueOf(f));
  }

  public AbstractStringBuilder insert(int offset, double d) {
    return insert(offset, String.valueOf(d));
  }

  public int indexOf(String str) {
    return indexOf(str, 0);
  }

  public int indexOf(String str, int fromIndex) {
    int tgtCount = str.length();
    if (fromIndex >= count) {
      return tgtCount == 0 ? count : -1;
    }
    if (fromIndex < 0) {
      fromIndex = 0;
    }
    if (tgtCount == 0) {
      return fromIndex;
    }
    int max = count - tgtCount;
    char first = str.charAt(0);
    for (int i = fromIndex; i <= max; i++) {
      if (value[i] != first) {
        continue;
      }
      int j = 1;
      while (j < tgtCount && value[i + j] == str.charAt(j)) {
        j++;
      }
      if (j == tgtCount) {
        return i;
      }
    }
    return -1;
  }

  public int lastIndexOf(String str) {
    return lastIndexOf(str, count);
  }

  public int lastIndexOf(String str, int fromIndex) {
    int tgtCount = str.length();
    int rightIndex = count - tgtCount;
    if (fromIndex > rightIndex) {
      fromIndex = rightIndex;
    }
    if (fromIndex < 0) {
      return -1;
    }
    if (tgtCount == 0) {
      return fromIndex;
    }
    for (int i = fromIndex; i >= 0; i--) {
      int j = 0;
      while (j < tgtCount && value[i + j] == str.charAt(j)) {
        j++;
      }
      if (j == tgtCount) {
        return i;
      }
    }
    return -1;
  }

  public AbstractStringBuilder reverse() {
    boolean hasSurrogates = false;
    int n = count - 1;
    for (int j = (n - 1) >> 1; j >= 0; j--) {
      int k = n - j;
      char cj = value[j];
      char ck = value[k];
      value[j] = ck;
      value[k] = cj;
      if (Character.isSurrogate(cj) || Character.isSurrogate(ck)) {
        hasSurrogates = true;
      }
    }
    if (hasSurrogates) {
      for (int i = 0; i < count - 1; i++) {
        char c2 = value[i];
        if (Character.isLowSurrogate(c2)) {
          char c1 = value[i + 1];
          if (Character.isHighSurrogate(c1)) {
            value[i++] = c1;
            value[i] = c2;
          }
        }
      }
    }
    return this;
  }

  public AbstractStringBuilder repeat(int codePoint, int count) {
    if (count < 0) {
      throw new IllegalArgumentException("count is negative: " + count);
    }
    if (!Character.isValidCodePoint(codePoint)) {
      throw new IllegalArgumentException(String.format("Not a valid Unicode code point: 0x%X", codePoint));
    }
    if (Character.isBmpCodePoint(codePoint)) {
      ensureCapacityInternal(this.count + count);
      java.util.Arrays.fill(value, this.count, this.count + count, (char) codePoint);
      this.count += count;
    } else {
      for (int i = 0; i < count; i++) {
        appendCodePoint(codePoint);
      }
    }
    return this;
  }

  public AbstractStringBuilder repeat(CharSequence cs, int count) {
    if (count < 0) {
      throw new IllegalArgumentException("count is negative: " + count);
    }
    if (cs == null) {
      cs = "null";
    }
    String s = cs.toString();
    for (int i = 0; i < count; i++) {
      append(s);
    }
    return this;
  }

  @Override
  public abstract String toString();

  int compareToImpl(AbstractStringBuilder another) {
    Objects.requireNonNull(another);
    int len1 = count;
    int len2 = another.count;
    int lim = Math.min(len1, len2);
    for (int k = 0; k < lim; k++) {
      char c1 = value[k];
      char c2 = another.value[k];
      if (c1 != c2) {
        return c1 - c2;
      }
    }
    return len1 - len2;
  }
}
