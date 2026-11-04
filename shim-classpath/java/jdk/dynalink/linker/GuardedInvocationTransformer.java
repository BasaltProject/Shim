/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.dynalink.linker;

@FunctionalInterface
public interface GuardedInvocationTransformer {
  jdk.dynalink.linker.GuardedInvocation filter(jdk.dynalink.linker.GuardedInvocation guardedInvocation, jdk.dynalink.linker.LinkRequest linkRequest, jdk.dynalink.linker.LinkerServices linkerServices);
}
