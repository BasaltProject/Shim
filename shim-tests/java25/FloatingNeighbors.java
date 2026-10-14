/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

public class FloatingNeighbors {
    static void checkDouble(long bits, double direction) {
        double value = Double.longBitsToDouble(bits);
        System.out.println(Double.doubleToLongBits(Math.nextUp(value)) + ":" + Double.doubleToLongBits(Math.nextDown(value))
                + ":" + Double.doubleToLongBits(Math.nextAfter(value, direction)) + ":" + Double.doubleToLongBits(Math.ulp(value)));
    }
    static void checkFloat(int bits, double direction) {
        float value = Float.intBitsToFloat(bits);
        System.out.println(Float.floatToIntBits(Math.nextUp(value)) + ":" + Float.floatToIntBits(Math.nextDown(value))
                + ":" + Float.floatToIntBits(Math.nextAfter(value, direction)) + ":" + Float.floatToIntBits(Math.ulp(value)));
    }
    public static void main(String[] args) {
        long[] doubles = {0, Long.MIN_VALUE, 1, 0x8000000000000001L, 0x0010000000000000L, 0x7fefffffffffffffL,
                0x7ff0000000000000L, 0xfff0000000000000L, 0x7ff8000000000042L};
        int[] floats = {0, Integer.MIN_VALUE, 1, 0x80000001, 0x00800000, 0x7f7fffff, 0x7f800000,
                0xff800000, 0x7fc00042};
        double[] directions = {-0.0, 0.0, -1, 1, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, Double.NaN};
        for (long bits : doubles) for (double direction : directions) checkDouble(bits, direction);
        for (int bits : floats) for (double direction : directions) checkFloat(bits, direction);
        for (int exponent = 1; exponent < 2047; exponent += 17) {
            long bits = (long) exponent << 52;
            checkDouble(bits - 1, 0); checkDouble(bits, 0); checkDouble(bits + 1, 0);
        }
        for (int exponent = 1; exponent < 255; exponent += 7) {
            int bits = exponent << 23;
            checkFloat(bits - 1, 0); checkFloat(bits, 0); checkFloat(bits + 1, 0);
        }
        long seed = 25;
        for (int i = 0; i < 128; ++i) {
            seed = seed * 6364136223846793005L + 1442695040888963407L;
            checkDouble(seed, 0);
            checkFloat((int) (seed >>> 32), Double.longBitsToDouble(seed));
        }
        checkFloat(Float.floatToIntBits(1.0f), 1.0 + 0x1.0p-52);
        checkFloat(Float.floatToIntBits(1.0f), 1.0 - 0x1.0p-52);
    }
}
