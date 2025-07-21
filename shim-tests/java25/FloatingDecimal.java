/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

public class FloatingDecimal {
    static void checkDouble(long bits) {
        double value = Double.longBitsToDouble(bits);
        String text = Double.toString(value);
        if (!Double.isNaN(value) && Double.doubleToRawLongBits(Double.parseDouble(text)) != bits)
            throw new AssertionError(Long.toHexString(bits));
        if (!text.equals(String.valueOf(value)) || !text.equals(new StringBuilder().append(value).toString()))
            throw new AssertionError(text);
        System.out.println(Long.toHexString(bits) + ":" + text);
    }
    static void checkFloat(int bits) {
        float value = Float.intBitsToFloat(bits);
        String text = Float.toString(value);
        if (!Float.isNaN(value) && Float.floatToRawIntBits(Float.parseFloat(text)) != bits)
            throw new AssertionError(Integer.toHexString(bits));
        if (!text.equals(String.valueOf(value)) || !text.equals(new StringBuilder().append(value).toString()))
            throw new AssertionError(text);
        System.out.println(Integer.toHexString(bits) + ":" + text);
    }
    public static void main(String[] args) {
        long[] doubles = {0, Long.MIN_VALUE, 1, 2, 3, 0x0010000000000000L,
                0x7fefffffffffffffL, 0x7ff0000000000000L, 0xfff0000000000000L,
                0x7ff8000000000042L, Double.doubleToRawLongBits(7.0), Double.doubleToRawLongBits(0.1),
                Double.doubleToRawLongBits(1e23), Double.doubleToRawLongBits(0.001), Double.doubleToRawLongBits(1e7)};
        for (long bits : doubles) checkDouble(bits);
        int[] floats = {0, Integer.MIN_VALUE, 1, 2, 3, 0x00800000, 0x7f7fffff, 0x7f800000,
                0xff800000, 0x7fc00042, Float.floatToRawIntBits(7.0f), Float.floatToRawIntBits(0.1f)};
        for (int bits : floats) checkFloat(bits);
        long seed = 25;
        for (int i = 0; i < 256; ++i) {
            seed = seed * 6364136223846793005L + 1442695040888963407L;
            checkDouble(seed);
            checkFloat((int) (seed >>> 32));
        }
        for (long bits : new long[] {0, 1, -1, Long.MIN_VALUE, Long.MAX_VALUE}) {
            System.out.println(Long.toHexString(bits));
            System.out.println(Long.toOctalString(bits));
            System.out.println(Long.toBinaryString(bits));
        }
        System.out.println("concat " + 7.0 + " " + 0.1f + " " + Double.MIN_VALUE);
        System.out.println(7.0);
        System.out.println(0.1f);
    }
}
