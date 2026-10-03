/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.dynalink.linker;

@FunctionalInterface
public interface MethodHandleTransformer {
  java.lang.invoke.MethodHandle transform(java.lang.invoke.MethodHandle methodHandle);
}
