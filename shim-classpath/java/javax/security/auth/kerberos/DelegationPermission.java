/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.security.auth.kerberos;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.security.BasicPermission;
import java.security.Permission;
import java.security.PermissionCollection;
import java.util.Enumeration;
import java.util.Vector;

public final class DelegationPermission extends BasicPermission implements java.io.Serializable {
  private static final long serialVersionUID = 883133252142523922L;

  private transient String subordinate;
  private transient String service;

  public DelegationPermission(String principals) {
    super(principals);
    init(principals);
  }

  public DelegationPermission(String principals, String actions) {
    super(principals, actions);
    init(principals);
  }

  private void init(String target) {
    StringTokenizerHelper st = new StringTokenizerHelper(target);
    if (!st.hasMore()) {
      throw new IllegalArgumentException("service principal [" + target + "] syntax invalid: improperly quoted");
    }
    subordinate = st.next();
    if (!st.hasMore()) {
      throw new IllegalArgumentException("service principal [" + target + "] syntax invalid: improperly quoted");
    }
    service = st.next();
  }

  private static final class StringTokenizerHelper {
    private final String s;
    private int pos = 0;

    StringTokenizerHelper(String s) {
      this.s = s;
    }

    boolean hasMore() {
      while (pos < s.length() && Character.isWhitespace(s.charAt(pos))) {
        pos++;
      }
      return pos < s.length();
    }

    String next() {
      if (s.charAt(pos) != '"') {
        throw new IllegalArgumentException("service principal [" + s + "] syntax invalid: improperly quoted");
      }
      int end = s.indexOf('"', pos + 1);
      if (end < 0) {
        throw new IllegalArgumentException("service principal [" + s + "] syntax invalid: improperly quoted");
      }
      String t = s.substring(pos + 1, end);
      pos = end + 1;
      return t;
    }
  }

  public boolean implies(Permission p) {
    if (!(p instanceof DelegationPermission)) {
      return false;
    }
    DelegationPermission that = (DelegationPermission) p;
    return this.subordinate.equals(that.subordinate) && this.service.equals(that.service);
  }

  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof DelegationPermission)) {
      return false;
    }
    DelegationPermission that = (DelegationPermission) obj;
    return implies(that);
  }

  public int hashCode() {
    return getName().hashCode();
  }

  public PermissionCollection newPermissionCollection() {
    return new KrbDelegationPermissionCollection();
  }

  private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
    s.defaultReadObject();
    init(getName());
  }
}

final class KrbDelegationPermissionCollection extends PermissionCollection implements java.io.Serializable {
  private static final long serialVersionUID = -3383936936589966948L;

  private Vector<Permission> perms = new Vector<Permission>();

  public void add(Permission permission) {
    if (!(permission instanceof DelegationPermission)) {
      throw new IllegalArgumentException("invalid permission: " + permission);
    }
    if (isReadOnly()) {
      throw new SecurityException("attempt to add a Permission to a readonly PermissionCollection");
    }
    perms.add(permission);
  }

  public boolean implies(Permission permission) {
    if (!(permission instanceof DelegationPermission)) {
      return false;
    }
    for (Permission p : perms) {
      if (p.implies(permission)) {
        return true;
      }
    }
    return false;
  }

  public Enumeration<Permission> elements() {
    return perms.elements();
  }
}
