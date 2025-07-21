/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.util.function;

import java.util.Objects;

@FunctionalInterface
public interface BiFunction<T, U, R> {
    R apply(T first, U second);
    default <V> BiFunction<T, U, V> andThen(Function<? super R, ? extends V> after) {
        Objects.requireNonNull(after);
        return (first, second) -> after.apply(apply(first, second));
    }
}
