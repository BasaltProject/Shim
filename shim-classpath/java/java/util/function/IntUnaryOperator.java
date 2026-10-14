/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.util.function;

@FunctionalInterface
public interface IntUnaryOperator {
    int applyAsInt(int value);
    default IntUnaryOperator compose(IntUnaryOperator before) {
        java.util.Objects.requireNonNull(before);
        return value -> applyAsInt(before.applyAsInt(value));
    }
    default IntUnaryOperator andThen(IntUnaryOperator after) {
        java.util.Objects.requireNonNull(after);
        return value -> after.applyAsInt(applyAsInt(value));
    }
    static IntUnaryOperator identity() { return value -> value; }
}
