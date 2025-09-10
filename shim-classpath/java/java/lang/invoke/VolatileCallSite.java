/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.invoke;

public class VolatileCallSite extends CallSite {
  public VolatileCallSite(MethodType type) {
    super(type);
    this.target = MethodHandles.empty(type);
  }

  public VolatileCallSite(MethodHandle target) {
    super(target);
  }

  @Override
  public final MethodHandle getTarget() {
    return target;
  }

  @Override
  public void setTarget(MethodHandle newTarget) {
    checkTargetChange(this.target, newTarget);
    this.target = newTarget;
  }

  @Override
  public final MethodHandle dynamicInvoker() {
    return makeDynamicInvoker();
  }
}
