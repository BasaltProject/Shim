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

public final class Long extends Number implements Comparable<Long>,
    java.lang.constant.Constable, java.lang.constant.ConstantDesc {
  private static final long serialVersionUID = 4290774380558885855L;

  public static final long MIN_VALUE = -9223372036854775808l;
  public static final long MAX_VALUE =  9223372036854775807l;

  public static final int SIZE = 64;
  public static final int BYTES = 8;

  public static final Class TYPE = shim.Classes.forCanonicalName("J");

  private final long value;

  public Long(long value) {
    this.value = value;
  }

  public Long(String s) {
    this.value = parseLong(s);
  }

  public static Long valueOf(String value) {
    return new Long(value);
  }

  public static Long valueOf(long value) {
    return new Long(value);
  }

  public int compareTo(Long o) {
    return compare(value, o.value);
  }

  public static int compare(long x, long y) {
    return (x < y) ? -1 : ((x == y) ? 0 : 1);
  }

  public boolean equals(Object o) {
    return o instanceof Long && ((Long) o).value == value;
  }

  public int hashCode() {
    return hashCode(value);
  }

  public static int hashCode(long value) {
    return (int) (value ^ (value >>> 32));
  }

  public String toString() {
    return String.valueOf(value);
  }

  public static String toString(long v, int radix) {
    if (radix < 1 || radix > 36) {
      throw new IllegalArgumentException("radix " + radix + " not in [1,36]");
    }

    if (v == 0) {
      return "0";
    }

    boolean negative = v < 0;

    int size = (negative ? 1 : 0);
    for (long n = v; n != 0; n /= radix) ++size;

    char[] array = new char[size];

    int i = size - 1;
    for (long n = v; n != 0; n /= radix) {
      long digit = n % radix;
      if (negative) digit = -digit;

      if (digit >= 0 && digit <= 9) {
        array[i] = (char) ('0' + digit);
      } else {
        array[i] = (char) ('a' + (digit - 10));
      }
      --i;
    }

    if (negative) {
      array[i] = '-';
    }

    return new String(array, 0, size, false);
  }

  public static String toString(long v) {
    return toString(v, 10);
  }

  private static String unsignedString(long value, int shift) {
    char[] digits = new char[64];
    int position = digits.length;
    int mask = (1 << shift) - 1;
    do {
      digits[--position] = "0123456789abcdef".charAt((int) value & mask);
      value >>>= shift;
    } while (value != 0);
    return new String(digits, position, digits.length - position);
  }

  public static String toHexString(long v) {
    return unsignedString(v, 4);
  }

  public static String toOctalString(long v) {
    return unsignedString(v, 3);
  }

  public static String toBinaryString(long v) {
    return unsignedString(v, 1);
  }

  public byte byteValue() {
    return (byte) value;
  }

  public short shortValue() {
    return (short) value;
  }

  public int intValue() {
    return (int) value;
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

  public static int signum(long v) {
    if (v == 0)     return  0;
    else if (v > 0) return  1;
    else            return -1;
  }

  public static long parseLong(CharSequence s, int beginIndex, int endIndex, int radix) {
    return parseLong0(s, beginIndex, endIndex, radix, true);
  }

  private static NumberFormatException numberError(CharSequence s, int beginIndex, int endIndex, int radix,
      int errorIndex, boolean seq) {
    if (seq) {
      return new NumberFormatException("Error at index " + (errorIndex - beginIndex) + " in: \""
          + s.subSequence(beginIndex, endIndex) + "\"");
    }
    return new NumberFormatException("For input string: \"" + s.subSequence(beginIndex, endIndex) + "\""
        + (radix == 10 ? "" : " under radix " + radix));
  }

  private static long parseLong0(CharSequence s, int beginIndex, int endIndex, int radix, boolean seq) {
    java.util.Objects.requireNonNull(s);
    java.util.Objects.checkFromToIndex(beginIndex, endIndex, s.length());
    if (radix < Character.MIN_RADIX) {
      throw new NumberFormatException("radix " + radix + " less than Character.MIN_RADIX");
    }
    if (radix > Character.MAX_RADIX) {
      throw new NumberFormatException("radix " + radix + " greater than Character.MAX_RADIX");
    }
    int len = endIndex - beginIndex;
    if (len == 0) {
      throw numberError(s, beginIndex, endIndex, radix, beginIndex, false);
    }
    int i = beginIndex;
    char firstChar = s.charAt(i);
    boolean negative = false;
    long limit = -Long.MAX_VALUE;
    if (firstChar < '0') {
      if (firstChar == '-') {
        negative = true;
        limit = Long.MIN_VALUE;
      } else if (firstChar != '+') {
        throw numberError(s, beginIndex, endIndex, radix, i, seq);
      }
      if (len == 1) {
        throw numberError(s, beginIndex, endIndex, radix, i + 1, seq);
      }
      i++;
    }
    long multmin = limit / radix;
    long result = 0;
    while (i < endIndex) {
      int digit = Character.digit(s.charAt(i), radix);
      if (digit < 0 || result < multmin) {
        throw numberError(s, beginIndex, endIndex, radix, i, seq);
      }
      result *= radix;
      if (result < limit + digit) {
        throw numberError(s, beginIndex, endIndex, radix, i, seq);
      }
      i++;
      result -= digit;
    }
    return negative ? result : -result;
  }

  public static long parseLong(String s, int radix) {
    if (s == null) {
      throw new NumberFormatException("Cannot parse null string");
    }
    return parseLong0(s, 0, s.length(), radix, false);
  }

  public static long parseLong(String s) {
    return parseLong(s, 10);
  }

  public static int compareUnsigned(long x, long y) {
    return compare(x + MIN_VALUE, y + MIN_VALUE);
  }

  public static long divideUnsigned(long dividend, long divisor) {
    if (divisor < 0L) {
      return (compareUnsigned(dividend, divisor)) < 0 ? 0L : 1L;
    }
    if (dividend >= 0) {
      return dividend / divisor;
    } else {
      long q = ((dividend >>> 1) / divisor) << 1;
      long r = dividend - q * divisor;
      if (compareUnsigned(r, divisor) >= 0) {
        q++;
      }
      return q;
    }
  }

  public static long remainderUnsigned(long dividend, long divisor) {
    if (dividend >= 0 && divisor > 0) {
      return dividend % divisor;
    } else {
      long q = divideUnsigned(dividend, divisor);
      return dividend - q * divisor;
    }
  }

  public static String toUnsignedString(long i, int radix) {
    if (i >= 0) {
      return toString(i, radix);
    }
    if (radix < Character.MIN_RADIX || radix > Character.MAX_RADIX) {
      radix = 10;
    }
    char[] buf = new char[65];
    int charPos = 65;
    long radixL = radix;
    while (compareUnsigned(i, radixL) >= 0) {
      long q = divideUnsigned(i, radixL);
      long r = i - q * radixL;
      buf[--charPos] = Character.forDigit((int) r, radix);
      i = q;
    }
    buf[--charPos] = Character.forDigit((int) i, radix);
    return new String(buf, charPos, 65 - charPos);
  }

  public static String toUnsignedString(long i) {
    return toUnsignedString(i, 10);
  }

  public static long parseUnsignedLong(CharSequence s, int beginIndex, int endIndex, int radix) {
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
    if (len <= 12 || (radix == 10 && len <= 18)) {
      return parseLong(s, beginIndex, endIndex, radix);
    }
    long first = parseUnsignedLong(s, beginIndex, endIndex - 1, radix);
    int second = Character.digit(s.charAt(endIndex - 1), radix);
    if (second < 0) {
      throw new NumberFormatException("Bad digit at end of " + s.subSequence(beginIndex, endIndex));
    }
    long result = first * radix + second;
    if (compareUnsigned(result, first) < 0 || divideUnsigned(result - second, radix) != first) {
      throw new NumberFormatException(String.format("String value %s exceeds range of unsigned long.", s.subSequence(beginIndex, endIndex)));
    }
    return result;
  }

  public static long parseUnsignedLong(String s, int radix) {
    if (s == null) {
      throw new NumberFormatException("Cannot parse null string");
    }
    return parseUnsignedLong(s, 0, s.length(), radix);
  }

  public static long parseUnsignedLong(String s) {
    return parseUnsignedLong(s, 10);
  }

  public static long min(long a, long b) {
    return Math.min(a, b);
  }

  public static long max(long a, long b) {
    return Math.max(a, b);
  }

  public static long sum(long a, long b) {
    return a + b;
  }

  public static long rotateLeft(long i, int distance) {
    return (i << distance) | (i >>> -distance);
  }

  public static long rotateRight(long i, int distance) {
    return (i >>> distance) | (i << -distance);
  }

  public static int bitCount(long i) {
    i = i - ((i >>> 1) & 0x5555555555555555L);
    i = (i & 0x3333333333333333L) + ((i >>> 2) & 0x3333333333333333L);
    i = (i + (i >>> 4)) & 0x0f0f0f0f0f0f0f0fL;
    i = i + (i >>> 8);
    i = i + (i >>> 16);
    i = i + (i >>> 32);
    return (int) i & 0x7f;
  }

  public static int numberOfLeadingZeros(long i) {
    int x = (int) (i >>> 32);
    return x == 0 ? 32 + Integer.numberOfLeadingZeros((int) i) : Integer.numberOfLeadingZeros(x);
  }

  public static int numberOfTrailingZeros(long i) {
    int x = (int) i;
    return x == 0 ? 32 + Integer.numberOfTrailingZeros((int) (i >>> 32)) : Integer.numberOfTrailingZeros(x);
  }

  public static long reverseBytes(long i) {
    i = (i & 0x00ff00ff00ff00ffL) << 8 | (i >>> 8) & 0x00ff00ff00ff00ffL;
    return (i << 48) | ((i & 0xffff0000L) << 16) |
        ((i >>> 16) & 0xffff0000L) | (i >>> 48);
  }

  public static long highestOneBit(long i) {
    i |= (i >> 1);
    i |= (i >> 2);
    i |= (i >> 4);
    i |= (i >> 8);
    i |= (i >> 16);
    i |= (i >> 32);
    return i - (i >>> 1);
  }

  public static long lowestOneBit(long i) {
    return i & -i;
  }

  public static long reverse(long i) {
    i = (i & 0x5555555555555555L) << 1 | (i >>> 1) & 0x5555555555555555L;
    i = (i & 0x3333333333333333L) << 2 | (i >>> 2) & 0x3333333333333333L;
    i = (i & 0x0f0f0f0f0f0f0f0fL) << 4 | (i >>> 4) & 0x0f0f0f0f0f0f0f0fL;
    return reverseBytes(i);
  }

  public static long compress(long i, long mask) {
    i &= mask;
    long res = 0;
    int outBit = 0;
    while (mask != 0) {
      long bit = mask & -mask;
      if ((i & bit) != 0) {
        res |= (1L << outBit);
      }
      outBit++;
      mask &= mask - 1;
    }
    return res;
  }

  public static long expand(long i, long mask) {
    long res = 0;
    int inBit = 0;
    while (mask != 0) {
      long bit = mask & -mask;
      if (((i >>> inBit) & 1L) != 0) {
        res |= bit;
      }
      inBit++;
      mask &= mask - 1;
    }
    return res;
  }

  @Override
  public java.util.Optional<Long> describeConstable() {
    return java.util.Optional.of(this);
  }

  @Override
  public Long resolveConstantDesc(java.lang.invoke.MethodHandles.Lookup lookup) {
    return this;
  }

  public static Long valueOf(String s, int radix) throws NumberFormatException {
    return Long.valueOf(parseLong(s, radix));
  }

  public static Long decode(String nm) {
    int radix = 10;
    int index = 0;
    boolean negative = false;
    long result;
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
      result = parseLong(nm.substring(index), radix);
      result = negative ? -result : result;
    } catch (NumberFormatException e) {
      String constant = negative ? ("-" + nm.substring(index)) : nm.substring(index);
      result = parseLong(constant, radix);
    }
    return Long.valueOf(result);
  }

  public static Long getLong(String nm) {
    return getLong(nm, null);
  }

  public static Long getLong(String nm, long val) {
    Long result = getLong(nm, null);
    return result == null ? Long.valueOf(val) : result;
  }

  public static Long getLong(String nm, Long val) {
    String v = null;
    try {
      v = System.getProperty(nm);
    } catch (IllegalArgumentException | NullPointerException e) {
    }
    if (v != null) {
      try {
        return Long.decode(v);
      } catch (NumberFormatException e) {
      }
    }
    return val;
  }
}
