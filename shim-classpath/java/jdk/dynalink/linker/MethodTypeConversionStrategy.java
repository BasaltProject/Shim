/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.dynalink.linker;

@FunctionalInterface
public interface MethodTypeConversionStrategy {
  java.lang.invoke.MethodHandle asType(java.lang.invoke.MethodHandle methodHandle, java.lang.invoke.MethodType methodType);
}
