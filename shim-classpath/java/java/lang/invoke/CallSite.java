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

public abstract class CallSite {
  private final MethodHandle target;

  protected CallSite(MethodHandle target) {
    if (target == null) {
      throw new NullPointerException();
    }
    this.target = target;
  }

  protected final MethodHandle target() {
    return target;
  }

  public abstract MethodHandle getTarget();

  public abstract void setTarget(MethodHandle target);

  public abstract MethodHandle dynamicInvoker();
}
