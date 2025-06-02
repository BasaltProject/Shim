/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

public class CoreBytecode {
    private static int select(int value) {
        switch (value) {
            case -20: return 3;
            case 0: return 7;
            case 400: return 11;
            default: return -1;
        }
    }

    public static void main(String[] args) {
        long sum = 0;
        int[][] values = {{2, 3}, {5, 7}};
        for (int[] row : values) {
            for (int value : row) {
                sum += value;
            }
        }
        System.out.println(sum);
        System.out.println(select(-20) + select(400) + select(9));
        System.out.println(Float.floatToIntBits(-0.0f));
        System.out.println(Double.doubleToLongBits(Double.NaN));
        System.out.println((1234567890123L * 17L) >>> 4);
        try {
            int zero = args.length;
            System.out.println(10 / zero);
        } catch (ArithmeticException expected) {
            System.out.println("arithmetic");
        } finally {
            System.out.println("finally");
        }
    }
}
