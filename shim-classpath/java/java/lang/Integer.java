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

public final class Integer extends Number implements Comparable<Integer>,
    java.lang.constant.Constable, java.lang.constant.ConstantDesc {
  public static final Class TYPE = shim.Classes.forCanonicalName("I");

  public static final int MIN_VALUE = 0x80000000;
  public static final int MAX_VALUE = 0x7FFFFFFF;

  public static final int SIZE = 32;
  public static final int BYTES = 4;

  private final int value;

  public Integer(int value) {
    this.value = value;
  }

  public Integer(String s) {
    this.value = parseInt(s);
  }

  public static Integer valueOf(int value) {
    return new Integer(value);
  }

  public static Integer valueOf(String value) {
    return valueOf(parseInt(value));
  }

  public boolean equals(Object o) {
    return o instanceof Integer && ((Integer) o).value == value;
  }

  public int hashCode() {
    return hashCode(value);
  }

  public static int hashCode(int value) {
    return value;
  }

  public int compareTo(Integer other) {
    return compare(value, other.value);
  }

  public static int compare(int x, int y) {
    return (x < y) ? -1 : ((x == y) ? 0 : 1);
  }

  public String toString() {
    return toString(value);
  }

  public static String toString(int v, int radix) {
    return Long.toString(v, radix);
  }

  public static String toString(int v) {
    return toString(v, 10);
  }

  public static String toHexString(int v) {
    return Long.toString(((long) v) & 0xFFFFFFFFL, 16);
  }

  public static String toOctalString(int v) {
    return Long.toString(((long) v) & 0xFFFFFFFFL, 8);
  }

  public static String toBinaryString(int v) {
    return Long.toString(((long) v) & 0xFFFFFFFFL, 2);
  }

  public byte byteValue() {
    return (byte) value;
  }

  public short shortValue() {
    return (short) value;
  }

  public int intValue() {
    return value;
  }

  public long longValue() {
    return value;
  }

  public float floatValue() {
    return (float) value;
  }

  public double doubleValue() {
    return (double) value;
  }

  public static int signum(int v) {
    if (v == 0)     return  0;
    else if (v > 0) return  1;
    else            return -1;
  }

  // See http://graphics.stanford.edu/~seander/bithacks.html#CountBitsSetParallel
  public static int bitCount(int v) {
    v = v - ((v >> 1) & 0x55555555);
    v = (v & 0x33333333) + ((v >> 2) & 0x33333333);
    return ((v + (v >> 4) & 0xF0F0F0F) * 0x1010101) >> 24;
  }

  public static int reverseBytes(int v) {
    return (v >>> 24) |
           ((v >> 8) & 0x0000FF00) |
           ((v << 8) & 0x00FF0000) |
           (v << 24);
  }

  public static int parseInt(String s) {
    return parseInt(s, 10);
  }

  public static int parseInt(CharSequence s, int beginIndex, int endIndex, int radix) {
    java.util.Objects.requireNonNull(s);
    java.util.Objects.checkFromToIndex(beginIndex, endIndex, s.length());
    if (radix < Character.MIN_RADIX || radix > Character.MAX_RADIX) {
      throw new NumberFormatException("radix " + radix + " not in [" + Character.MIN_RADIX + ", " + Character.MAX_RADIX + "]");
    }
    int len = endIndex - beginIndex;
    if (len == 0) {
      throw new NumberFormatException("For input string: \"\"");
    }
    int i = beginIndex;
    char firstChar = s.charAt(i);
    boolean negative = false;
    int limit = -Integer.MAX_VALUE;
    if (firstChar < '0') {
      if (firstChar == '-') {
        negative = true;
        limit = Integer.MIN_VALUE;
      } else if (firstChar != '+') {
        throw new NumberFormatException("For input string: \"" + s.subSequence(beginIndex, endIndex) + "\"");
      }
      if (len == 1) {
        throw new NumberFormatException("For input string: \"" + s.subSequence(beginIndex, endIndex) + "\"");
      }
      i++;
    }
    int multmin = limit / radix;
    int result = 0;
    while (i < endIndex) {
      int digit = Character.digit(s.charAt(i++), radix);
      if (digit < 0) {
        throw new NumberFormatException("For input string: \"" + s.subSequence(beginIndex, endIndex) + "\"");
      }
      if (result < multmin) {
        throw new NumberFormatException("For input string: \"" + s.subSequence(beginIndex, endIndex) + "\"");
      }
      result *= radix;
      if (result < limit + digit) {
        throw new NumberFormatException("For input string: \"" + s.subSequence(beginIndex, endIndex) + "\"");
      }
      result -= digit;
    }
    return negative ? result : -result;
  }

  public static int parseInt(String s, int radix) {
    if (s == null) {
      throw new NumberFormatException("null");
    }
    return parseInt(s, 0, s.length(), radix);
  }

  public static Integer decode(String nm) {
    int radix = 10;
    int index = 0;
    boolean negative = false;
    int result;
    if (nm.isEmpty()) {
      throw new NumberFormatException("Zero length string");
    }
    char firstChar = nm.charAt(0);
    if (firstChar == '-') {
      negative = true;
      index++;
    } else if (firstChar == '+') {
      index++;
    }
    if (nm.startsWith("0x", index) || nm.startsWith("0X", index)) {
      index += 2;
      radix = 16;
    } else if (nm.startsWith("#", index)) {
      index++;
      radix = 16;
    } else if (nm.startsWith("0", index) && nm.length() > 1 + index) {
      index++;
      radix = 8;
    }
    if (nm.startsWith("-", index) || nm.startsWith("+", index)) {
      throw new NumberFormatException("Sign character in wrong position");
    }
    try {
      result = parseInt(nm.substring(index), radix);
      result = negative ? -result : result;
    } catch (NumberFormatException e) {
      String constant = negative ? ("-" + nm.substring(index)) : nm.substring(index);
      result = parseInt(constant, radix);
    }
    return Integer.valueOf(result);
  }

  public static int numberOfLeadingZeros(int i) {
    // See nlz5 at http://www.hackersdelight.org/hdcodetxt/nlz.c.txt
    i |= i >> 1;
    i |= i >> 2;
    i |= i >> 4;
    i |= i >> 8;
    i |= i >> 16;
    return bitCount(~i);
  }

  public static int compareUnsigned(int x, int y) {
    return compare(x + MIN_VALUE, y + MIN_VALUE);
  }

  public static long toUnsignedLong(int x) {
    return ((long) x) & 0xFFFFFFFFL;
  }

  public static int divideUnsigned(int dividend, int divisor) {
    return (int) (toUnsignedLong(dividend) / toUnsignedLong(divisor));
  }

  public static int remainderUnsigned(int dividend, int divisor) {
    return (int) (toUnsignedLong(dividend) % toUnsignedLong(divisor));
  }

  public static String toUnsignedString(int i, int radix) {
    return Long.toUnsignedString(toUnsignedLong(i), radix);
  }

  public static String toUnsignedString(int i) {
    return toUnsignedString(i, 10);
  }

  public static int parseUnsignedInt(CharSequence s, int beginIndex, int endIndex, int radix) {
    java.util.Objects.requireNonNull(s);
    java.util.Objects.checkFromToIndex(beginIndex, endIndex, s.length());
    int len = endIndex - beginIndex;
    if (len == 0) {
      throw new NumberFormatException("For input string: \"\"");
    }
    char firstChar = s.charAt(beginIndex);
    if (firstChar == '-') {
      throw new NumberFormatException(String.format("Illegal leading minus sign on unsigned string %s.", s.subSequence(beginIndex, endIndex)));
    }
    if (firstChar == '+') {
      beginIndex++;
      len--;
      if (len == 0) {
        throw new NumberFormatException("For input string: \"+\"");
      }
    }
    if (len <= 5 || (radix == 10 && len <= 9)) {
      return parseInt(s, beginIndex, endIndex, radix);
    } else {
      long ell = Long.parseLong(s, beginIndex, endIndex, radix);
      if ((ell & 0xFFFFFFFF00000000L) == 0) {
        return (int) ell;
      } else {
        throw new NumberFormatException(String.format("String value %s exceeds range of unsigned integer.", s.subSequence(beginIndex, endIndex)));
      }
    }
  }

  public static int parseUnsignedInt(String s, int radix) {
    if (s == null) {
      throw new NumberFormatException("null");
    }
    return parseUnsignedInt(s, 0, s.length(), radix);
  }

  public static int parseUnsignedInt(String s) {
    return parseUnsignedInt(s, 10);
  }

  public static int min(int a, int b) {
    return Math.min(a, b);
  }

  public static int max(int a, int b) {
    return Math.max(a, b);
  }

  public static int sum(int a, int b) {
    return a + b;
  }

  public static int rotateLeft(int i, int distance) {
    return (i << distance) | (i >>> -distance);
  }

  public static int rotateRight(int i, int distance) {
    return (i >>> distance) | (i << -distance);
  }

  public static int numberOfTrailingZeros(int i) {
    return bitCount((i & -i) - 1);
  }

  public static int highestOneBit(int i) {
    i |= (i >> 1);
    i |= (i >> 2);
    i |= (i >> 4);
    i |= (i >> 8);
    i |= (i >> 16);
    return i - (i >>> 1);
  }

  public static int lowestOneBit(int i) {
    return i & -i;
  }

  public static int reverse(int i) {
    i = (i & 0x55555555) << 1 | (i >>> 1) & 0x55555555;
    i = (i & 0x33333333) << 2 | (i >>> 2) & 0x33333333;
    i = (i & 0x0f0f0f0f) << 4 | (i >>> 4) & 0x0f0f0f0f;
    return reverseBytes(i);
  }

  public static int compress(int i, int mask) {
    i &= mask;
    int res = 0;
    int outBit = 0;
    while (mask != 0) {
      int bit = mask & -mask;
      if ((i & bit) != 0) {
        res |= (1 << outBit);
      }
      outBit++;
      mask &= mask - 1;
    }
    return res;
  }

  public static int expand(int i, int mask) {
    int res = 0;
    int inBit = 0;
    while (mask != 0) {
      int bit = mask & -mask;
      if (((i >>> inBit) & 1) != 0) {
        res |= bit;
      }
      inBit++;
      mask &= mask - 1;
    }
    return res;
  }

  @Override
  public java.util.Optional<Integer> describeConstable() {
    return java.util.Optional.of(this);
  }

  @Override
  public Integer resolveConstantDesc(java.lang.invoke.MethodHandles.Lookup lookup) {
    return this;
  }

  public static Integer valueOf(String s, int radix) throws NumberFormatException {
    return Integer.valueOf(parseInt(s, radix));
  }

  public static Integer getInteger(String nm) {
    return getInteger(nm, null);
  }

  public static Integer getInteger(String nm, int val) {
    Integer result = getInteger(nm, null);
    return result == null ? Integer.valueOf(val) : result;
  }

  public static Integer getInteger(String nm, Integer val) {
    String v = null;
    try {
      v = System.getProperty(nm);
    } catch (IllegalArgumentException | NullPointerException e) {
    }
    if (v != null) {
      try {
        return Integer.decode(v);
      } catch (NumberFormatException e) {
      }
    }
    return val;
  }
}
