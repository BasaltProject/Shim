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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public abstract class BasicPermission extends Permission implements java.io.Serializable {
  private static final long serialVersionUID = 6279438298436773498L;

  private transient boolean wildcard;
  private transient String path;
  private transient boolean exitVM;

  public BasicPermission(String name) {
    super(name);
    init(name);
  }

  public BasicPermission(String name, String actions) {
    super(name);
    init(name);
  }

  private void init(String name) {
    Objects.requireNonNull(name, "name can't be null");
    int len = name.length();
    if (len == 0) {
      throw new IllegalArgumentException("name can't be empty");
    }
    char last = name.charAt(len - 1);
    if (last == '*' && (len == 1 || name.charAt(len - 2) == '.')) {
      wildcard = true;
      path = len == 1 ? "" : name.substring(0, len - 1);
    } else if (name.equals("exitVM")) {
      wildcard = true;
      path = "exitVM.";
      exitVM = true;
    } else {
      path = name;
    }
  }

  @Override
  public boolean implies(Permission p) {
    if (p == null || p.getClass() != getClass()) {
      return false;
    }
    BasicPermission that = (BasicPermission) p;
    if (wildcard) {
      if (that.wildcard) {
        return that.path.startsWith(path);
      }
      return that.path.length() > path.length() && that.path.startsWith(path);
    }
    return !that.wildcard && path.equals(that.path);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj == null || obj.getClass() != getClass()) {
      return false;
    }
    return getName().equals(((BasicPermission) obj).getName());
  }

  @Override
  public int hashCode() {
    return getName().hashCode();
  }

  @Override
  public String getActions() {
    return "";
  }

  @Override
  public PermissionCollection newPermissionCollection() {
    return new BasicPermissionCollection(getClass());
  }

  boolean isExitVM() {
    return exitVM;
  }
}

final class BasicPermissionCollection extends PermissionCollection implements java.io.Serializable {
  private static final long serialVersionUID = 739301742472979399L;

  private final Map<String, Permission> perms = new HashMap<String, Permission>(11);
  private boolean allAllowed;
  private final Class<?> permClass;

  BasicPermissionCollection(Class<?> clazz) {
    permClass = clazz;
  }

  @Override
  public void add(Permission permission) {
    if (!(permission instanceof BasicPermission)) {
      throw new IllegalArgumentException("invalid permission: " + permission);
    }
    if (isReadOnly()) {
      throw new SecurityException("attempt to add a Permission to a readonly PermissionCollection");
    }
    BasicPermission bp = (BasicPermission) permission;
    if (permClass == null) {
    } else if (bp.getClass() != permClass) {
      throw new IllegalArgumentException("invalid permission: " + permission);
    }
    synchronized (this) {
      perms.put(bp.getName(), permission);
    }
    if (!allAllowed && bp.getName().equals("*")) {
      allAllowed = true;
    }
  }

  @Override
  public boolean implies(Permission permission) {
    if (!(permission instanceof BasicPermission)) {
      return false;
    }
    BasicPermission bp = (BasicPermission) permission;
    if (bp.getClass() != permClass) {
      return false;
    }
    if (allAllowed) {
      return true;
    }
    String name = bp.getName();
    Permission x;
    synchronized (this) {
      x = perms.get(name);
    }
    if (x != null) {
      return x.implies(permission);
    }
    int last;
    int offset = name.length() - 1;
    while ((last = name.lastIndexOf('.', offset)) != -1) {
      name = name.substring(0, last + 1) + "*";
      synchronized (this) {
        x = perms.get(name);
      }
      if (x != null) {
        return x.implies(permission);
      }
      offset = last - 1;
    }
    return false;
  }

  @Override
  public Enumeration<Permission> elements() {
    synchronized (this) {
      return Collections.enumeration(new java.util.ArrayList<Permission>(perms.values()));
    }
  }
}
