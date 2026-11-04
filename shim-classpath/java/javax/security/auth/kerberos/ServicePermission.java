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
import java.io.ObjectOutputStream;
import java.security.Permission;
import java.security.PermissionCollection;
import java.util.Enumeration;
import java.util.Vector;

public final class ServicePermission extends Permission implements java.io.Serializable {
  private static final long serialVersionUID = -1227585031618624935L;

  private static final int INITIATE = 0x1;
  private static final int ACCEPT = 0x2;
  private static final int ALL = INITIATE | ACCEPT;
  private static final int NONE = 0x0;

  private transient int mask;
  private String actions;

  public ServicePermission(String servicePrincipal, String action) {
    super(servicePrincipal);
    if (servicePrincipal == null) {
      throw new NullPointerException("service class cannot be null");
    }
    init(servicePrincipal, getMask(action));
  }

  private void init(String servicePrincipal, int mask) {
    if ((mask & ALL) != mask) {
      throw new IllegalArgumentException("invalid actions mask");
    }
    this.mask = mask;
  }

  public boolean implies(Permission p) {
    if (!(p instanceof ServicePermission)) {
      return false;
    }
    ServicePermission that = (ServicePermission) p;
    return ((this.mask & that.mask) == that.mask) && impliesIgnoreMask(that);
  }

  boolean impliesIgnoreMask(ServicePermission p) {
    return (this.getName().equals("*") || this.getName().equals(p.getName())
        || (p.getName().startsWith(this.getName().replace("*", "")) && this.getName().endsWith("*") && !this.getName().equals("*")));
  }

  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof ServicePermission)) {
      return false;
    }
    ServicePermission that = (ServicePermission) obj;
    return ((this.mask & that.mask) == that.mask) && this.getName().equals(that.getName()) && this.mask == that.mask;
  }

  public int hashCode() {
    return (getName().hashCode() ^ mask);
  }

  private static String getActions(int mask) {
    StringBuilder sb = new StringBuilder();
    boolean comma = false;
    if ((mask & INITIATE) == INITIATE) {
      comma = true;
      sb.append("initiate");
    }
    if ((mask & ACCEPT) == ACCEPT) {
      if (comma) {
        sb.append(',');
      } else {
        comma = true;
      }
      sb.append("accept");
    }
    return sb.toString();
  }

  public String getActions() {
    if (actions == null) {
      actions = getActions(this.mask);
    }
    return actions;
  }

  public PermissionCollection newPermissionCollection() {
    return new KrbServicePermissionCollection();
  }

  int getMask() {
    return mask;
  }

  private static int getMask(String action) {
    if (action == null) {
      throw new NullPointerException("action cannot be null");
    }
    if (action.equals("")) {
      throw new IllegalArgumentException("action cannot be empty");
    }
    int mask = NONE;
    for (String a : action.split(",")) {
      String t = a.trim();
      if (t.equalsIgnoreCase("initiate")) {
        mask |= INITIATE;
      } else if (t.equalsIgnoreCase("accept")) {
        mask |= ACCEPT;
      } else {
        throw new IllegalArgumentException("invalid permission: " + action);
      }
    }
    return mask;
  }

  private void writeObject(ObjectOutputStream s) throws IOException {
    if (actions == null) {
      getActions();
    }
    s.defaultWriteObject();
  }

  private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
    s.defaultReadObject();
    init(getName(), getMask(actions));
  }
}

final class KrbServicePermissionCollection extends PermissionCollection implements java.io.Serializable {
  private static final long serialVersionUID = -4118834211490102011L;

  private Vector<Permission> perms = new Vector<Permission>();

  public void add(Permission permission) {
    if (!(permission instanceof ServicePermission)) {
      throw new IllegalArgumentException("invalid permission: " + permission);
    }
    if (isReadOnly()) {
      throw new SecurityException("attempt to add a Permission to a readonly PermissionCollection");
    }
    synchronized (perms) {
      perms.add(permission);
    }
  }

  public boolean implies(Permission permission) {
    if (!(permission instanceof ServicePermission)) {
      return false;
    }
    ServicePermission np = (ServicePermission) permission;
    int desired = np.getMask();
    int effective = 0;
    synchronized (perms) {
      for (Permission p : perms) {
        ServicePermission sp = (ServicePermission) p;
        if ((sp.getMask() & desired) != 0 && sp.impliesIgnoreMask(np)) {
          effective |= sp.getMask();
          if ((effective & desired) == desired) {
            return true;
          }
        }
      }
    }
    return false;
  }

  public Enumeration<Permission> elements() {
    return perms.elements();
  }
}
