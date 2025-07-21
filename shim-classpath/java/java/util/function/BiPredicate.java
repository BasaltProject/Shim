/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.util.function;

import java.util.Objects;

@FunctionalInterface
public interface BiPredicate<T, U> {
    boolean test(T first, U second);
    default BiPredicate<T, U> and(BiPredicate<? super T, ? super U> other) {
        Objects.requireNonNull(other);
        return (first, second) -> test(first, second) && other.test(first, second);
    }
    default BiPredicate<T, U> or(BiPredicate<? super T, ? super U> other) {
        Objects.requireNonNull(other);
        return (first, second) -> test(first, second) || other.test(first, second);
    }
    default BiPredicate<T, U> negate() { return (first, second) -> !test(first, second); }
}
