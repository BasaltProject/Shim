/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

public class LambdaCalls {
    interface Operation { int apply(int value); }
    interface Slice { String apply(String value, int offset); }
    int offset(int value) { return value + 3; }
    static int twice(int value) { return value * 2; }
    public static void main(String[] args) {
        Operation lambda = value -> value + 7;
        Operation reference = LambdaCalls::twice;
        int captured = 13;
        Operation closure = value -> value + captured;
        System.out.println(lambda.apply(5));
        System.out.println(reference.apply(11));
        System.out.println(closure.apply(17));
        Operation bound = new LambdaCalls()::offset;
        Slice unbound = String::substring;
        System.out.println(bound.apply(9));
        System.out.println(unbound.apply("modern", 2));
    }
}
