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

public abstract class Permission {
  
  protected String name;
  
  public Permission(String name) {
    this.name = name;
  }
  
  public String getName() {
    return name;
  }
  
  public boolean implies(Permission permission) {
    return equals(permission);
  }

  public String getActions() {
    return "";
  }

  public void checkGuard(Object object) throws SecurityException { }

  @Override
  public String toString() {
    String actions = getActions();
    if (actions == null || actions.isEmpty()) {
      return "(\"" + getClass().getName() + "\" \"" + name + "\")";
    }
    return "(\"" + getClass().getName() + "\" \"" + name + "\" \"" + actions + "\")";
  }
  
  public PermissionCollection newPermissionCollection() {
    return null;
  }
}
