/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.util.function;

import java.util.Objects;

@FunctionalInterface
public interface BinaryOperator<T> extends BiFunction<T, T, T> {
    static <T> BinaryOperator<T> minBy(java.util.Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator);
        return (first, second) -> comparator.compare(first, second) <= 0 ? first : second;
    }
    static <T> BinaryOperator<T> maxBy(java.util.Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator);
        return (first, second) -> comparator.compare(first, second) >= 0 ? first : second;
    }
}
