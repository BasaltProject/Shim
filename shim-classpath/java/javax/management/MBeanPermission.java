/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management;

import java.security.Permission;

public class MBeanPermission extends Permission {
  private static final long serialVersionUID = -2416928705275160661L;

  private final String actions;
  private final String className;
  private final String member;
  private final ObjectName objectName;

  public MBeanPermission(String name, String actions) {
    super(name);
    if (name == null) {
      throw new IllegalArgumentException("MBeanPermission name cannot be null");
    }
    if (name.trim().length() == 0) {
      throw new IllegalArgumentException("MBeanPermission name cannot be empty");
    }
    if (actions == null) {
      throw new IllegalArgumentException("MBeanPermission: actions can't be null");
    }
    this.actions = actions.trim();
    String cls = name;
    String mem = null;
    ObjectName on = null;
    int open = name.indexOf('[');
    if (open >= 0 && name.endsWith("]")) {
      try {
        on = new ObjectName(name.substring(open + 1, name.length() - 1));
      } catch (MalformedObjectNameException e) {
        throw new IllegalArgumentException("Invalid ObjectName: " + e.getMessage());
      }
      cls = name.substring(0, open);
    }
    int hash = cls.indexOf('#');
    if (hash >= 0) {
      mem = cls.substring(hash + 1);
      cls = cls.substring(0, hash);
    }
    this.className = cls.isEmpty() ? "*" : cls;
    this.member = mem;
    this.objectName = on;
  }

  public MBeanPermission(String className, String member, ObjectName objectName, String actions) {
    this(makeName(className, member, objectName), actions);
  }

  private static String makeName(String className, String member, ObjectName objectName) {
    StringBuilder b = new StringBuilder();
    b.append(className == null ? "-" : className);
    if (member == null) {
      b.append("#-");
    } else if (member.length() != 0) {
      b.append("#").append(member);
    }
    if (objectName == null) {
      b.append("[-]");
    } else {
      b.append("[").append(objectName.getCanonicalName()).append("]");
    }
    return b.toString();
  }

  @Override
  public String getActions() {
    return actions;
  }

  @Override
  public int hashCode() {
    return getName().hashCode() + actions.hashCode();
  }

  @Override
  public boolean implies(Permission p) {
    if (!(p instanceof MBeanPermission)) {
      return false;
    }
    MBeanPermission that = (MBeanPermission) p;
    if (!actions.equals("*")) {
      for (String a : that.actions.split(",")) {
        boolean found = false;
        for (String have : actions.split(",")) {
          found |= have.trim().equals(a.trim());
        }
        if (!found) {
          return false;
        }
      }
    }
    if (!className.equals("*") && !className.equals(that.className)) {
      return false;
    }
    if (member != null && !member.equals("*") && !member.equals(that.member)) {
      return false;
    }
    if (objectName != null && that.objectName != null && !objectName.apply(that.objectName)
        && !objectName.equals(that.objectName)) {
      return false;
    }
    return true;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof MBeanPermission)) {
      return false;
    }
    MBeanPermission that = (MBeanPermission) obj;
    return getName().equals(that.getName()) && actions.equals(that.actions);
  }
}
