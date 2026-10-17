/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.runtime;

final class ExactConversionsSupport {
  private ExactConversionsSupport() {
  }

  private static boolean isNegativeZero(float f) {
    return Float.floatToRawIntBits(f) == Integer.MIN_VALUE;
  }

  private static boolean isNegativeZero(double d) {
    return Double.doubleToRawLongBits(d) == Long.MIN_VALUE;
  }

  public static boolean isIntToByteExact(int n) {
    return n == (byte) n;
  }

  public static boolean isIntToShortExact(int n) {
    return n == (short) n;
  }

  public static boolean isIntToCharExact(int n) {
    return n == (char) n;
  }

  public static boolean isIntToFloatExact(int n) {
    return (long) (float) n == n;
  }

  public static boolean isLongToByteExact(long n) {
    return n == (byte) n;
  }

  public static boolean isLongToShortExact(long n) {
    return n == (short) n;
  }

  public static boolean isLongToCharExact(long n) {
    return n == (char) n;
  }

  public static boolean isLongToIntExact(long n) {
    return n == (int) n;
  }

  public static boolean isLongToFloatExact(long n) {
    float f = (float) n;
    return f != 0x1p63f && (long) f == n;
  }

  public static boolean isLongToDoubleExact(long n) {
    double d = (double) n;
    return d != 0x1p63 && (long) d == n;
  }

  public static boolean isFloatToByteExact(float f) {
    return f == (byte) f && !isNegativeZero(f);
  }

  public static boolean isFloatToShortExact(float f) {
    return f == (short) f && !isNegativeZero(f);
  }

  public static boolean isFloatToCharExact(float f) {
    return f == (char) f && !isNegativeZero(f);
  }

  public static boolean isFloatToIntExact(float f) {
    return (double) f == (double) (int) f && !isNegativeZero(f);
  }

  public static boolean isFloatToLongExact(float f) {
    return f != 0x1p63f && f == (long) f && !isNegativeZero(f);
  }

  public static boolean isDoubleToByteExact(double d) {
    return d == (byte) d && !isNegativeZero(d);
  }

  public static boolean isDoubleToShortExact(double d) {
    return d == (short) d && !isNegativeZero(d);
  }

  public static boolean isDoubleToCharExact(double d) {
    return d == (char) d && !isNegativeZero(d);
  }

  public static boolean isDoubleToIntExact(double d) {
    return d == (int) d && !isNegativeZero(d);
  }

  public static boolean isDoubleToLongExact(double d) {
    return d != 0x1p63 && d == (long) d && !isNegativeZero(d);
  }

  public static boolean isDoubleToFloatExact(double d) {
    return (double) (float) d == d || Double.isNaN(d);
  }
}
