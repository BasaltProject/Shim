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

public final class Double extends Number implements Comparable<Double>,
    java.lang.constant.Constable, java.lang.constant.ConstantDesc {
  public static final Class TYPE = shim.Classes.forCanonicalName("D");

  public static final double NEGATIVE_INFINITY = -1.0 / 0.0;
  public static final double POSITIVE_INFINITY =  1.0 / 0.0;
  public static final double NaN =  0.0 / 0.0;

  public static final double MIN_VALUE = 0x0.0000000000001p-1022;
  public static final double MAX_VALUE = 1.79769313486231570815e+308;

  public static final double MIN_NORMAL = 0x1.0p-1022;
  public static final int MIN_EXPONENT = -1022;
  public static final int MAX_EXPONENT = 1023;
  public static final int SIZE = 64;
  public static final int BYTES = 8;
  public static final int PRECISION = 53;

  private final double value;

  public Double(String value) {
    this.value = parseDouble(value);
  }

  public Double(double value) {
    this.value = value;
  }

  public static Double valueOf(double value) {
    return new Double(value);
  }

  public static Double valueOf(String s) {
    return new Double(s);
  }

  public boolean equals(Object o) {
    return o instanceof Double && doubleToLongBits(((Double) o).value) == doubleToLongBits(value);
  }

  public int hashCode() { return hashCode(value); }

  public static int hashCode(double value) {
    long bits = doubleToLongBits(value);
    return (int) (bits ^ (bits >>> 32));
  }

  public int compareTo(Double other) { return compare(value, other.value); }

  public static int compare(double first, double second) {
    if (first < second) return -1;
    if (first > second) return 1;
    long a = doubleToLongBits(first), b = doubleToLongBits(second);
    return a == b ? 0 : (a < b ? -1 : 1);
  }

  public static double sum(double first, double second) { return first + second; }

  public static double min(double first, double second) { return Math.min(first, second); }

  public static double max(double first, double second) { return Math.max(first, second); }

  public static boolean isFinite(double value) {
    return !isNaN(value) && !isInfinite(value);
  }

  public String toString() {
    return toString(value);
  }

  public static String toString(double v) {
    byte[] buffer = new byte[32];
    int numChars = fillBufferWithDouble(v, buffer, buffer.length);
    return new String(buffer, 0, numChars, false);
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
    return (long) value;
  }

  public float floatValue() {
    return (float) value;
  }

  public double doubleValue() {
    return value;
  }

  public boolean isInfinite() {
    return isInfinite(value);
  }

  public boolean isNaN() {
    return isNaN(value);
  }

  public static double parseDouble(String s) {
    int[] numRead = new int[1];
    double d = doubleFromString(s, numRead);
    if (numRead[0] == 1) {
      return d;
    } else {
      throw new NumberFormatException(s);
    }
  }

  public static long doubleToLongBits(double value) {
    if (isNaN(value)) return 0x7ff8000000000000L;
    return doubleToRawLongBits(value);
  }

  public static native int fillBufferWithDouble(double value, byte[] buffer,
                                                int charCount);

  public static native long doubleToRawLongBits(double value);

  public static native double longBitsToDouble(long bits);

  public static native boolean isInfinite(double value);

  public static native boolean isNaN(double value);

  public static native double doubleFromString(String s, int[] numRead);

  @Override
  public java.util.Optional<Double> describeConstable() {
    return java.util.Optional.of(this);
  }

  @Override
  public Double resolveConstantDesc(java.lang.invoke.MethodHandles.Lookup lookup) {
    return this;
  }

  public static String toHexString(double d) {
    if (isNaN(d) || isInfinite(d)) {
      return toString(d);
    }
    StringBuilder answer = new StringBuilder(24);
    if (Math.copySign(1.0, d) == -1.0) {
      answer.append("-");
    }
    answer.append("0x");
    d = Math.abs(d);
    if (d == 0.0) {
      answer.append("0.0p0");
    } else {
      boolean subnormal = d < MIN_NORMAL;
      long significand = (doubleToLongBits(d) & 0x000FFFFFFFFFFFFFL) | 0x8000000000000000L;
      answer.append(subnormal ? "0." : "1.");
      String digits = Long.toHexString(significand).substring(3, 16);
      if (digits.equals("0000000000000")) {
        answer.append("0");
      } else {
        int end = digits.length();
        while (end > 1 && digits.charAt(end - 1) == '0') {
          --end;
        }
        answer.append(digits, 0, end);
      }
      answer.append('p');
      answer.append(subnormal ? MIN_EXPONENT : Math.getExponent(d));
    }
    return answer.toString();
  }
}
