/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

public class FloatingRound {
    static void check(double value) {
        System.out.println(Long.toHexString(Double.doubleToRawLongBits(value)) + ":" + Math.round(value));
    }
    static void check(float value) {
        System.out.println(Integer.toHexString(Float.floatToRawIntBits(value)) + ":" + Math.round(value));
    }
    public static void main(String[] args) {
        double[] doubles = {0, -0.0, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY,
                Double.MIN_VALUE, -Double.MIN_VALUE, 0.5, -0.5, 1.5, -1.5, 0x1.0p52, 0x1.0p63, -0x1.0p63};
        for (double value : doubles) {
            check(value); check(Math.nextDown(value)); check(Math.nextUp(value));
        }
        float[] floats = {0, -0.0f, Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY,
                Float.MIN_VALUE, -Float.MIN_VALUE, 0.5f, -0.5f, 1.5f, -1.5f, 0x1.0p23f, 0x1.0p31f, -0x1.0p31f};
        for (float value : floats) {
            check(value); check(Math.nextDown(value)); check(Math.nextUp(value));
        }
        for (int i = -64; i <= 64; ++i) {
            double half = i + 0.5;
            check(half); check(Math.nextDown(half)); check(Math.nextUp(half));
            float halfFloat = i + 0.5f;
            check(halfFloat); check(Math.nextDown(halfFloat)); check(Math.nextUp(halfFloat));
        }
        long seed = 25;
        for (int i = 0; i < 256; ++i) {
            seed = seed * 6364136223846793005L + 1442695040888963407L;
            check(Double.longBitsToDouble(seed));
            check(Float.intBitsToFloat((int) (seed >>> 32)));
        }
    }
}
