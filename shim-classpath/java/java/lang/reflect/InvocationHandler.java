/*
 * Based on Avian (https://github.com/ReadyTalk/avian).
 * Original work Copyright (c) 2008-2015, Avian Contributors.
 * Original work licensed under ISC; see LICENSE.txt:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE.txt
 *
 * Modifications Copyright (c) 2026 BasaltMC.
 * Modifications licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: ISC AND MIT
 */

/* Copyright (c) 2008-2015, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.lang.reflect;

import java.util.Objects;

public interface InvocationHandler {
  public Object invoke(Object proxy, Method method, Object[] arguments) throws Throwable;

  public static Object invokeDefault(Object proxy, Method method, Object... args) throws Throwable {
    Objects.requireNonNull(proxy, "proxy");
    Objects.requireNonNull(method, "method");
    if (!Proxy.isProxyClass(proxy.getClass())) {
      throw new IllegalArgumentException("not a proxy instance");
    }
    if (!method.isDefault()) {
      throw new IllegalArgumentException("not a default method");
    }
    if (!method.getDeclaringClass().isAssignableFrom(proxy.getClass())) {
      throw new IllegalArgumentException("default method is not member of proxy interface: " + method);
    }
    try {
      return method.invokeNonVirtual(proxy, args != null ? args : new Object[0]);
    } catch (InvocationTargetException e) {
      Throwable target = e.getTargetException();
      if (target != null) {
        throw target;
      }
      throw e;
    }
  }
}
