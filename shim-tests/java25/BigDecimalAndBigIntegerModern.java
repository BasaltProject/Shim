/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

// Minimum release: 17

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Arrays;

public class BigDecimalAndBigIntegerModern {

  public static void main(String[] args) throws Exception {
    testBigIntegerConstructorsAndRadix();
    testBigIntegerArithmeticAndPow();
    testBigIntegerBitsAndSqrt();
    testBigIntegerNumberAndComparisons();
    testBigDecimalConstructorsAndParsing();
    testBigDecimalArithmeticAndRounding();
    testBigDecimalScalingAndFormatting();
    testBigDecimalExactConversionsAndExceptions();
    testMathContextString();
    System.out.println("BigDecimalAndBigIntegerModern passed successfully");
  }

  private static void testBigIntegerConstructorsAndRadix() {
    BigInteger a = new BigInteger("12345678901234567890");
    System.out.println("a: " + a);
    System.out.println("a radix 16: " + a.toString(16));

    BigInteger hex = new BigInteger("-deadbeef", 16);
    System.out.println("hex: " + hex);
    System.out.println("hex radix 16: " + hex.toString(16));

    byte[] bytes = a.toByteArray();
    BigInteger fromBytes = new BigInteger(bytes);
    System.out.println("fromBytes equals a: " + a.equals(fromBytes));

    byte[] negBytes = hex.toByteArray();
    BigInteger fromNegBytes = new BigInteger(negBytes);
    System.out.println("fromNegBytes equals hex: " + hex.equals(fromNegBytes));

    BigInteger signMag = new BigInteger(-1, new byte[] {1, 2, 3});
    System.out.println("signMag: " + signMag);
  }

  private static void testBigIntegerArithmeticAndPow() {
    BigInteger x = new BigInteger("987654321987654321");
    BigInteger y = new BigInteger("123456789");

    System.out.println("add: " + x.add(y));
    System.out.println("sub: " + x.subtract(y));
    System.out.println("mul: " + x.multiply(y));

    BigInteger[] qr = x.divideAndRemainder(y);
    System.out.println("div: " + qr[0]);
    System.out.println("rem: " + qr[1]);
    System.out.println("check div/rem: " + qr[0].multiply(y).add(qr[1]).equals(x));

    BigInteger p = BigInteger.valueOf(3).pow(10);
    System.out.println("3^10: " + p);

    BigInteger gcdVal = x.gcd(y);
    System.out.println("gcd: " + gcdVal);

    BigInteger modPowVal = BigInteger.valueOf(7).modPow(BigInteger.valueOf(13), BigInteger.valueOf(100));
    System.out.println("7^13 mod 100: " + modPowVal);
  }

  private static void testBigIntegerBitsAndSqrt() {
    BigInteger v = BigInteger.valueOf(12345);
    System.out.println("bitLength: " + v.bitLength());
    System.out.println("bitCount: " + v.bitCount());
    System.out.println("testBit 0: " + v.testBit(0));
    System.out.println("getLowestSetBit: " + v.getLowestSetBit());

    BigInteger setB = v.setBit(1);
    System.out.println("setBit 1: " + setB);
    System.out.println("clearBit 0: " + v.clearBit(0));
    System.out.println("flipBit 0: " + v.flipBit(0));

    BigInteger a = BigInteger.valueOf(0b1100);
    BigInteger b = BigInteger.valueOf(0b1010);
    System.out.println("and: " + a.and(b).toString(2));
    System.out.println("or: " + a.or(b).toString(2));
    System.out.println("xor: " + a.xor(b).toString(2));
    System.out.println("not: " + BigInteger.valueOf(0).not());

    BigInteger sqrtVal = new BigInteger("1000000").sqrt();
    System.out.println("sqrt 1000000: " + sqrtVal);
    BigInteger[] sqrtRem = new BigInteger("1000005").sqrtAndRemainder();
    System.out.println("sqrt rem q: " + sqrtRem[0] + " r: " + sqrtRem[1]);
  }

  private static void testBigIntegerNumberAndComparisons() {
    BigInteger bi = BigInteger.valueOf(42);
    Number num = bi;
    System.out.println("num int: " + num.intValue());
    System.out.println("num long: " + num.longValue());
    System.out.println("num float: " + num.floatValue());
    System.out.println("num double: " + num.doubleValue());

    BigInteger small = BigInteger.valueOf(10);
    BigInteger large = BigInteger.valueOf(20);
    System.out.println("min: " + small.min(large));
    System.out.println("max: " + small.max(large));
    System.out.println("cmp: " + small.compareTo(large));
  }

  private static void testBigDecimalConstructorsAndParsing() {
    BigDecimal d1 = new BigDecimal("123.450");
    System.out.println("d1: " + d1);
    System.out.println("d1 scale: " + d1.scale());
    System.out.println("d1 precision: " + d1.precision());

    BigDecimal d2 = new BigDecimal("-0.0050");
    System.out.println("d2: " + d2);
    System.out.println("d2 scale: " + d2.scale());

    BigDecimal exp1 = new BigDecimal("1.23E4");
    System.out.println("exp1: " + exp1);
    System.out.println("exp1 plain: " + exp1.toPlainString());

    BigDecimal exp2 = new BigDecimal("1.23e-2");
    System.out.println("exp2: " + exp2);
    System.out.println("exp2 plain: " + exp2.toPlainString());

    BigDecimal fromDouble = BigDecimal.valueOf(0.125);
    System.out.println("fromDouble: " + fromDouble);

    BigDecimal fromLong = BigDecimal.valueOf(123456, 3);
    System.out.println("fromLong scale 3: " + fromLong);
  }

  private static void testBigDecimalArithmeticAndRounding() {
    BigDecimal a = new BigDecimal("10.50");
    BigDecimal b = new BigDecimal("3.25");

    System.out.println("add: " + a.add(b));
    System.out.println("sub: " + a.subtract(b));
    System.out.println("mul: " + a.multiply(b));

    BigDecimal divHalfUp = a.divide(b, 4, RoundingMode.HALF_UP);
    System.out.println("div half_up: " + divHalfUp);

    BigDecimal divHalfEven = a.divide(b, 4, RoundingMode.HALF_EVEN);
    System.out.println("div half_even: " + divHalfEven);

    BigDecimal divDown = a.divide(b, 4, RoundingMode.DOWN);
    System.out.println("div down: " + divDown);

    BigDecimal exact = new BigDecimal("1.00").divide(new BigDecimal("0.25"));
    System.out.println("exact div: " + exact);

    BigDecimal intPart = a.divideToIntegralValue(b);
    BigDecimal rem = a.remainder(b);
    System.out.println("integral: " + intPart);
    System.out.println("rem: " + rem);
    System.out.println("check integral/rem: " + (intPart.multiply(b).add(rem).compareTo(a) == 0));

    BigDecimal p = new BigDecimal("2.5").pow(3);
    System.out.println("2.5^3: " + p);
  }

  private static void testBigDecimalScalingAndFormatting() {
    BigDecimal val = new BigDecimal("123.45600");
    System.out.println("stripped: " + val.stripTrailingZeros());
    System.out.println("stripped scale: " + val.stripTrailingZeros().scale());

    BigDecimal setS = val.setScale(2, RoundingMode.HALF_UP);
    System.out.println("setScale 2 half_up: " + setS);

    BigDecimal mLeft = new BigDecimal("123.45").movePointLeft(2);
    System.out.println("moveLeft 2: " + mLeft);

    BigDecimal mRight = new BigDecimal("123.45").movePointRight(2);
    System.out.println("moveRight 2: " + mRight);

    BigDecimal eng = new BigDecimal("12345.67");
    System.out.println("eng: " + eng.toEngineeringString());
    BigDecimal smallEng = new BigDecimal("0.0000123");
    System.out.println("small eng: " + smallEng.toEngineeringString());

    System.out.println("ulp: " + val.ulp());
  }

  private static void testBigDecimalExactConversionsAndExceptions() {
    BigDecimal bi = new BigDecimal("12345");
    System.out.println("toBigInteger: " + bi.toBigInteger());
    System.out.println("toBigIntegerExact: " + bi.toBigIntegerExact());
    System.out.println("intValueExact: " + bi.intValueExact());
    System.out.println("longValueExact: " + bi.longValueExact());

    try {
      new BigDecimal("123.45").toBigIntegerExact();
      System.out.println("Expected ArithmeticException on non-exact toBigInteger");
    } catch (ArithmeticException e) {
      System.out.println("caught non-exact toBigInteger: true");
    }

    try {
      BigDecimal.ONE.divide(new BigDecimal("3"));
      System.out.println("Expected ArithmeticException on non-terminating divide");
    } catch (ArithmeticException e) {
      System.out.println("caught non-terminating divide: true");
    }

    try {
      BigDecimal.ONE.divide(BigDecimal.ZERO);
      System.out.println("Expected ArithmeticException on divide by zero");
    } catch (ArithmeticException e) {
      System.out.println("caught divide by zero: true");
    }
  }

  private static void testMathContextString() {
    MathContext mc = new MathContext("precision=7 roundingMode=HALF_EVEN");
    System.out.println("mc precision: " + mc.getPrecision());
    System.out.println("mc rounding: " + mc.getRoundingMode());
    System.out.println("mc equals DECIMAL32: " + mc.equals(MathContext.DECIMAL32));

    BigDecimal rounded = new BigDecimal("1.23456789").round(mc);
    System.out.println("rounded with mc: " + rounded);
  }
}
