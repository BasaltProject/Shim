/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.invoke;

import java.util.Objects;

public class MutableCallSite extends CallSite {
  public MutableCallSite(MethodType type) {
    super(type);
    this.target = MethodHandles.empty(type);
  }

  public MutableCallSite(MethodHandle target) {
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

  public static void syncAll(MutableCallSite[] sites) {
    Objects.requireNonNull(sites, "sites");
    for (MutableCallSite site : sites) {
      Objects.requireNonNull(site, "site");
    }
  }
}
