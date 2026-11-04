/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.dynalink.support;

import java.lang.invoke.MethodHandle;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicReference;
import jdk.dynalink.CallSiteDescriptor;
import jdk.dynalink.linker.GuardedInvocation;

public class ChainedCallSite extends AbstractRelinkableCallSite {
  private final AtomicReference<LinkedList<GuardedInvocation>> invocations = new AtomicReference<>();

  public ChainedCallSite(CallSiteDescriptor descriptor) {
    super(descriptor);
  }

  protected int getMaxChainLength() {
    return 8;
  }

  @Override
  public void relink(GuardedInvocation guardedInvocation, MethodHandle relinkAndInvoke) {
    relinkInternal(guardedInvocation, relinkAndInvoke, false);
  }

  @Override
  public void resetAndRelink(GuardedInvocation guardedInvocation, MethodHandle relinkAndInvoke) {
    relinkInternal(guardedInvocation, relinkAndInvoke, true);
  }

  private MethodHandle relinkInternal(GuardedInvocation invocation, MethodHandle relink, boolean reset) {
    final LinkedList<GuardedInvocation> currentInvocations = invocations.get();
    @SuppressWarnings("unchecked")
    final LinkedList<GuardedInvocation> newInvocations = currentInvocations == null || reset
        ? new LinkedList<GuardedInvocation>() : (LinkedList<GuardedInvocation>) currentInvocations.clone();
    newInvocations.removeIf(GuardedInvocation::hasBeenInvalidated);
    if (newInvocations.size() >= getMaxChainLength()) {
      newInvocations.removeFirst();
    }
    newInvocations.addLast(invocation);
    MethodHandle target = relink;
    for (GuardedInvocation inv : newInvocations) {
      target = inv.compose(target);
    }
    invocations.set(newInvocations);
    setTarget(target);
    return target;
  }
}
