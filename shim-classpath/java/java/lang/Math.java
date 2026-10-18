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
  public static final double TAU = 2.0 * PI;

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
    if (minimum > maximum) throw new IllegalArgumentException(minimum + " > " + maximum);
    return (int) max((long) minimum, min(value, (long) maximum));
  }

  public static long clamp(long value, long minimum, long maximum) {
    if (minimum > maximum) throw new IllegalArgumentException(minimum + " > " + maximum);
    return max(minimum, min(value, maximum));
  }

  public static float clamp(float value, float minimum, float maximum) {
    if (Float.isNaN(minimum)) {
      throw new IllegalArgumentException("min is NaN");
    }
    if (Float.isNaN(maximum)) {
      throw new IllegalArgumentException("max is NaN");
    }
    if (Float.compare(minimum, maximum) > 0) {
      throw new IllegalArgumentException(minimum + " > " + maximum);
    }
    return max(minimum, min(value, maximum));
  }

  public static double clamp(double value, double minimum, double maximum) {
    if (Double.isNaN(minimum)) {
      throw new IllegalArgumentException("min is NaN");
    }
    if (Double.isNaN(maximum)) {
      throw new IllegalArgumentException("max is NaN");
    }
    if (Double.compare(minimum, maximum) > 0) {
      throw new IllegalArgumentException(minimum + " > " + maximum);
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

  public static long round(double value) {
    int exponent = getExponent(value);
    if (exponent >= 52) return (long) value;
    if (exponent < -1) return 0;
    long bits = Double.doubleToRawLongBits(value);
    return roundSignificand((bits & 0x000fffffffffffffL) | 0x0010000000000000L, 52 - exponent, bits < 0);
  }

  public static int round(float value) {
    int exponent = getExponent(value);
    if (exponent >= 23) return (int) value;
    if (exponent < -1) return 0;
    int bits = Float.floatToRawIntBits(value);
    return (int) roundSignificand((bits & 0x007fffff) | 0x00800000, 23 - exponent, bits < 0);
  }

  private static long roundSignificand(long significand, int shift, boolean negative) {
    long integral = significand >>> shift;
    long remainder = significand & ((1L << shift) - 1);
    long half = 1L << (shift - 1);
    if (negative) return -integral - (remainder > half ? 1 : 0);
    return integral + (remainder >= half ? 1 : 0);
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

  public static double cosh(double v) {
    return shim.FdLibm.cosh(v);
  }

  public static native double sinh(double v);

  public static native double tanh(double v);

  public static native double acos(double v);

  public static native double asin(double v);

  public static native double atan(double v);

  public static double atan2(double y, double x) {
    return shim.FdLibm.atan2(y, x);
  }

  public static native double sqrt(double v);

  public static native double pow(double v, double e);

  public static int addExact(int x, int y) {
    int r = x + y;
    if (((x ^ r) & (y ^ r)) < 0) {
      throw new ArithmeticException("integer overflow");
    }
    return r;
  }

  public static long addExact(long x, long y) {
    long r = x + y;
    if (((x ^ r) & (y ^ r)) < 0) {
      throw new ArithmeticException("long overflow");
    }
    return r;
  }

  public static int subtractExact(int x, int y) {
    int r = x - y;
    if (((x ^ y) & (x ^ r)) < 0) {
      throw new ArithmeticException("integer overflow");
    }
    return r;
  }

  public static long subtractExact(long x, long y) {
    long r = x - y;
    if (((x ^ y) & (x ^ r)) < 0) {
      throw new ArithmeticException("long overflow");
    }
    return r;
  }

  public static int multiplyExact(int x, int y) {
    long r = (long) x * (long) y;
    if ((int) r != r) {
      throw new ArithmeticException("integer overflow");
    }
    return (int) r;
  }

  public static long multiplyExact(long x, int y) {
    return multiplyExact(x, (long) y);
  }

  public static long multiplyExact(long x, long y) {
    long r = x * y;
    long ax = Math.abs(x);
    long ay = Math.abs(y);
    if (((ax | ay) >>> 31 != 0)) {
      if (((y != 0) && (r / y != x)) ||
          (x == Long.MIN_VALUE && y == -1)) {
        throw new ArithmeticException("long overflow");
      }
    }
    return r;
  }

  public static int incrementExact(int a) {
    if (a == Integer.MAX_VALUE) {
      throw new ArithmeticException("integer overflow");
    }
    return a + 1;
  }

  public static long incrementExact(long a) {
    if (a == Long.MAX_VALUE) {
      throw new ArithmeticException("long overflow");
    }
    return a + 1;
  }

  public static int decrementExact(int a) {
    if (a == Integer.MIN_VALUE) {
      throw new ArithmeticException("integer overflow");
    }
    return a - 1;
  }

  public static long decrementExact(long a) {
    if (a == Long.MIN_VALUE) {
      throw new ArithmeticException("long overflow");
    }
    return a - 1;
  }

  public static int negateExact(int a) {
    if (a == Integer.MIN_VALUE) {
      throw new ArithmeticException("integer overflow");
    }
    return -a;
  }

  public static long negateExact(long a) {
    if (a == Long.MIN_VALUE) {
      throw new ArithmeticException("long overflow");
    }
    return -a;
  }

  public static int toIntExact(long value) {
    if ((int) value != value) {
      throw new ArithmeticException("integer overflow");
    }
    return (int) value;
  }

  public static long multiplyHigh(long x, long y) {
    long x1 = x >> 32;
    long x2 = x & 0xFFFFFFFFL;
    long y1 = y >> 32;
    long y2 = y & 0xFFFFFFFFL;

    long z2 = x2 * y2;
    long t = x1 * y2 + (z2 >>> 32);
    long z1 = t & 0xFFFFFFFFL;
    long z0 = t >> 32;
    z1 += x2 * y1;

    return x1 * y1 + z0 + (z1 >> 32);
  }

  public static long unsignedMultiplyHigh(long x, long y) {
    long x1 = x >>> 32;
    long x2 = x & 0xFFFFFFFFL;
    long y1 = y >>> 32;
    long y2 = y & 0xFFFFFFFFL;

    long z2 = x2 * y2;
    long t = x1 * y2 + (z2 >>> 32);
    long z1 = t & 0xFFFFFFFFL;
    long z0 = t >>> 32;
    z1 += x2 * y1;

    return x1 * y1 + z0 + (z1 >>> 32);
  }

  public static int floorDiv(int x, int y) {
    int r = x / y;
    if ((x ^ y) < 0 && (r * y != x)) {
      r--;
    }
    return r;
  }

  public static int floorDivExact(int x, int y) {
    if ((x == Integer.MIN_VALUE) && (y == -1)) {
      throw new ArithmeticException("integer overflow");
    }
    int r = x / y;
    if ((x ^ y) < 0 && (r * y != x)) {
      r--;
    }
    return r;
  }

  public static int ceilDiv(int x, int y) {
    int r = x / y;
    if ((x ^ y) >= 0 && (r * y != x)) {
      r++;
    }
    return r;
  }

  public static int ceilDivExact(int x, int y) {
    if ((x == Integer.MIN_VALUE) && (y == -1)) {
      throw new ArithmeticException("integer overflow");
    }
    int r = x / y;
    if ((x ^ y) >= 0 && (r * y != x)) {
      r++;
    }
    return r;
  }

  public static int floorMod(int x, int y) {
    int r = x % y;
    if ((x ^ y) < 0 && r != 0) {
      r += y;
    }
    return r;
  }

  public static int ceilMod(int x, int y) {
    int r = x % y;
    if ((x ^ y) >= 0 && r != 0) {
      r -= y;
    }
    return r;
  }

  public static long floorDiv(long x, int y) {
    return floorDiv(x, (long) y);
  }

  public static long floorDiv(long x, long y) {
    long r = x / y;
    if ((x ^ y) < 0 && (r * y != x)) {
      r--;
    }
    return r;
  }

  public static long floorDivExact(long x, long y) {
    if ((x == Long.MIN_VALUE) && (y == -1)) {
      throw new ArithmeticException("long overflow");
    }
    long r = x / y;
    if ((x ^ y) < 0 && (r * y != x)) {
      r--;
    }
    return r;
  }

  public static int floorMod(long x, int y) {
    return (int) floorMod(x, (long) y);
  }

  public static long floorMod(long x, long y) {
    long r = x % y;
    if ((x ^ y) < 0 && r != 0) {
      r += y;
    }
    return r;
  }

  public static long ceilDiv(long x, int y) {
    return ceilDiv(x, (long) y);
  }

  public static long ceilDiv(long x, long y) {
    long r = x / y;
    if ((x ^ y) >= 0 && (r * y != x)) {
      r++;
    }
    return r;
  }

  public static long ceilDivExact(long x, long y) {
    if ((x == Long.MIN_VALUE) && (y == -1)) {
      throw new ArithmeticException("long overflow");
    }
    long r = x / y;
    if ((x ^ y) >= 0 && (r * y != x)) {
      r++;
    }
    return r;
  }

  public static int ceilMod(long x, int y) {
    return (int) ceilMod(x, (long) y);
  }

  public static long ceilMod(long x, long y) {
    long r = x % y;
    if ((x ^ y) >= 0 && r != 0) {
      r -= y;
    }
    return r;
  }

  public static int divideExact(int x, int y) {
    if (x == Integer.MIN_VALUE && y == -1) {
      throw new ArithmeticException("integer overflow");
    }
    return x / y;
  }

  public static long divideExact(long x, long y) {
    if (x == Long.MIN_VALUE && y == -1L) {
      throw new ArithmeticException("long overflow");
    }
    return x / y;
  }

  public static double fma(double a, double b, double c) {
    if (Double.isNaN(a) || Double.isNaN(b) || Double.isNaN(c)) {
      return Double.NaN;
    }
    boolean aInf = Double.isInfinite(a);
    boolean bInf = Double.isInfinite(b);
    boolean cInf = Double.isInfinite(c);
    if (aInf || bInf || cInf) {
      if ((aInf && b == 0.0) || (bInf && a == 0.0)) {
        return Double.NaN;
      }
      double prod = a * b;
      if (Double.isInfinite(prod) && !aInf && !bInf) {
        return c;
      }
      return prod + c;
    }
    java.math.BigDecimal bda = new java.math.BigDecimal(a);
    java.math.BigDecimal bdb = new java.math.BigDecimal(b);
    java.math.BigDecimal prod = bda.multiply(bdb);
    if (c == 0.0) {
      if (a == 0.0 || b == 0.0) {
        return a * b + c;
      }
      return prod.doubleValue();
    }
    return prod.add(new java.math.BigDecimal(c)).doubleValue();
  }

  public static float fma(float a, float b, float c) {
    if (Float.isFinite(a) && Float.isFinite(b) && Float.isFinite(c)) {
      if ((double) a == 0.0 || (double) b == 0.0) {
        return a * b + c;
      }
      java.math.BigDecimal prod = new java.math.BigDecimal((double) a * (double) b);
      return prod.add(new java.math.BigDecimal((double) c)).floatValue();
    }
    return (float) fma((double) a, (double) b, (double) c);
  }

  public static native double log10(double v);

  public static double cbrt(double x) {
    final int b1 = 715094163;
    final int b2 = 696219795;
    final double c = 5.42857142857142815906e-01;
    final double d = -7.05306122448979611050e-01;
    final double e = 1.41428571428571436819e+00;
    final double f = 1.60714285714285720630e+00;
    final double g = 3.57142857142857150787e-01;
    long bits = Double.doubleToRawLongBits(x);
    int hx = (int) (bits >>> 32);
    int sign = hx & 0x80000000;
    hx ^= sign;
    if (hx >= 0x7ff00000) {
      return x + x;
    }
    if ((hx | (int) bits) == 0) {
      return x;
    }
    x = Double.longBitsToDouble(((long) hx << 32) | (bits & 0xffffffffL));
    double t = 0.0;
    if (hx < 0x00100000) {
      t = Double.longBitsToDouble(0x43500000L << 32);
      t *= x;
      long tb = Double.doubleToRawLongBits(t);
      t = Double.longBitsToDouble((((long) ((int) (tb >>> 32) / 3 + b2)) << 32) | (tb & 0xffffffffL));
    } else {
      t = Double.longBitsToDouble((long) (hx / 3 + b1) << 32);
    }
    double r = t * t / x;
    double s = c + r * t;
    t *= g + f / (s + e + d / s);
    long tb = Double.doubleToRawLongBits(t);
    t = Double.longBitsToDouble((((long) ((int) (tb >>> 32) + 1)) << 32));
    s = t * t;
    r = x / s;
    double w = t + t;
    r = (r - t) / (w + r);
    t = t + t * r;
    tb = Double.doubleToRawLongBits(t);
    return Double.longBitsToDouble(tb | ((long) sign << 32));
  }

  public static native double expm1(double v);

  public static native double log1p(double v);

  public static double hypot(double x, double y) {
    return shim.FdLibm.hypot(x, y);
  }

  public static native double IEEEremainder(double dividend, double divisor);

  private static final double DEGREES_TO_RADIANS = 0.017453292519943295;
  private static final double RADIANS_TO_DEGREES = 57.29577951308232;

  public static double toRadians(double angdeg) {
    return angdeg * DEGREES_TO_RADIANS;
  }

  public static double toDegrees(double angrad) {
    return angrad * RADIANS_TO_DEGREES;
  }

  public static double rint(double a) {
    double twoToThe52 = (double) (1L << 52);
    double sign = copySign(1.0, a);
    a = abs(a);
    if (a < twoToThe52) {
      a = (twoToThe52 + a) - twoToThe52;
    }
    return sign * a;
  }

  private static double powerOfTwoD(int n) {
    return Double.longBitsToDouble((((long) n + (long) Double.MAX_EXPONENT) << 52) & 0x7FF0000000000000L);
  }

  public static double scalb(double d, int scaleFactor) {
    final int maxScale = Double.MAX_EXPONENT + -Double.MIN_EXPONENT + 53 + 1;
    final double twoToTheDoubleScaleUp = powerOfTwoD(512);
    final double twoToTheDoubleScaleDown = powerOfTwoD(-512);
    int scaleIncrement;
    double expDelta;
    if (scaleFactor < 0) {
      scaleFactor = max(scaleFactor, -maxScale);
      scaleIncrement = -512;
      expDelta = twoToTheDoubleScaleDown;
    } else {
      scaleFactor = min(scaleFactor, maxScale);
      scaleIncrement = 512;
      expDelta = twoToTheDoubleScaleUp;
    }
    int t = (scaleFactor >> 9 - 1) >>> 32 - 9;
    int expAdjust = ((scaleFactor + t) & (512 - 1)) - t;
    d *= powerOfTwoD(expAdjust);
    scaleFactor -= expAdjust;
    while (scaleFactor != 0) {
      d *= expDelta;
      scaleFactor -= scaleIncrement;
    }
    return d;
  }

  public static float scalb(float f, int scaleFactor) {
    final int maxScale = Float.MAX_EXPONENT + -Float.MIN_EXPONENT + 24 + 1;
    scaleFactor = max(min(scaleFactor, maxScale), -maxScale);
    return (float) ((double) f * powerOfTwoD(scaleFactor));
  }

  public static long multiplyFull(int x, int y) {
    return (long) x * (long) y;
  }

  public static int absExact(int a) {
    if (a == Integer.MIN_VALUE) {
      throw new ArithmeticException("Overflow to represent absolute value of Integer.MIN_VALUE");
    }
    return abs(a);
  }

  public static long absExact(long a) {
    if (a == Long.MIN_VALUE) {
      throw new ArithmeticException("Overflow to represent absolute value of Long.MIN_VALUE");
    }
    return abs(a);
  }

  public static int unsignedMultiplyExact(int x, int y) {
    long r = Integer.toUnsignedLong(x) * Integer.toUnsignedLong(y);
    if ((r >>> 32) != 0) {
      throw new ArithmeticException("unsigned integer overflow");
    }
    return (int) r;
  }

  public static long unsignedMultiplyExact(long x, int y) {
    return unsignedMultiplyExact(x, Integer.toUnsignedLong(y));
  }

  public static long unsignedMultiplyExact(long x, long y) {
    long hi = unsignedMultiplyHigh(x, y);
    if (hi != 0) {
      throw new ArithmeticException("unsigned long overflow");
    }
    return x * y;
  }

  public static int powExact(int base, int exponent) {
    if (exponent < 0) {
      throw new ArithmeticException("negative exponent");
    }
    int result = 1;
    while (exponent > 0) {
      if ((exponent & 1) == 1) {
        result = multiplyExact(result, base);
      }
      exponent >>= 1;
      if (exponent > 0) {
        base = multiplyExact(base, base);
      }
    }
    return result;
  }

  public static long powExact(long base, int exponent) {
    if (exponent < 0) {
      throw new ArithmeticException("negative exponent");
    }
    long result = 1;
    while (exponent > 0) {
      if ((exponent & 1) == 1) {
        result = multiplyExact(result, base);
      }
      exponent >>= 1;
      if (exponent > 0) {
        base = multiplyExact(base, base);
      }
    }
    return result;
  }

  public static int unsignedPowExact(int base, int exponent) {
    if (exponent < 0) {
      throw new ArithmeticException("negative exponent");
    }
    int result = 1;
    while (exponent > 0) {
      if ((exponent & 1) == 1) {
        result = unsignedMultiplyExact(result, base);
      }
      exponent >>= 1;
      if (exponent > 0) {
        base = unsignedMultiplyExact(base, base);
      }
    }
    return result;
  }

  public static long unsignedPowExact(long base, int exponent) {
    if (exponent < 0) {
      throw new ArithmeticException("negative exponent");
    }
    long result = 1;
    while (exponent > 0) {
      if ((exponent & 1) == 1) {
        result = unsignedMultiplyExact(result, base);
      }
      exponent >>= 1;
      if (exponent > 0) {
        base = unsignedMultiplyExact(base, base);
      }
    }
    return result;
  }
}
