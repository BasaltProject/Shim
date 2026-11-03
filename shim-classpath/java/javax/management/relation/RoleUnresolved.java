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

public class RoleUnresolved implements Serializable {
  private static final long serialVersionUID = -48350262537070138L;

  private String myRoleName = null;
  private List<ObjectName> myRoleValue = null;
  private int myPbType = 0;

  public RoleUnresolved(String name, List<ObjectName> value, int pbType) throws IllegalArgumentException {
    if (name == null) {
      throw new IllegalArgumentException("Invalid parameter");
    }
    setRoleName(name);
    setRoleValue(value);
    setProblemType(pbType);
  }

  public String getRoleName() {
    return myRoleName;
  }

  public List<ObjectName> getRoleValue() {
    return myRoleValue;
  }

  public int getProblemType() {
    return myPbType;
  }

  public void setRoleName(String name) throws IllegalArgumentException {
    if (name == null) {
      throw new IllegalArgumentException("Invalid parameter");
    }
    myRoleName = name;
  }

  public void setRoleValue(List<ObjectName> value) {
    if (value != null) {
      myRoleValue = new ArrayList<ObjectName>(value);
    } else {
      myRoleValue = null;
    }
  }

  public void setProblemType(int pbType) throws IllegalArgumentException {
    if (!(RoleStatus.isRoleStatus(pbType))) {
      throw new IllegalArgumentException("Incorrect problem type.");
    }
    myPbType = pbType;
  }

  public Object clone() {
    try {
      return new RoleUnresolved(myRoleName, myRoleValue, myPbType);
    } catch (IllegalArgumentException exc) {
      return null;
    }
  }

  public String toString() {
    StringBuilder result = new StringBuilder();
    result.append("role name: " + myRoleName);
    if (myRoleValue != null) {
      result.append("; value: ");
      for (Iterator<ObjectName> objNameIter = myRoleValue.iterator(); objNameIter.hasNext();) {
        ObjectName currObjName = objNameIter.next();
        result.append(currObjName.toString());
        if (objNameIter.hasNext()) {
          result.append(", ");
        }
      }
    }
    result.append("; problem type: " + myPbType);
    return result.toString();
  }
}
