/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

public class FloatingMinMax {
    public static void main(String[] args) {
        {
            float[] values = {Float.NEGATIVE_INFINITY, -1, -0.0f, 0, 1,
                    Float.POSITIVE_INFINITY, Float.NaN};
            for (float a : values) for (float b : values) {
                System.out.println(Float.floatToIntBits(Math.min(a, b)) + ":" + Float.floatToIntBits(Math.max(a, b)));
                System.out.println(Float.floatToIntBits(Float.min(a, b)) + ":" + Float.floatToIntBits(Float.max(a, b)));
                System.out.println(Float.floatToIntBits(Float.sum(a, b)));
            }
        }
        {
            double[] values = {Double.NEGATIVE_INFINITY, -1, -0.0, 0, 1,
                    Double.POSITIVE_INFINITY, Double.NaN};
            for (double a : values) for (double b : values) {
                System.out.println(Double.doubleToLongBits(Math.min(a, b)) + ":" + Double.doubleToLongBits(Math.max(a, b)));
                System.out.println(Double.doubleToLongBits(Double.min(a, b)) + ":" + Double.doubleToLongBits(Double.max(a, b)));
                System.out.println(Double.doubleToLongBits(Double.sum(a, b)));
            }
        }
    }
}
