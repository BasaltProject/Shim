/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security;

import java.security.cert.Certificate;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Objects;

public final class UnresolvedPermission extends Permission implements java.io.Serializable {
  private static final long serialVersionUID = -4821973115467008846L;

  private final String type;
  private final String name;
  private final String actions;
  private final Certificate[] certs;

  public UnresolvedPermission(String type, String name, String actions, Certificate[] certs) {
    super(type);
    if (type == null) {
      throw new NullPointerException("type can't be null");
    }
    this.type = type;
    this.name = name;
    this.actions = actions;
    this.certs = certs == null ? null : certs.clone();
  }

  @Override
  public boolean implies(Permission p) {
    return false;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof UnresolvedPermission)) {
      return false;
    }
    UnresolvedPermission that = (UnresolvedPermission) obj;
    return type.equals(that.type) && Objects.equals(name, that.name) && Objects.equals(actions, that.actions)
        && java.util.Arrays.equals(certs, that.certs);
  }

  @Override
  public int hashCode() {
    int hash = type.hashCode();
    if (name != null) {
      hash ^= name.hashCode();
    }
    if (actions != null) {
      hash ^= actions.hashCode();
    }
    return hash;
  }

  @Override
  public String getActions() {
    return "";
  }

  public String getUnresolvedType() {
    return type;
  }

  public String getUnresolvedName() {
    return name;
  }

  public String getUnresolvedActions() {
    return actions;
  }

  public Certificate[] getUnresolvedCerts() {
    return certs == null ? null : certs.clone();
  }

  @Override
  public String toString() {
    return "(unresolved " + type + " " + name + " " + actions + ")";
  }

  @Override
  public PermissionCollection newPermissionCollection() {
    return new UnresolvedPermissionCollection();
  }
}

final class UnresolvedPermissionCollection extends PermissionCollection implements java.io.Serializable {
  private static final long serialVersionUID = -7176153071733132400L;

  private final java.util.List<Permission> perms = new java.util.ArrayList<Permission>();

  @Override
  public void add(Permission permission) {
    if (!(permission instanceof UnresolvedPermission)) {
      throw new IllegalArgumentException("invalid permission: " + permission);
    }
    if (isReadOnly()) {
      throw new SecurityException("attempt to add a Permission to a readonly PermissionCollection");
    }
    perms.add(permission);
  }

  @Override
  public boolean implies(Permission permission) {
    return false;
  }

  @Override
  public Enumeration<Permission> elements() {
    return Collections.enumeration(new java.util.ArrayList<Permission>(perms));
  }
}
