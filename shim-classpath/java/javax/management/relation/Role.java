/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management.relation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.management.ObjectName;

public class Role implements Serializable {
  private static final long serialVersionUID = -279985518429862552L;

  private String myName = null;
  private List<ObjectName> myObjNameList = new ArrayList<ObjectName>();

  public Role(String roleName, List<ObjectName> roleValue) throws IllegalArgumentException {
    if (roleName == null || roleValue == null) {
      throw new IllegalArgumentException("Invalid parameter");
    }
    setRoleName(roleName);
    setRoleValue(roleValue);
  }

  public String getRoleName() {
    return myName;
  }

  public List<ObjectName> getRoleValue() {
    return myObjNameList;
  }

  public void setRoleName(String roleName) throws IllegalArgumentException {
    if (roleName == null) {
      throw new IllegalArgumentException("Invalid parameter");
    }
    myName = roleName;
  }

  public void setRoleValue(List<ObjectName> roleValue) throws IllegalArgumentException {
    if (roleValue == null) {
      throw new IllegalArgumentException("Invalid parameter");
    }
    myObjNameList = new ArrayList<ObjectName>(roleValue);
  }

  public String toString() {
    StringBuilder result = new StringBuilder();
    result.append("role name: " + myName + "; role value: ");
    for (Iterator<ObjectName> objNameIter = myObjNameList.iterator(); objNameIter.hasNext();) {
      ObjectName currObjName = objNameIter.next();
      result.append(currObjName.toString());
      if (objNameIter.hasNext()) {
        result.append(", ");
      }
    }
    return result.toString();
  }

  public Object clone() {
    try {
      return new Role(myName, myObjNameList);
    } catch (IllegalArgumentException exc) {
      return null;
    }
  }

  public static String roleValueToString(List<ObjectName> roleValue) throws IllegalArgumentException {
    if (roleValue == null) {
      throw new IllegalArgumentException("Invalid parameter");
    }
    StringBuilder result = new StringBuilder();
    for (ObjectName currObjName : roleValue) {
      if (result.length() > 0) {
        result.append("\n");
      }
      result.append(currObjName.toString());
    }
    return result.toString();
  }
}
