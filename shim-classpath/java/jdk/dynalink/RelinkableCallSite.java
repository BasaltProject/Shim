/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.dynalink;

public interface RelinkableCallSite {
  jdk.dynalink.CallSiteDescriptor getDescriptor();
  void initialize(java.lang.invoke.MethodHandle methodHandle);
  void relink(jdk.dynalink.linker.GuardedInvocation guardedInvocation, java.lang.invoke.MethodHandle methodHandle);
  void resetAndRelink(jdk.dynalink.linker.GuardedInvocation guardedInvocation, java.lang.invoke.MethodHandle methodHandle);
}
