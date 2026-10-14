/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.util.function;

@FunctionalInterface
public interface IntPredicate {
    boolean test(int value);
    default IntPredicate and(IntPredicate other) {
        java.util.Objects.requireNonNull(other);
        return value -> test(value) && other.test(value);
    }
    default IntPredicate or(IntPredicate other) {
        java.util.Objects.requireNonNull(other);
        return value -> test(value) || other.test(value);
    }
    default IntPredicate negate() { return value -> !test(value); }
}
