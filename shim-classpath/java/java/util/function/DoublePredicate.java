/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.util.function;

@FunctionalInterface
public interface DoublePredicate {
    boolean test(double value);
    default DoublePredicate and(DoublePredicate other) {
        java.util.Objects.requireNonNull(other);
        return value -> test(value) && other.test(value);
    }
    default DoublePredicate or(DoublePredicate other) {
        java.util.Objects.requireNonNull(other);
        return value -> test(value) || other.test(value);
    }
    default DoublePredicate negate() { return value -> !test(value); }
}
