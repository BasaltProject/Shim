/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.dynalink.support;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MutableCallSite;
import jdk.dynalink.CallSiteDescriptor;
import jdk.dynalink.RelinkableCallSite;

public abstract class AbstractRelinkableCallSite extends MutableCallSite implements RelinkableCallSite {
  private final CallSiteDescriptor descriptor;

  protected AbstractRelinkableCallSite(CallSiteDescriptor descriptor) {
    super(descriptor.getMethodType());
    this.descriptor = descriptor;
  }

  @Override
  public CallSiteDescriptor getDescriptor() {
    return descriptor;
  }

  @Override
  public void initialize(MethodHandle relinkAndInvoke) {
    setTarget(relinkAndInvoke);
  }
}
