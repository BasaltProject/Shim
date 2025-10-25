/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.util;

import java.io.Serializable;
import java.security.BasicPermission;
import java.security.Permission;
import java.security.PermissionCollection;

public final class PropertyPermission extends BasicPermission {
  private static final long serialVersionUID = 885438825399942851L;

  private static final int kRead = 0x1;
  private static final int kWrite = 0x2;

  private transient int mask;
  private String actions;

  public PropertyPermission(String name, String actions) {
    super(name, actions);
    init(getMask(actions));
  }

  private void init(int mask) {
    if ((mask & (kRead | kWrite)) != mask) {
      throw new IllegalArgumentException("invalid actions mask");
    }
    if (mask == 0) {
      throw new IllegalArgumentException("invalid actions mask");
    }
    this.mask = mask;
  }

  private static int getMask(String actions) {
    int mask = 0;
    if (actions == null || actions.trim().isEmpty()) {
      return mask;
    }
    for (String token : actions.split(",", -1)) {
      String a = token.trim().toLowerCase(Locale.ROOT);
      if (a.equals("read")) {
        mask |= kRead;
      } else if (a.equals("write")) {
        mask |= kWrite;
      } else {
        throw new IllegalArgumentException("invalid permission: " + actions);
      }
    }
    return mask;
  }

  @Override
  public boolean implies(Permission p) {
    if (!(p instanceof PropertyPermission)) {
      return false;
    }
    PropertyPermission that = (PropertyPermission) p;
    return (mask & that.mask) == that.mask && super.implies(that);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof PropertyPermission)) {
      return false;
    }
    PropertyPermission that = (PropertyPermission) obj;
    return mask == that.mask && getName().equals(that.getName());
  }

  @Override
  public int hashCode() {
    return getName().hashCode();
  }

  int getMask() {
    return mask;
  }

  @Override
  public String getActions() {
    if (actions == null) {
      actions = mask == (kRead | kWrite) ? "read,write" : mask == kRead ? "read" : "write";
    }
    return actions;
  }

  @Override
  public PermissionCollection newPermissionCollection() {
    return new PropertyPermissionCollection();
  }
}

final class PropertyPermissionCollection extends PermissionCollection implements Serializable {
  private static final long serialVersionUID = 7015263904581634791L;

  private final Map<String, PropertyPermission> perms = new HashMap<String, PropertyPermission>(32);
  private boolean allAllowed;

  @Override
  public void add(Permission permission) {
    if (!(permission instanceof PropertyPermission)) {
      throw new IllegalArgumentException("invalid permission: " + permission);
    }
    if (isReadOnly()) {
      throw new SecurityException("attempt to add a Permission to a readonly PermissionCollection");
    }
    PropertyPermission pp = (PropertyPermission) permission;
    String propName = pp.getName();
    synchronized (this) {
      PropertyPermission existing = perms.get(propName);
      if (existing != null) {
        int oldMask = existing.getMask();
        int newMask = pp.getMask();
        if (oldMask != newMask) {
          int effective = oldMask | newMask;
          String actions = effective == 3 ? "read,write" : effective == 1 ? "read" : "write";
          perms.put(propName, new PropertyPermission(propName, actions));
        }
      } else {
        perms.put(propName, pp);
      }
    }
    if (!allAllowed && perms.containsKey("*")) {
      allAllowed = true;
    }
  }

  @Override
  public boolean implies(Permission permission) {
    if (!(permission instanceof PropertyPermission)) {
      return false;
    }
    PropertyPermission pp = (PropertyPermission) permission;
    int desired = pp.getMask();
    int effective = 0;
    PropertyPermission x;
    synchronized (this) {
      if (allAllowed) {
        x = perms.get("*");
        if (x != null) {
          effective |= x.getMask();
          if ((effective & desired) == desired) {
            return true;
          }
        }
      }
      String name = pp.getName();
      x = perms.get(name);
      if (x != null) {
        effective |= x.getMask();
        if ((effective & desired) == desired) {
          return true;
        }
      }
      int last;
      int offset = name.length() - 1;
      while ((last = name.lastIndexOf('.', offset)) != -1) {
        name = name.substring(0, last + 1) + "*";
        x = perms.get(name);
        if (x != null) {
          effective |= x.getMask();
          if ((effective & desired) == desired) {
            return true;
          }
        }
        offset = last - 1;
      }
    }
    return false;
  }

  @Override
  public Enumeration<Permission> elements() {
    synchronized (this) {
      return Collections.<Permission>enumeration(new ArrayList<Permission>(perms.values()));
    }
  }
}
