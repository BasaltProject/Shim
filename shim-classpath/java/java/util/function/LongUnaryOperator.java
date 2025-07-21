/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.util.function;

@FunctionalInterface
public interface LongUnaryOperator {
    long applyAsLong(long value);
    default LongUnaryOperator compose(LongUnaryOperator before) {
        java.util.Objects.requireNonNull(before);
        return value -> applyAsLong(before.applyAsLong(value));
    }
    default LongUnaryOperator andThen(LongUnaryOperator after) {
        java.util.Objects.requireNonNull(after);
        return value -> after.applyAsLong(applyAsLong(value));
    }
    static LongUnaryOperator identity() { return value -> value; }
}
