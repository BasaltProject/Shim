/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.dynalink.support;

import java.lang.invoke.MethodHandle;
import jdk.dynalink.CallSiteDescriptor;
import jdk.dynalink.linker.GuardedInvocation;

public class SimpleRelinkableCallSite extends AbstractRelinkableCallSite {
  public SimpleRelinkableCallSite(CallSiteDescriptor descriptor) {
    super(descriptor);
  }

  @Override
  public void relink(GuardedInvocation guardedInvocation, MethodHandle relinkAndInvoke) {
    setTarget(guardedInvocation.compose(relinkAndInvoke));
  }

  @Override
  public void resetAndRelink(GuardedInvocation guardedInvocation, MethodHandle relinkAndInvoke) {
    relink(guardedInvocation, relinkAndInvoke);
  }
}
