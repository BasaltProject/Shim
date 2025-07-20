/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.util.function;

import java.util.Objects;

@FunctionalInterface
public interface Function<T, R> {
    R apply(T value);
    default <V> Function<V, R> compose(Function<? super V, ? extends T> before) {
        Objects.requireNonNull(before);
        return value -> apply(before.apply(value));
    }
    default <V> Function<T, V> andThen(Function<? super R, ? extends V> after) {
        Objects.requireNonNull(after);
        return value -> after.apply(apply(value));
    }
    static <T> Function<T, T> identity() { return value -> value; }
}
