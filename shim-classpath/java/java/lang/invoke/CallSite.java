/*
 * Based on Avian (https://github.com/ReadyTalk/avian).
 * Original work Copyright (c) 2008-2016, Avian Contributors.
 * Original work licensed under ISC; see LICENSE.txt:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE.txt
 *
 * Modifications Copyright (c) 2026 BasaltMC.
 * Modifications licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: ISC AND MIT
 */

/* Copyright (c) 2008-2016, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.lang.invoke;

import java.util.Objects;

public abstract class CallSite {
  final MethodType type;
  volatile MethodHandle target;

  CallSite(MethodType type) {
    this.type = Objects.requireNonNull(type, "type");
    this.target = null;
  }

  CallSite(MethodHandle target) {
    this.target = Objects.requireNonNull(target, "target");
    this.type = target.type();
  }

  protected final MethodHandle target() {
    return target;
  }

  public MethodType type() {
    return type != null ? type : (target != null ? target.type() : null);
  }

  public abstract MethodHandle getTarget();

  public abstract void setTarget(MethodHandle target);

  public abstract MethodHandle dynamicInvoker();

  void checkTargetChange(MethodHandle oldTarget, MethodHandle newTarget) {
    Objects.requireNonNull(newTarget, "newTarget");
    MethodType expectedType = type();
    if (!newTarget.type().equals(expectedType)) {
      throw new WrongMethodTypeException("target and call site types do not match: "
          + newTarget.type() + " vs " + expectedType);
    }
  }

  MethodHandle makeDynamicInvoker() {
    return new MethodHandle.DynamicInvokerMethodHandle(this);
  }
}
