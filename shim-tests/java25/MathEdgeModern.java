/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

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
  }
}
