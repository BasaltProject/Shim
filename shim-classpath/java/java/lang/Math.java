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

import java.util.Random;

public final class Math {
  public static final double E = 2.718281828459045;
  public static final double PI = 3.141592653589793;

  private static class Static {
    public static final Random random = new Random();
  }

  private Math() { }

  public static double max(double a, double b) {
    if (Double.isNaN(a)) return a;
    if (Double.isNaN(b)) return b;
    return Double.compare(a, b) >= 0 ? a : b;
  }

  public static double min(double a, double b) {
    if (Double.isNaN(a)) return a;
    if (Double.isNaN(b)) return b;
    return Double.compare(a, b) <= 0 ? a : b;
  }

  public static float max(float a, float b) {
    if (Float.isNaN(a)) return a;
    if (Float.isNaN(b)) return b;
    return Float.compare(a, b) >= 0 ? a : b;
  }

  public static float min(float a, float b) {
    if (Float.isNaN(a)) return a;
    if (Float.isNaN(b)) return b;
    return Float.compare(a, b) <= 0 ? a : b;
  }

  public static int clamp(long value, int minimum, int maximum) {
    if (minimum > maximum) throw new IllegalArgumentException("Invalid clamp range");
    return (int) max((long) minimum, min(value, (long) maximum));
  }

  public static long clamp(long value, long minimum, long maximum) {
    if (minimum > maximum) throw new IllegalArgumentException("Invalid clamp range");
    return max(minimum, min(value, maximum));
  }

  public static float clamp(float value, float minimum, float maximum) {
    if (Float.isNaN(minimum) || Float.isNaN(maximum) || Float.compare(minimum, maximum) > 0) {
      throw new IllegalArgumentException("Invalid clamp range");
    }
    return max(minimum, min(value, maximum));
  }

  public static double clamp(double value, double minimum, double maximum) {
    if (Double.isNaN(minimum) || Double.isNaN(maximum) || Double.compare(minimum, maximum) > 0) {
      throw new IllegalArgumentException("Invalid clamp range");
    }
    return max(minimum, min(value, maximum));
  }

  public static long max(long a, long b) {
    return (a < b ? b : a);
  }

  public static long min(long a, long b) {
    return (a > b ? b : a);
  }

  public static int max(int a, int b) {
    return (a < b ? b : a);
  }

  public static int min(int a, int b) {
    return (a > b ? b : a);
  }

  public static int abs(int v) {
    return (v < 0 ? -v : v);
  }

  public static long abs(long v) {
    return (v < 0 ? -v : v);
  }

  public static float abs(float v) {
    return Float.intBitsToFloat(Float.floatToRawIntBits(v) & 0x7fffffff);
  }

  public static double abs(double v) {
    return Double.longBitsToDouble(Double.doubleToRawLongBits(v) & 0x7fffffffffffffffL);
  }

  public static long round(double v) {
    return (long) Math.floor(v + 0.5);
  }

  public static int round(float v) {
    return (int) Math.floor(v + 0.5);
  }

  public static double signum(double d) {
    return d == 0 || Double.isNaN(d) ? d : copySign(1.0, d);
  }

  public static float signum(float f) {
    return f == 0 || Float.isNaN(f) ? f : copySign(1.0f, f);
  }

  public static double copySign(double magnitude, double sign) {
    long bits = (Double.doubleToRawLongBits(magnitude) & 0x7fffffffffffffffL)
        | (Double.doubleToRawLongBits(sign) & 0x8000000000000000L);
    return Double.longBitsToDouble(bits);
  }

  public static float copySign(float magnitude, float sign) {
    int bits = (Float.floatToRawIntBits(magnitude) & 0x7fffffff)
        | (Float.floatToRawIntBits(sign) & 0x80000000);
    return Float.intBitsToFloat(bits);
  }

  public static int getExponent(double value) {
    return (int) ((Double.doubleToRawLongBits(value) >>> 52) & 0x7ff) - 1023;
  }

  public static int getExponent(float value) {
    return ((Float.floatToRawIntBits(value) >>> 23) & 0xff) - 127;
  }

  public static float nextUp(float value) {
    if (Float.isNaN(value) || value == Float.POSITIVE_INFINITY) return value;
    if (value == 0) return Float.MIN_VALUE;
    int bits = Float.floatToRawIntBits(value);
    return Float.intBitsToFloat(value > 0 ? bits + 1 : bits - 1);
  }

  public static float nextDown(float value) {
    if (Float.isNaN(value) || value == Float.NEGATIVE_INFINITY) return value;
    if (value == 0) return Float.intBitsToFloat(0x80000001);
    int bits = Float.floatToRawIntBits(value);
    return Float.intBitsToFloat(value > 0 ? bits - 1 : bits + 1);
  }

  public static float nextAfter(float start, double direction) {
    if (Float.isNaN(start) || Double.isNaN(direction)) return Float.NaN;
    if (start == direction) return (float) direction;
    return start < direction ? nextUp(start) : nextDown(start);
  }

  public static float ulp(float value) {
    int exponent = getExponent(value);
    if (exponent == Float.MAX_EXPONENT + 1) return abs(value);
    if (exponent == Float.MIN_EXPONENT - 1) return Float.MIN_VALUE;
    int distanceExponent = exponent - 23;
    if (distanceExponent >= Float.MIN_EXPONENT) {
      return Float.intBitsToFloat(((int) (distanceExponent + 127)) << 23);
    }
    return Float.intBitsToFloat((int) 1 << (distanceExponent - Float.MIN_EXPONENT + 23));
  }

  public static double nextUp(double value) {
    if (Double.isNaN(value) || value == Double.POSITIVE_INFINITY) return value;
    if (value == 0) return Double.MIN_VALUE;
    long bits = Double.doubleToRawLongBits(value);
    return Double.longBitsToDouble(value > 0 ? bits + 1 : bits - 1);
  }

  public static double nextDown(double value) {
    if (Double.isNaN(value) || value == Double.NEGATIVE_INFINITY) return value;
    if (value == 0) return Double.longBitsToDouble(0x8000000000000001L);
    long bits = Double.doubleToRawLongBits(value);
    return Double.longBitsToDouble(value > 0 ? bits - 1 : bits + 1);
  }

  public static double nextAfter(double start, double direction) {
    if (Double.isNaN(start) || Double.isNaN(direction)) return Double.NaN;
    if (start == direction) return (double) direction;
    return start < direction ? nextUp(start) : nextDown(start);
  }

  public static double ulp(double value) {
    int exponent = getExponent(value);
    if (exponent == Double.MAX_EXPONENT + 1) return abs(value);
    if (exponent == Double.MIN_EXPONENT - 1) return Double.MIN_VALUE;
    int distanceExponent = exponent - 52;
    if (distanceExponent >= Double.MIN_EXPONENT) {
      return Double.longBitsToDouble(((long) (distanceExponent + 1023)) << 52);
    }
    return Double.longBitsToDouble((long) 1 << (distanceExponent - Double.MIN_EXPONENT + 52));
  }

  public static double random() {
    return Static.random.nextDouble();
  }

  public static native double floor(double v);

  public static native double ceil(double v);

  public static native double exp(double v);

  public static native double log(double v);

  public static native double cos(double v);

  public static native double sin(double v);

  public static native double tan(double v);

  public static native double cosh(double v);

  public static native double sinh(double v);

  public static native double tanh(double v);

  public static native double acos(double v);

  public static native double asin(double v);

  public static native double atan(double v);

  public static native double atan2(double y, double x);

  public static native double sqrt(double v);

  public static native double pow(double v, double e);
}
