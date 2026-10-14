/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.util.function;

@FunctionalInterface
public interface DoubleUnaryOperator {
    double applyAsDouble(double value);
    default DoubleUnaryOperator compose(DoubleUnaryOperator before) {
        java.util.Objects.requireNonNull(before);
        return value -> applyAsDouble(before.applyAsDouble(value));
    }
    default DoubleUnaryOperator andThen(DoubleUnaryOperator after) {
        java.util.Objects.requireNonNull(after);
        return value -> after.applyAsDouble(applyAsDouble(value));
    }
    static DoubleUnaryOperator identity() { return value -> value; }
}
