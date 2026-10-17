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

import java.util.Collections;
import java.util.Enumeration;

public final class AllPermission extends Permission {
  private static final long serialVersionUID = -2916474571451318075L;

  public AllPermission() {
    super("<all permissions>");
  }

  public AllPermission(String name, String actions) {
    this();
  }

  @Override
  public boolean implies(Permission p) {
    return true;
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof AllPermission;
  }

  @Override
  public int hashCode() {
    return 1;
  }

  @Override
  public String getActions() {
    return "<all actions>";
  }

  @Override
  public PermissionCollection newPermissionCollection() {
    return new AllPermissionCollection();
  }
}

final class AllPermissionCollection extends PermissionCollection implements java.io.Serializable {
  private static final long serialVersionUID = -4023431423420167738L;

  private boolean allAllowed;

  @Override
  public void add(Permission permission) {
    if (!(permission instanceof AllPermission)) {
      throw new IllegalArgumentException("invalid permission: " + permission);
    }
    if (isReadOnly()) {
      throw new SecurityException("attempt to add a Permission to a readonly PermissionCollection");
    }
    allAllowed = true;
  }

  @Override
  public boolean implies(Permission permission) {
    return allAllowed;
  }

  @Override
  public Enumeration<Permission> elements() {
    return allAllowed ? Collections.<Permission>enumeration(Collections.<Permission>singletonList(new AllPermission()))
        : Collections.<Permission>emptyEnumeration();
  }
}
