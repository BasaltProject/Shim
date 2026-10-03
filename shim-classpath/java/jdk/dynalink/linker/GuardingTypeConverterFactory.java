/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.dynalink.linker;

public interface GuardingTypeConverterFactory {
  jdk.dynalink.linker.GuardedInvocation convertToType(java.lang.Class<?> p0, java.lang.Class<?> p1, java.util.function.Supplier<java.lang.invoke.MethodHandles.Lookup> supplier) throws java.lang.Exception;
}
