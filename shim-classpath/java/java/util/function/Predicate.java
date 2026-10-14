/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.util.function;

import java.util.Objects;

@FunctionalInterface
public interface Predicate<T> {
    boolean test(T value);
    default Predicate<T> and(Predicate<? super T> other) {
        Objects.requireNonNull(other);
        return value -> test(value) && other.test(value);
    }
    default Predicate<T> or(Predicate<? super T> other) {
        Objects.requireNonNull(other);
        return value -> test(value) || other.test(value);
    }
    default Predicate<T> negate() { return value -> !test(value); }
    static <T> Predicate<T> isEqual(Object target) { return value -> Objects.equals(target, value); }
    @SuppressWarnings("unchecked")
    static <T> Predicate<T> not(Predicate<? super T> target) {
        return (Predicate<T>) Objects.requireNonNull(target).negate();
    }
}
