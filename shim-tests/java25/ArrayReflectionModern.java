/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

import java.lang.reflect.Array;

public class ArrayReflectionModern {
  public static void main(String[] args) {
    testPrimitiveGetters();
    testPrimitiveWideningGetters();
    testPrimitiveSetters();
    testPrimitiveWideningSetters();
    testGetLength();
    testMultiDimensionalNewInstance();
    testExceptionContracts();
  }

  private static void testPrimitiveGetters() {
    boolean[] z = new boolean[] { true, false };
    expectEquals(true, Array.getBoolean(z, 0));
    expectEquals(false, Array.getBoolean(z, 1));

    byte[] b = new byte[] { 10, -20 };
    expectEquals((byte) 10, Array.getByte(b, 0));
    expectEquals((byte) -20, Array.getByte(b, 1));

    char[] c = new char[] { 'A', 'Z' };
    expectEquals('A', Array.getChar(c, 0));
    expectEquals('Z', Array.getChar(c, 1));

    short[] s = new short[] { 1000, -2000 };
    expectEquals((short) 1000, Array.getShort(s, 0));
    expectEquals((short) -2000, Array.getShort(s, 1));

    int[] i = new int[] { 100000, -200000 };
    expectEquals(100000, Array.getInt(i, 0));
    expectEquals(-200000, Array.getInt(i, 1));

    long[] j = new long[] { 9999999999L, -8888888888L };
    expectEquals(9999999999L, Array.getLong(j, 0));
    expectEquals(-8888888888L, Array.getLong(j, 1));

    float[] f = new float[] { 1.5f, -2.5f };
    expectEquals(1.5f, Array.getFloat(f, 0));
    expectEquals(-2.5f, Array.getFloat(f, 1));

    double[] d = new double[] { 3.14159, -2.71828 };
    expectEquals(3.14159, Array.getDouble(d, 0));
    expectEquals(-2.71828, Array.getDouble(d, 1));

    System.out.println("PASS: primitive getters");
  }

  private static void testPrimitiveWideningGetters() {
    byte[] b = new byte[] { 42 };
    expectEquals((short) 42, Array.getShort(b, 0));
    expectEquals(42, Array.getInt(b, 0));
    expectEquals(42L, Array.getLong(b, 0));
    expectEquals(42.0f, Array.getFloat(b, 0));
    expectEquals(42.0, Array.getDouble(b, 0));

    short[] s = new short[] { 300 };
    expectEquals(300, Array.getInt(s, 0));
    expectEquals(300L, Array.getLong(s, 0));
    expectEquals(300.0, Array.getDouble(s, 0));

    char[] c = new char[] { 'X' };
    expectEquals((int) 'X', Array.getInt(c, 0));
    expectEquals((long) 'X', Array.getLong(c, 0));

    int[] i = new int[] { 77777 };
    expectEquals(77777L, Array.getLong(i, 0));
    expectEquals(77777.0, Array.getDouble(i, 0));

    float[] f = new float[] { 2.25f };
    expectEquals(2.25, Array.getDouble(f, 0));

    System.out.println("PASS: primitive widening getters");
  }

  private static void testPrimitiveSetters() {
    boolean[] z = new boolean[2];
    Array.setBoolean(z, 0, true);
    expectEquals(true, z[0]);

    byte[] b = new byte[2];
    Array.setByte(b, 0, (byte) 55);
    expectEquals((byte) 55, b[0]);

    char[] c = new char[2];
    Array.setChar(c, 0, 'K');
    expectEquals('K', c[0]);

    short[] s = new short[2];
    Array.setShort(s, 0, (short) 1234);
    expectEquals((short) 1234, s[0]);

    int[] i = new int[2];
    Array.setInt(i, 0, 54321);
    expectEquals(54321, i[0]);

    long[] j = new long[2];
    Array.setLong(j, 0, 12345678901L);
    expectEquals(12345678901L, j[0]);

    float[] f = new float[2];
    Array.setFloat(f, 0, 9.75f);
    expectEquals(9.75f, f[0]);

    double[] d = new double[2];
    Array.setDouble(d, 0, 8.125);
    expectEquals(8.125, d[0]);

    System.out.println("PASS: primitive setters");
  }

  private static void testPrimitiveWideningSetters() {
    double[] d = new double[6];
    Array.setByte(d, 0, (byte) 10);
    expectEquals(10.0, d[0]);
    Array.setShort(d, 1, (short) 20);
    expectEquals(20.0, d[1]);
    Array.setChar(d, 2, 'A');
    expectEquals((double) 'A', d[2]);
    Array.setInt(d, 3, 30);
    expectEquals(30.0, d[3]);
    Array.setLong(d, 4, 40L);
    expectEquals(40.0, d[4]);
    Array.setFloat(d, 5, 50.5f);
    expectEquals(50.5, d[5]);

    long[] j = new long[3];
    Array.setByte(j, 0, (byte) 1);
    expectEquals(1L, j[0]);
    Array.setShort(j, 1, (short) 2);
    expectEquals(2L, j[1]);
    Array.setInt(j, 2, 3);
    expectEquals(3L, j[2]);

    System.out.println("PASS: primitive widening setters");
  }

  private static void testGetLength() {
    int[] arr = new int[5];
    expectEquals(5, Array.getLength(arr));

    String[] sArr = new String[8];
    expectEquals(8, Array.getLength(sArr));

    try {
      Array.getLength(null);
      throw new AssertionError("Expected NullPointerException for null array in getLength");
    } catch (NullPointerException expected) {
      // Expected
    }

    try {
      Array.getLength("not-an-array");
      throw new AssertionError("Expected IllegalArgumentException for non-array in getLength");
    } catch (IllegalArgumentException expected) {
      // Expected
    }

    System.out.println("PASS: getLength");
  }

  private static void testMultiDimensionalNewInstance() {
    // 2D int array
    Object i2 = Array.newInstance(int.class, 3, 4);
    expectEquals("[[I", i2.getClass().getName());
    int[][] i2Arr = (int[][]) i2;
    expectEquals(3, i2Arr.length);
    expectEquals(4, i2Arr[0].length);
    expectEquals(4, i2Arr[1].length);
    expectEquals(4, i2Arr[2].length);
    i2Arr[1][2] = 42;
    expectEquals(42, i2Arr[1][2]);

    // 3D String array
    Object s3 = Array.newInstance(String.class, 2, 3, 4);
    expectEquals("[[[Ljava.lang.String;", s3.getClass().getName());
    String[][][] s3Arr = (String[][][]) s3;
    expectEquals(2, s3Arr.length);
    expectEquals(3, s3Arr[0].length);
    expectEquals(4, s3Arr[0][0].length);
    s3Arr[1][2][3] = "Shim";
    expectEquals("Shim", s3Arr[1][2][3]);

    // Zero-dimension outer
    Object zeroOuter = Array.newInstance(double.class, 0, 5);
    expectEquals("[[D", zeroOuter.getClass().getName());
    expectEquals(0, Array.getLength(zeroOuter));

    System.out.println("PASS: multi-dimensional newInstance");
  }

  private static void testExceptionContracts() {
    int[] arr = new int[2];

    // Narrowing rejection in setters
    try {
      Array.setInt(new byte[2], 0, 100);
      throw new AssertionError("Expected IllegalArgumentException for narrowing setInt on byte[]");
    } catch (IllegalArgumentException expected) {
      // Expected
    }

    try {
      Array.setDouble(new float[2], 0, 1.0);
      throw new AssertionError("Expected IllegalArgumentException for narrowing setDouble on float[]");
    } catch (IllegalArgumentException expected) {
      // Expected
    }

    // Narrowing rejection in getters
    try {
      Array.getByte(new int[] { 10 }, 0);
      throw new AssertionError("Expected IllegalArgumentException for narrowing getByte on int[]");
    } catch (IllegalArgumentException expected) {
      // Expected
    }

    // Non-array rejection in getters/setters
    try {
      Array.getInt("not-array", 0);
      throw new AssertionError("Expected IllegalArgumentException for getInt on String");
    } catch (IllegalArgumentException expected) {
      // Expected
    }

    try {
      Array.setInt("not-array", 0, 1);
      throw new AssertionError("Expected IllegalArgumentException for setInt on String");
    } catch (IllegalArgumentException expected) {
      // Expected
    }

    // Null checks in getters/setters
    try {
      Array.getInt(null, 0);
      throw new AssertionError("Expected NullPointerException for getInt on null");
    } catch (NullPointerException expected) {
      // Expected
    }

    try {
      Array.setInt(null, 0, 1);
      throw new AssertionError("Expected NullPointerException for setInt on null");
    } catch (NullPointerException expected) {
      // Expected
    }

    // newInstance exceptions
    try {
      Array.newInstance(null, 5);
      throw new AssertionError("Expected NullPointerException for null componentType in newInstance");
    } catch (NullPointerException expected) {
      // Expected
    }

    try {
      Array.newInstance(void.class, 5);
      throw new AssertionError("Expected IllegalArgumentException for void in newInstance");
    } catch (IllegalArgumentException expected) {
      // Expected
    }

    try {
      Array.newInstance(int.class, new int[0]);
      throw new AssertionError("Expected IllegalArgumentException for 0 dimensions in newInstance");
    } catch (IllegalArgumentException expected) {
      // Expected
    }

    try {
      Array.newInstance(int.class, 2, -1);
      throw new AssertionError("Expected NegativeArraySizeException for negative dimension in newInstance");
    } catch (NegativeArraySizeException expected) {
      // Expected
    }

    System.out.println("PASS: exception contracts");
  }

  private static void expectEquals(Object expected, Object actual) {
    if (expected == null && actual == null) return;
    if (expected != null && expected.equals(actual)) return;
    throw new AssertionError("Expected " + expected + " but got " + actual);
  }

  private static void expectEquals(long expected, long actual) {
    if (expected != actual) {
      throw new AssertionError("Expected " + expected + " but got " + actual);
    }
  }

  private static void expectEquals(double expected, double actual) {
    if (Double.doubleToLongBits(expected) != Double.doubleToLongBits(actual)) {
      throw new AssertionError("Expected " + expected + " but got " + actual);
    }
  }
}
