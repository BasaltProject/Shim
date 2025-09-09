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

public final class Short extends Number implements Comparable<Short>, java.lang.constant.Constable {
  public static final Class TYPE = shim.Classes.forCanonicalName("S");

  public static final short MIN_VALUE = -32768;
  public static final short MAX_VALUE = 32767;

  public static final int SIZE = 16;
  public static final int BYTES = 2;

  private final short value;

  public Short(short value) {
    this.value = value;
  }

  public static Short valueOf(short value) {
    return new Short(value);
  }

  public int compareTo(Short o) {
    return value - o.value;
  }

  public boolean equals(Object o) {
    return o instanceof Short && ((Short) o).value == value;
  }

  public int hashCode() {
    return hashCode(value);
  }

  public static int hashCode(short value) {
    return (int) value;
  }

  @Override
  public java.util.Optional<java.lang.constant.DynamicConstantDesc<Short>> describeConstable() {
    return java.util.Optional.of(java.lang.constant.DynamicConstantDesc.ofNamed(
      java.lang.constant.ConstantDescs.BSM_EXPLICIT_CAST,
      java.lang.constant.ConstantDescs.DEFAULT_NAME,
      java.lang.constant.ConstantDescs.CD_short,
      intValue()));
  }

  public String toString() {
    return toString(value);
  }

  public static String toString(short v, int radix) {
    return Long.toString(v, radix);
  }

  public static String toString(short v) {
    return toString(v, 10);
  }

  public byte byteValue() {
    return (byte) value;
  }

  public short shortValue() {
    return value;
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

  public static int toUnsignedInt(short x) {
    return ((int) x) & 0xffff;
  }

  public static long toUnsignedLong(short x) {
    return ((long) x) & 0xffffL;
  }

  public static int compareUnsigned(short x, short y) {
    return toUnsignedInt(x) - toUnsignedInt(y);
  }

  public static int compare(short x, short y) {
    return x - y;
  }

  public static short reverseBytes(short i) {
    return (short) (((i & 0xFF00) >> 8) | (i << 8));
  }
}
