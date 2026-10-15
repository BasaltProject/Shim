/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.lang.invoke;

import java.util.Objects;

public class ConstantCallSite extends CallSite {
  private final boolean isFrozen;

  public ConstantCallSite(MethodHandle target) {
    super(target);
    this.isFrozen = true;
  }

  protected ConstantCallSite(MethodType targetType, MethodHandle createTargetHook) throws Throwable {
    super(targetType);
    Objects.requireNonNull(createTargetHook, "createTargetHook");
    this.isFrozen = false;
    MethodHandle boundTarget = (MethodHandle) createTargetHook.invokeWithArguments(this);
    checkTargetChange(null, boundTarget);
    this.target = boundTarget;
  }

  @Override
  public final MethodHandle getTarget() {
    if (target == null) {
      throw new IllegalStateException("call site is not yet bound");
    }
    return target;
  }

  @Override
  public final void setTarget(MethodHandle target) {
    throw new UnsupportedOperationException("ConstantCallSite target is permanent");
  }

  @Override
  public final MethodHandle dynamicInvoker() {
    return getTarget();
  }
}
