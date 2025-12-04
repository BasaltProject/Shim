/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

public class MathEdgeModern {
  static int zero = 0;
  static long lzero = 0;
  static int m1 = -1;
  static long lm1 = -1;

  interface Op { Object run() throws Exception; }

  static void t(String name, Op op) {
    try {
      System.out.println(name + " = " + op.run());
    } catch (Exception e) {
      System.out.println(name + " ! " + e.getClass().getName() + ": " + e.getMessage());
    }
  }

  public static void main(String[] args) {
    t("div0", () -> 5 / zero);
    t("rem0", () -> 5L % lzero);
    t("minDiv", () -> Integer.MIN_VALUE / m1);
    t("minRem", () -> Long.MIN_VALUE % lm1);
    t("minLongDiv", () -> Long.MIN_VALUE / lm1);
    t("floorMod0", () -> Math.floorMod(1, zero));
    t("floorModMin", () -> Math.floorMod(Integer.MIN_VALUE, m1));
    t("ceilDiv0", () -> Math.ceilDiv(1L, lzero));
    t("clampInt", () -> Math.clamp(5L, 10, 1));
    t("clampDouble", () -> Math.clamp(1.0, 2.5, -1.5));
    t("clampNaNMin", () -> Math.clamp(1.0, Double.NaN, 1.0));
    t("clampNaNMax", () -> Math.clamp(1.0f, 0f, Float.NaN));
    t("powExactNeg", () -> Math.powExact(2, -1));
    t("unsignedPowExactNeg", () -> Math.unsignedPowExact(2L, -1));
    t("unsignedMulInt", () -> Math.unsignedMultiplyExact(-1, 2));
    t("unsignedMulLong", () -> Math.unsignedMultiplyExact(-1L, 2L));
    t("powOneNaN", () -> Math.pow(1.0, Double.NaN));
    t("powOneInf", () -> Math.pow(1.0, Double.POSITIVE_INFINITY));
    t("powMinusOneInf", () -> Math.pow(-1.0, Double.NEGATIVE_INFINITY));
    t("powNaNZero", () -> Math.pow(Double.NaN, 0.0));
    t("exp", () -> Math.exp(2.5) + " " + Math.exp(-1.5) + " " + Math.exp(3.5));
    t("strictExp", () -> StrictMath.exp(1.0) + " " + StrictMath.exp(-3.7) + " " + StrictMath.exp(300.5));
    t("strictSin", () -> StrictMath.sin(2.5) + " " + StrictMath.sin(1.0e22) + " " + StrictMath.sin(-8.08e208));
    t("strictCos", () -> StrictMath.cos(0.1) + " " + StrictMath.cos(1.5e300) + " " + StrictMath.cos(4.0e6));
    t("strictPow", () -> StrictMath.pow(2.5, 2.5) + " " + StrictMath.pow(-3.0, 7.0) + " " + StrictMath.pow(0.1, 33.3));
    t("strictAtan2", () -> StrictMath.atan2(1.0e10, -1.5) + " " + StrictMath.atan2(-0.0, -2.0));
    t("strictHyperbolic", () -> StrictMath.cosh(1.0) + " " + StrictMath.hypot(3.14159, 1.2345678912345679E8));
    t("strictCbrt", () -> StrictMath.cbrt(2.5) + " " + StrictMath.cbrt(-1.0e-310));
    t("strictTan", () -> StrictMath.tan(1.0) + " " + StrictMath.tan(1.0e22) + " " + StrictMath.tan(-0.7));
    t("strictInverse", () -> StrictMath.asin(0.3) + " " + StrictMath.acos(-0.9) + " " + StrictMath.atan(7.5));
    t("strictLog", () -> StrictMath.log(10.0) + " " + StrictMath.log10(2.0e-310) + " " + StrictMath.log1p(1.0e-5));
    t("strictExpm1", () -> StrictMath.expm1(15.118685898537043) + " " + StrictMath.sinh(-15.345947706271401));
    t("strictTanh", () -> StrictMath.tanh(-7.5038666715406155) + " " + StrictMath.tanh(0.3));
    t("clearBitHigh", () -> BigInteger.TEN.clearBit(Integer.MAX_VALUE));
    t("clearBitNeg", () -> BigInteger.TEN.negate().clearBit(Integer.MAX_VALUE));
    t("flipBitHigh", () -> BigInteger.TEN.flipBit(Integer.MAX_VALUE));
    t("divideZeroScale", () -> BigDecimal.ONE.divide(BigDecimal.ZERO, RoundingMode.DOWN));
    BigDecimal big = BigDecimal.valueOf(Long.MAX_VALUE).multiply(BigDecimal.TEN);
    t("divideZeroBig", () -> big.divide(BigDecimal.ZERO, 2, RoundingMode.DOWN));
    t("movePointLeftZero", () -> new BigDecimal("1E+5").movePointLeft(0));
  }
}
