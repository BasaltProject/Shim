/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management;

import java.security.BasicPermission;
import java.security.Permission;

public class MBeanServerPermission extends BasicPermission {
  private static final long serialVersionUID = -5661980843569388590L;

  public MBeanServerPermission(String name) {
    this(name, null);
  }

  public MBeanServerPermission(String name, String actions) {
    super(name, actions);
    if (name == null) {
      throw new NullPointerException("MBeanServerPermission: target name can't be null");
    }
    if (actions != null && actions.length() > 0) {
      throw new IllegalArgumentException("MBeanServerPermission actions must be null: " + actions);
    }
    String[] known = {"createMBeanServer", "findMBeanServer", "newMBeanServer", "releaseMBeanServer"};
    if (!name.equals("*")) {
      for (String part : name.split(",")) {
        boolean ok = false;
        for (String k : known) {
          ok |= k.equals(part.trim());
        }
        if (!ok) {
          throw new IllegalArgumentException("Invalid permission: " + part.trim());
        }
      }
    }
  }

  @Override
  public boolean implies(Permission p) {
    if (!(p instanceof MBeanServerPermission)) {
      return false;
    }
    if (getName().equals("*")) {
      return true;
    }
    for (String want : p.getName().split(",")) {
      boolean found = false;
      for (String have : getName().split(",")) {
        found |= have.trim().equals(want.trim());
      }
      if (!found && !p.getName().equals("*")) {
        return false;
      }
    }
    return !p.getName().equals("*") || getName().equals("*");
  }
}
