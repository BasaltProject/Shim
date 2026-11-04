/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.dynalink.linker;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.SwitchPoint;
import java.util.List;
import java.util.Objects;
import jdk.dynalink.CallSiteDescriptor;
import jdk.dynalink.linker.support.Guards;

public class GuardedInvocation {
  private final MethodHandle invocation;
  private final MethodHandle guard;
  private final Class<? extends Throwable> exception;
  private final SwitchPoint[] switchPoints;

  public GuardedInvocation(MethodHandle invocation) {
    this(invocation, null, (SwitchPoint[]) null, null);
  }

  public GuardedInvocation(MethodHandle invocation, MethodHandle guard) {
    this(invocation, guard, (SwitchPoint[]) null, null);
  }

  public GuardedInvocation(MethodHandle invocation, SwitchPoint switchPoint) {
    this(invocation, null, switchPoint, null);
  }

  public GuardedInvocation(MethodHandle invocation, MethodHandle guard, SwitchPoint switchPoint) {
    this(invocation, guard, switchPoint, null);
  }

  public GuardedInvocation(MethodHandle invocation, MethodHandle guard, SwitchPoint switchPoint,
      Class<? extends Throwable> exception) {
    this.invocation = Objects.requireNonNull(invocation);
    this.guard = guard;
    this.switchPoints = switchPoint == null ? null : new SwitchPoint[] {switchPoint};
    this.exception = exception;
  }

  public GuardedInvocation(MethodHandle invocation, MethodHandle guard, SwitchPoint[] switchPoints,
      Class<? extends Throwable> exception) {
    this.invocation = Objects.requireNonNull(invocation);
    this.guard = guard;
    this.switchPoints = switchPoints == null ? null : switchPoints.clone();
    this.exception = exception;
  }

  public MethodHandle getInvocation() {
    return invocation;
  }

  public MethodHandle getGuard() {
    return guard;
  }

  public SwitchPoint[] getSwitchPoints() {
    return switchPoints == null ? null : switchPoints.clone();
  }

  public Class<? extends Throwable> getException() {
    return exception;
  }

  public boolean hasBeenInvalidated() {
    if (switchPoints == null) {
      return false;
    }
    for (SwitchPoint sp : switchPoints) {
      if (sp.hasBeenInvalidated()) {
        return true;
      }
    }
    return false;
  }

  public GuardedInvocation replaceMethods(MethodHandle newInvocation, MethodHandle newGuard) {
    return new GuardedInvocation(newInvocation, newGuard, switchPoints, exception);
  }

  public GuardedInvocation addSwitchPoint(SwitchPoint newSwitchPoint) {
    if (newSwitchPoint == null) {
      return this;
    }
    final SwitchPoint[] newSwitchPoints;
    if (switchPoints != null) {
      newSwitchPoints = new SwitchPoint[switchPoints.length + 1];
      System.arraycopy(switchPoints, 0, newSwitchPoints, 0, switchPoints.length);
      newSwitchPoints[switchPoints.length] = newSwitchPoint;
    } else {
      newSwitchPoints = new SwitchPoint[] {newSwitchPoint};
    }
    return new GuardedInvocation(invocation, guard, newSwitchPoints, exception);
  }

  public GuardedInvocation asType(MethodType newType) {
    return replaceMethods(invocation.asType(newType), guard == null ? null : Guards.asType(guard, newType));
  }

  public GuardedInvocation asType(LinkerServices linkerServices, MethodType newType) {
    return replaceMethods(linkerServices.asType(invocation, newType),
        guard == null ? null : Guards.asType(linkerServices, guard, newType));
  }

  public GuardedInvocation asTypeSafeReturn(LinkerServices linkerServices, MethodType newType) {
    return replaceMethods(linkerServices.asTypeLosslessReturn(invocation, newType),
        guard == null ? null : Guards.asType(linkerServices, guard, newType));
  }

  public GuardedInvocation asType(CallSiteDescriptor desc) {
    return asType(desc.getMethodType());
  }

  public GuardedInvocation filterArguments(int pos, MethodHandle... filters) {
    return replaceMethods(MethodHandles.filterArguments(invocation, pos, filters),
        guard == null ? null : MethodHandles.filterArguments(guard, pos, filters));
  }

  public GuardedInvocation dropArguments(int pos, List<Class<?>> valueTypes) {
    return replaceMethods(MethodHandles.dropArguments(invocation, pos, valueTypes),
        guard == null ? null : MethodHandles.dropArguments(guard, pos, valueTypes));
  }

  public GuardedInvocation dropArguments(int pos, Class<?>... valueTypes) {
    return replaceMethods(MethodHandles.dropArguments(invocation, pos, valueTypes),
        guard == null ? null : MethodHandles.dropArguments(guard, pos, valueTypes));
  }

  public MethodHandle compose(MethodHandle fallback) {
    return compose(fallback, fallback, fallback);
  }

  public MethodHandle compose(MethodHandle guardFallback, MethodHandle switchpointFallback,
      MethodHandle catchFallback) {
    final MethodHandle guarded = guard == null ? invocation
        : MethodHandles.guardWithTest(guard, invocation, guardFallback);
    final MethodHandle spGuarded = switchPoints == null ? guarded : applySwitchPoints(guarded, switchpointFallback);
    return exception == null ? spGuarded : MethodHandles.catchException(spGuarded, exception,
        MethodHandles.dropArguments(catchFallback, 0, exception));
  }

  private MethodHandle applySwitchPoints(MethodHandle mh, MethodHandle fallback) {
    assert switchPoints != null;
    MethodHandle spGuarded = mh;
    for (final SwitchPoint sp : switchPoints) {
      spGuarded = sp.guardWithTest(spGuarded, fallback);
    }
    return spGuarded;
  }

  @Override
  public String toString() {
    return "invocation=" + invocation + ", guard=" + guard;
  }
}
