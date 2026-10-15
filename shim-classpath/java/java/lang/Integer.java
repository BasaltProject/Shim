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
    int byte3 =  v >>> 24;
    int byte2 = (v >>> 8) & 0xFF00;
    int byte1 = (v <<  8) & 0xFF00;
    int byte0 =  v << 24;
    return (byte0 | byte1 | byte2 | byte3);
  }

  public static int parseInt(String s) {
    return parseInt(s, 10);
  }

  public static int parseInt(String s, int radix) {
    return (int) Long.parseLong(s, radix);
  }

  public static Integer decode(String string) {
    if (string.startsWith("-")) {
      if (string.startsWith("-0") || string.startsWith("-#")) {
        return new Integer(-decode(string.substring(1)));
      }
    } else if (string.startsWith("0")) {
      char c = string.length() < 2 ? (char)-1 : string.charAt(1);
      if (c == 'x' || c == 'X') {
        return new Integer(parseInt(string.substring(2), 0x10));
      }
      return new Integer(parseInt(string, 010));
    } else if (string.startsWith("#")) {
      return new Integer(parseInt(string.substring(1), 0x10));
    }
    return new Integer(parseInt(string, 10));
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

  public static int parseUnsignedInt(String s, int radix) {
    if (s == null) {
      throw new NumberFormatException("null");
    }
    int len = s.length();
    if (len > 0) {
      char firstChar = s.charAt(0);
      if (firstChar == '-') {
        throw new NumberFormatException(String.format("Illegal leading minus sign on unsigned string %s.", s));
      } else {
        if (len <= 5 || (radix == 10 && len <= 9)) {
          return parseInt(s, radix);
        } else {
          long ell = Long.parseLong(s, radix);
          if ((ell & 0xFFFFFFFF00000000L) == 0) {
            return (int) ell;
          } else {
            throw new NumberFormatException(String.format("String value %s exceeds range of unsigned integer.", s));
          }
        }
      }
    } else {
      throw new NumberFormatException("For input string: \"\"");
    }
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

  @Override
  public java.util.Optional<Integer> describeConstable() {
    return java.util.Optional.of(this);
  }

  @Override
  public Integer resolveConstantDesc(java.lang.invoke.MethodHandles.Lookup lookup) {
    return this;
  }
}

