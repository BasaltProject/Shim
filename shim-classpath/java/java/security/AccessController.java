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

package java.security;

public final class AccessController {
  private AccessController() {
  }

  public static <T> T doPrivileged(PrivilegedAction<T> action) {
    return action.run();
  }

  public static <T> T doPrivilegedWithCombiner(PrivilegedAction<T> action) {
    return action.run();
  }

  public static <T> T doPrivileged(PrivilegedAction<T> action, AccessControlContext context) {
    return action.run();
  }

  public static <T> T doPrivileged(PrivilegedAction<T> action, AccessControlContext context,
      Permission... perms) {
    return action.run();
  }

  public static <T> T doPrivilegedWithCombiner(PrivilegedAction<T> action, AccessControlContext context,
      Permission... perms) {
    return action.run();
  }

  public static <T> T doPrivileged(PrivilegedExceptionAction<T> action) throws PrivilegedActionException {
    return run(action);
  }

  public static <T> T doPrivilegedWithCombiner(PrivilegedExceptionAction<T> action)
      throws PrivilegedActionException {
    return run(action);
  }

  public static <T> T doPrivileged(PrivilegedExceptionAction<T> action, AccessControlContext context)
      throws PrivilegedActionException {
    return run(action);
  }

  public static <T> T doPrivileged(PrivilegedExceptionAction<T> action, AccessControlContext context,
      Permission... perms) throws PrivilegedActionException {
    return run(action);
  }

  public static <T> T doPrivilegedWithCombiner(PrivilegedExceptionAction<T> action, AccessControlContext context,
      Permission... perms) throws PrivilegedActionException {
    return run(action);
  }

  private static <T> T run(PrivilegedExceptionAction<T> action) throws PrivilegedActionException {
    try {
      return action.run();
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new PrivilegedActionException(e);
    }
  }

  public static AccessControlContext getContext() {
    return new AccessControlContext(new ProtectionDomain[0]);
  }

  public static void checkPermission(Permission perm) throws AccessControlException {
    if (perm == null) {
      throw new NullPointerException("permission can't be null");
    }
    throw new AccessControlException("checking permissions is not supported");
  }
}
