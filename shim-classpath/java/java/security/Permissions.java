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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Permissions extends PermissionCollection implements java.io.Serializable {
  private static final long serialVersionUID = 4858622370623524688L;

  private final Map<Class<?>, PermissionCollection> permsMap = new HashMap<Class<?>, PermissionCollection>(11);
  private PermissionCollection allPermission;

  public Permissions() {
  }

  @Override
  public void add(Permission permission) {
    if (isReadOnly()) {
      throw new SecurityException("attempt to add a Permission to a readonly Permissions object");
    }
    synchronized (this) {
      PermissionCollection pc = permsMap.get(permission.getClass());
      if (pc == null) {
        pc = permission.newPermissionCollection();
        if (pc == null) {
          pc = new PermissionsHash();
        }
        permsMap.put(permission.getClass(), pc);
      }
      pc.add(permission);
      if (permission instanceof AllPermission) {
        allPermission = pc;
      }
    }
  }

  @Override
  public boolean implies(Permission permission) {
    if (allPermission != null) {
      return true;
    }
    PermissionCollection pc;
    synchronized (this) {
      pc = permsMap.get(permission.getClass());
    }
    return pc != null && pc.implies(permission);
  }

  @Override
  public Enumeration<Permission> elements() {
    List<Permission> all = new ArrayList<Permission>();
    synchronized (this) {
      for (PermissionCollection pc : permsMap.values()) {
        all.addAll(Collections.list(pc.elements()));
      }
    }
    return Collections.enumeration(all);
  }
}

final class PermissionsHash extends PermissionCollection implements java.io.Serializable {
  private static final long serialVersionUID = -8491988220802933440L;

  private final Map<Permission, Permission> perms = new HashMap<Permission, Permission>(11);

  @Override
  public void add(Permission permission) {
    synchronized (this) {
      perms.put(permission, permission);
    }
  }

  @Override
  public boolean implies(Permission permission) {
    synchronized (this) {
      if (perms.get(permission) != null) {
        return true;
      }
      for (Permission p : perms.values()) {
        if (p.implies(permission)) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public Enumeration<Permission> elements() {
    synchronized (this) {
      return Collections.enumeration(new ArrayList<Permission>(perms.values()));
    }
  }
}
