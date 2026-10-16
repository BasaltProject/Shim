/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

// Minimum release: 21

public class PrimitivesAndFloat16Modern {
  public static void main(String[] args) throws Exception {
    testBitCompression();
    testCharSequenceParsing();
    testFloat16Conversions();
    testMathAndStrictMathModern();
    System.out.println("PASS: PrimitivesAndFloat16Modern");
  }

  private static void testBitCompression() {
    assertEquals(5, Integer.compress(0b101010, 0b111000));
    assertEquals(40, Integer.expand(5, 0b111000));

    assertEquals(0, Integer.compress(0x12345678, 0));
    assertEquals(0, Integer.expand(0x12345678, 0));

    assertEquals(0x12345678, Integer.compress(0x12345678, 0xFFFFFFFF));
    assertEquals(0x12345678, Integer.expand(0x12345678, 0xFFFFFFFF));

    int alternating = 0x55555555;
    int compressedAlt = Integer.compress(0xFFFFFFFF, alternating);
    assertEquals(0xFFFF, compressedAlt);
    assertEquals(alternating, Integer.expand(compressedAlt, alternating));

    assertEquals(5L, Long.compress(0b101010L, 0b111000L));
    assertEquals(40L, Long.expand(5L, 0b111000L));

    long mask64 = 0x8000000000000001L;
    long val64 = 0x8000000000000001L;
    assertEquals(3L, Long.compress(val64, mask64));
    assertEquals(val64, Long.expand(3L, mask64));

    assertEquals(0L, Long.compress(0x123456789ABCDEF0L, 0L));
    assertEquals(0L, Long.expand(0x123456789ABCDEF0L, 0L));
  }

  private static void testCharSequenceParsing() {
    CharSequence seq = "prefix_+420_-789_1234567890123_suffix";

    assertEquals(420, Integer.parseInt(seq, 7, 11, 10));
    assertEquals(-789, Integer.parseInt(seq, 12, 16, 10));
    assertEquals(420, Integer.parseUnsignedInt(seq, 7, 11, 10));

    assertEquals(420L, Long.parseLong(seq, 7, 11, 10));
    assertEquals(-789L, Long.parseLong(seq, 12, 16, 10));
    assertEquals(1234567890123L, Long.parseLong(seq, 17, 30, 10));
    assertEquals(1234567890123L, Long.parseUnsignedLong(seq, 17, 30, 10));

    CharSequence hexSeq = "value: 0xDEADBEEF";
    assertEquals(0xDEADBEEFL, Long.parseUnsignedLong(hexSeq, 9, 17, 16));

    try {
      Integer.parseInt(seq, 12, 16, 10);
      Integer.parseUnsignedInt(seq, 12, 16, 10);
      throw new AssertionError("Expected NumberFormatException for negative unsigned");
    } catch (NumberFormatException expected) {
      // Expected
    }

    try {
      Integer.parseInt(seq, 0, 100, 10);
      throw new AssertionError("Expected IndexOutOfBoundsException");
    } catch (IndexOutOfBoundsException expected) {
      // Expected
    }
  }

  private static void testFloat16Conversions() {
    assertEquals((short) 0x0000, Float.floatToFloat16(0.0f));
    assertEquals((short) 0x8000, Float.floatToFloat16(-0.0f));
    assertEquals((short) 0x3C00, Float.floatToFloat16(1.0f));
    assertEquals((short) 0xBC00, Float.floatToFloat16(-1.0f));
    assertEquals((short) 0x3E00, Float.floatToFloat16(1.5f));
    assertEquals((short) 0x7C00, Float.floatToFloat16(Float.POSITIVE_INFINITY));
    assertEquals((short) 0xFC00, Float.floatToFloat16(Float.NEGATIVE_INFINITY));

    short nan16 = Float.floatToFloat16(Float.NaN);
    assertTrue((nan16 & 0x7C00) == 0x7C00);
    assertTrue((nan16 & 0x03FF) != 0);

    assertEquals((short) 0x7BFF, Float.floatToFloat16(65504.0f));
    assertEquals((short) 0x7C00, Float.floatToFloat16(65520.0f));
    assertEquals((short) 0x7C00, Float.floatToFloat16(100000.0f));

    assertEquals(0.0f, Float.float16ToFloat((short) 0x0000));
    assertEquals(-0.0f, Float.float16ToFloat((short) 0x8000));
    assertEquals(1.0f, Float.float16ToFloat((short) 0x3C00));
    assertEquals(-1.0f, Float.float16ToFloat((short) 0xBC00));
    assertEquals(1.5f, Float.float16ToFloat((short) 0x3E00));
    assertEquals(Float.POSITIVE_INFINITY, Float.float16ToFloat((short) 0x7C00));
    assertEquals(Float.NEGATIVE_INFINITY, Float.float16ToFloat((short) 0xFC00));
    assertTrue(Float.isNaN(Float.float16ToFloat((short) 0x7E00)));

    float subnormal = Float.float16ToFloat((short) 0x0001);
    assertTrue(subnormal > 0.0f);
    assertEquals((short) 0x0001, Float.floatToFloat16(subnormal));
  }

  private static void testMathAndStrictMathModern() {
    assertEquals(2.0 * Math.PI, Math.TAU);
    assertEquals(Math.TAU, StrictMath.TAU);

    assertEquals(5, Math.divideExact(10, 2));
    assertEquals(5L, Math.divideExact(10L, 2L));
    assertEquals(5, StrictMath.divideExact(10, 2));
    assertEquals(5L, StrictMath.divideExact(10L, 2L));

    try {
      Math.divideExact(Integer.MIN_VALUE, -1);
      throw new AssertionError("Expected ArithmeticException");
    } catch (ArithmeticException expected) {
      // Expected
    }

    try {
      StrictMath.divideExact(Long.MIN_VALUE, -1L);
      throw new AssertionError("Expected ArithmeticException");
    } catch (ArithmeticException expected) {
      // Expected
    }

    assertEquals(7.0, Math.fma(2.0, 3.0, 1.0));
    assertEquals(7.0f, Math.fma(2.0f, 3.0f, 1.0f));
    assertEquals(7.0, StrictMath.fma(2.0, 3.0, 1.0));
    assertEquals(7.0f, StrictMath.fma(2.0f, 3.0f, 1.0f));

    assertEquals(20, Math.clamp(25L, 10, 20));
    assertEquals(20L, Math.clamp(25L, 10L, 20L));
    assertEquals(20, StrictMath.clamp(25L, 10, 20));
    assertEquals(20L, StrictMath.clamp(25L, 10L, 20L));
  }

  private static void assertEquals(int expected, int actual) {
    if (expected != actual) {
      throw new AssertionError("Expected: " + expected + ", actual: " + actual);
    }
  }

  private static void assertEquals(long expected, long actual) {
    if (expected != actual) {
      throw new AssertionError("Expected: " + expected + ", actual: " + actual);
    }
  }

  private static void assertEquals(short expected, short actual) {
    if (expected != actual) {
      throw new AssertionError("Expected: 0x" + Integer.toHexString(expected & 0xFFFF)
          + ", actual: 0x" + Integer.toHexString(actual & 0xFFFF));
    }
  }

  private static void assertEquals(float expected, float actual) {
    if (Float.floatToIntBits(expected) != Float.floatToIntBits(actual)) {
      throw new AssertionError("Expected: " + expected + ", actual: " + actual);
    }
  }

  private static void assertEquals(double expected, double actual) {
    if (Double.doubleToLongBits(expected) != Double.doubleToLongBits(actual)) {
      throw new AssertionError("Expected: " + expected + ", actual: " + actual);
    }
  }

  private static void assertTrue(boolean condition) {
    if (!condition) {
      throw new AssertionError("Expected true condition");
    }
  }
}
