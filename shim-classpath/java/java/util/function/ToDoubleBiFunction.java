/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.util.function;

@FunctionalInterface
public interface ToDoubleBiFunction<T, U> {
  double applyAsDouble(T t, U u);
}
