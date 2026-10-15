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

public final class Long extends Number implements Comparable<Long> {
  public static final long MIN_VALUE = -9223372036854775808l;
  public static final long MAX_VALUE =  9223372036854775807l;

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

  private static long pow(long a, long b) {
    long c = 1;
    for (int i = 0; i < b; ++i) c *= a;
    return c;
  }

  public static long parseLong(String s) {
    return parseLong(s, 10);
  } 

  public static long parseLong(String s, int radix) {
    int i = 0;
    long number = 0;
    boolean negative = s.startsWith("-");
    int length = s.length();
    if (negative) {
      i = 1;
      -- length;
    }

    long factor = pow(radix, length - 1);
    for (; i < s.length(); ++i) {
      char c = s.charAt(i);
      int digit = Character.digit(c, radix);
      if (digit >= 0) {
        number += digit * factor;
        factor /= radix;
      } else {
        throw new NumberFormatException("invalid character " + c + " code " +
                                        (int) c);
      }
    }

    if (negative) {
      number = -number;
    }

    return number;
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

  public static long parseUnsignedLong(String s, int radix) {
    if (s == null) {
      throw new NumberFormatException("null");
    }
    int len = s.length();
    if (len > 0) {
      char firstChar = s.charAt(0);
      if (firstChar == '-') {
        throw new NumberFormatException(String.format("Illegal leading minus sign on unsigned string %s.", s));
      } else {
        if (len <= 12 || (radix == 10 && len <= 18)) {
          return parseLong(s, radix);
        }
        long first = parseUnsignedLong(s.substring(0, len - 1), radix);
        int second = Character.digit(s.charAt(len - 1), radix);
        if (second < 0) {
          throw new NumberFormatException("Bad digit at end of " + s);
        }
        long result = first * radix + second;
        if (compareUnsigned(result, first) < 0 || divideUnsigned(result - second, radix) != first) {
          throw new NumberFormatException(String.format("String value %s exceeds range of unsigned long.", s));
        }
        return result;
      }
    } else {
      throw new NumberFormatException("For input string: \"\"");
    }
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
}
