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

public final class Float extends Number implements Comparable<Float> {
  public static final Class TYPE = shim.Classes.forCanonicalName("F");
  private static final int EXP_BIT_MASK = 0x7F800000;
  private static final int SIGNIF_BIT_MASK = 0x007FFFFF;

  public static final float NEGATIVE_INFINITY = -1.0f / 0.0f;
  public static final float POSITIVE_INFINITY =  1.0f / 0.0f;
  public static final float NaN =  0.0f / 0.0f;

  public static final float MIN_VALUE = 0x0.000002p-126f;
  public static final float MAX_VALUE = 3.40282346638528859812e+38f;

  public static final float MIN_NORMAL = 0x1.0p-126f;
  public static final int MIN_EXPONENT = -126;
  public static final int MAX_EXPONENT = 127;
  public static final int SIZE = 32;
  public static final int BYTES = 4;
  public static final int PRECISION = 24;

  private final float value;  

  public Float(String value) {
    this.value = parseFloat(value);
  }

  public Float(float value) {
    this.value = value;
  }

  public static Float valueOf(float value) {
    return new Float(value);
  }

  public static Float valueOf(String s) {
    return new Float(s);
  }

  public boolean equals(Object o) {
    return o instanceof Float && floatToIntBits(((Float) o).value) == floatToIntBits(value);
  }

  public int hashCode() { return hashCode(value); }

  public static int hashCode(float value) {
    int bits = floatToIntBits(value);
    return bits;
  }

  public int compareTo(Float other) { return compare(value, other.value); }

  public static int compare(float first, float second) {
    if (first < second) return -1;
    if (first > second) return 1;
    int a = floatToIntBits(first), b = floatToIntBits(second);
    return a == b ? 0 : (a < b ? -1 : 1);
  }

  public static boolean isFinite(float value) {
    return !isNaN(value) && !isInfinite(value);
  }

  public String toString() {
    return toString(value);
  }

  public static String toString(float v) {
    byte[] buffer = new byte[24];
    int length = fillBufferWithFloat(v, buffer);
    return new String(buffer, 0, length, false);
  }

  private static native int fillBufferWithFloat(float value, byte[] buffer);

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
    return value;
  }

  public double doubleValue() {
    return (double) value;
  }

  public boolean isInfinite() {
    return isInfinite(value);
  }

  public boolean isNaN() {
    return isNaN(value);
  }

  public static float parseFloat(String s) {
    int[] numRead = new int[1];
    float f = floatFromString(s, numRead);
    if (numRead[0] == 1) {
      return f;
    } else {
      throw new NumberFormatException(s);
    }
  }
  
  public static int floatToIntBits(float value) {
    int result = floatToRawIntBits(value);
    
    // Check for NaN based on values of bit fields, maximum
    // exponent and nonzero significand.
    if (((result & EXP_BIT_MASK) == EXP_BIT_MASK) && (result & SIGNIF_BIT_MASK) != 0) {
      result = 0x7fc00000;
    }
    return result;
  }

  public static native int floatToRawIntBits(float value);

  public static native float intBitsToFloat(int bits);

  public static native boolean isInfinite(float value);

  public static native boolean isNaN(float value);

  public static native float floatFromString(String s, int[] numRead);
}
