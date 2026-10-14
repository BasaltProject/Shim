/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

public class FloatingSigns {
    static void checkDouble(long bits, long signBits) {
        double value = Double.longBitsToDouble(bits), sign = Double.longBitsToDouble(signBits);
        System.out.println(Double.doubleToLongBits(Math.abs(value)) + ":" + Double.doubleToLongBits(Math.signum(value))
                + ":" + Double.doubleToRawLongBits(Math.copySign(value, sign)) + ":" + Math.getExponent(value));
    }
    static void checkFloat(int bits, int signBits) {
        float value = Float.intBitsToFloat(bits), sign = Float.intBitsToFloat(signBits);
        System.out.println(Float.floatToIntBits(Math.abs(value)) + ":" + Float.floatToIntBits(Math.signum(value))
                + ":" + Float.floatToRawIntBits(Math.copySign(value, sign)) + ":" + Math.getExponent(value));
    }
    public static void main(String[] args) {
        long[] doubles = {0, Long.MIN_VALUE, 1, 0x0010000000000000L, 0x7fefffffffffffffL,
                0x7ff0000000000000L, 0xfff0000000000000L, 0x7ff8000000000042L, 0xfff8000000000042L};
        int[] floats = {0, Integer.MIN_VALUE, 1, 0x00800000, 0x7f7fffff, 0x7f800000,
                0xff800000, 0x7fc00042, 0xffc00042};
        for (long bits : doubles) for (long sign : doubles) checkDouble(bits, sign);
        for (int bits : floats) for (int sign : floats) checkFloat(bits, sign);
        long seed = 25;
        for (int i = 0; i < 256; ++i) {
            seed = seed * 6364136223846793005L + 1442695040888963407L;
            checkDouble(seed, ~seed);
            checkFloat((int) (seed >>> 32), (int) ~seed);
        }
    }
}
