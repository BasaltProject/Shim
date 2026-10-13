/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

public class LambdaCalls {
    interface Operation { int apply(int value); }
    static int twice(int value) { return value * 2; }
    public static void main(String[] args) {
        Operation lambda = value -> value + 7;
        Operation reference = LambdaCalls::twice;
        int captured = 13;
        Operation closure = value -> value + captured;
        System.out.println(lambda.apply(5));
        System.out.println(reference.apply(11));
        System.out.println(closure.apply(17));
    }
}
