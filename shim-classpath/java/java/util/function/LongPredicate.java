/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.util.function;

@FunctionalInterface
public interface LongPredicate {
    boolean test(long value);
    default LongPredicate and(LongPredicate other) {
        java.util.Objects.requireNonNull(other);
        return value -> test(value) && other.test(value);
    }
    default LongPredicate or(LongPredicate other) {
        java.util.Objects.requireNonNull(other);
        return value -> test(value) || other.test(value);
    }
    default LongPredicate negate() { return value -> !test(value); }
}
