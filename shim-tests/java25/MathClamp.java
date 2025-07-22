/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

// Minimum release: 21
public class MathClamp {
    public static void main(String[] args) {
        long[] integers = {Long.MIN_VALUE, -1, 0, 1, Long.MAX_VALUE};
        for (long value : integers) for (long low : integers) for (long high : integers) {
            try { System.out.println(Math.clamp(value, low, high)); }
            catch (IllegalArgumentException expected) { System.out.println("invalid long range"); }
            int min = low < Integer.MIN_VALUE ? Integer.MIN_VALUE : (low > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) low);
            int max = high < Integer.MIN_VALUE ? Integer.MIN_VALUE : (high > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) high);
            try { System.out.println(Math.clamp(value, min, max)); }
            catch (IllegalArgumentException expected) { System.out.println("invalid int range"); }
        }
        {
            float[] values = {Float.NEGATIVE_INFINITY, -1, -0.0f, 0, 1,
                    Float.POSITIVE_INFINITY, Float.NaN};
            for (float value : values) for (float low : values) for (float high : values) {
                try { System.out.println(Float.floatToIntBits(Math.clamp(value, low, high))); }
                catch (IllegalArgumentException expected) { System.out.println("invalid float range"); }
            }
        }
        {
            double[] values = {Double.NEGATIVE_INFINITY, -1, -0.0, 0, 1,
                    Double.POSITIVE_INFINITY, Double.NaN};
            for (double value : values) for (double low : values) for (double high : values) {
                try { System.out.println(Double.doubleToLongBits(Math.clamp(value, low, high))); }
                catch (IllegalArgumentException expected) { System.out.println("invalid double range"); }
            }
        }
    }
}
