/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management.relation;

import java.io.Serializable;
import javax.management.NotCompliantMBeanException;

public class RoleInfo implements Serializable {
  private static final long serialVersionUID = 2504952983494636987L;

  public static final int ROLE_CARDINALITY_INFINITY = -1;

  private String myName = null;
  private String myRefMBeanClassName = null;
  private boolean myIsReadableFlg;
  private boolean myIsWritableFlg;
  private String myDescription = null;
  private int myMinDegree;
  private int myMaxDegree;

  public RoleInfo(String roleName, String mbeanClassName, boolean read, boolean write, int min, int max,
      String descr) throws IllegalArgumentException, InvalidRoleInfoException, ClassNotFoundException,
      NotCompliantMBeanException {
    init(roleName, mbeanClassName, read, write, min, max, descr);
  }

  public RoleInfo(String roleName, String mbeanClassName, boolean read, boolean write)
      throws IllegalArgumentException, ClassNotFoundException, NotCompliantMBeanException {
    try {
      init(roleName, mbeanClassName, read, write, 1, 1, null);
    } catch (InvalidRoleInfoException exc) {
    }
  }

  public RoleInfo(String roleName, String mbeanClassName) throws IllegalArgumentException,
      ClassNotFoundException, NotCompliantMBeanException {
    try {
      init(roleName, mbeanClassName, true, true, 1, 1, null);
    } catch (InvalidRoleInfoException exc) {
    }
  }

  public RoleInfo(RoleInfo roleInfo) throws IllegalArgumentException {
    if (roleInfo == null) {
      throw new IllegalArgumentException("Invalid parameter");
    }
    try {
      init(roleInfo.getName(), roleInfo.getRefMBeanClassName(), roleInfo.isReadable(), roleInfo.isWritable(),
          roleInfo.getMinDegree(), roleInfo.getMaxDegree(), roleInfo.getDescription());
    } catch (InvalidRoleInfoException exc3) {
    }
  }

  public String getName() {
    return myName;
  }

  public boolean isReadable() {
    return myIsReadableFlg;
  }

  public boolean isWritable() {
    return myIsWritableFlg;
  }

  public String getDescription() {
    return myDescription;
  }

  public int getMinDegree() {
    return myMinDegree;
  }

  public int getMaxDegree() {
    return myMaxDegree;
  }

  public String getRefMBeanClassName() {
    return myRefMBeanClassName;
  }

  public boolean checkMinDegree(int value) {
    if (value >= ROLE_CARDINALITY_INFINITY && (myMinDegree == ROLE_CARDINALITY_INFINITY || value >= myMinDegree)) {
      return true;
    } else {
      return false;
    }
  }

  public boolean checkMaxDegree(int value) {
    if (value >= ROLE_CARDINALITY_INFINITY
        && (myMaxDegree == ROLE_CARDINALITY_INFINITY || (value != ROLE_CARDINALITY_INFINITY && value <= myMaxDegree))) {
      return true;
    } else {
      return false;
    }
  }

  public String toString() {
    StringBuilder result = new StringBuilder();
    result.append("role info name: " + myName);
    result.append("; isReadable: " + myIsReadableFlg);
    result.append("; isWritable: " + myIsWritableFlg);
    result.append("; description: " + myDescription);
    result.append("; minimum degree: " + myMinDegree);
    result.append("; maximum degree: " + myMaxDegree);
    result.append("; MBean class: " + myRefMBeanClassName);
    return result.toString();
  }

  private void init(String roleName, String mbeanClassName, boolean read, boolean write, int min, int max,
      String descr) throws IllegalArgumentException, InvalidRoleInfoException {
    if (roleName == null || mbeanClassName == null) {
      throw new IllegalArgumentException("Invalid parameter");
    }
    myName = roleName;
    myIsReadableFlg = read;
    myIsWritableFlg = write;
    if (descr != null) {
      myDescription = descr;
    }
    boolean invalidRoleInfoFlg = false;
    StringBuilder excMsgStrB = new StringBuilder();
    if (max != ROLE_CARDINALITY_INFINITY && (min == ROLE_CARDINALITY_INFINITY || min > max)) {
      excMsgStrB.append("Minimum degree ");
      excMsgStrB.append(min);
      excMsgStrB.append(" is greater than maximum degree ");
      excMsgStrB.append(max);
      invalidRoleInfoFlg = true;
    } else if (min < ROLE_CARDINALITY_INFINITY || max < ROLE_CARDINALITY_INFINITY) {
      excMsgStrB.append("Minimum or maximum degree has an illegal value, must be [0, ROLE_CARDINALITY_INFINITY].");
      invalidRoleInfoFlg = true;
    }
    if (invalidRoleInfoFlg) {
      throw new InvalidRoleInfoException(excMsgStrB.toString());
    }
    myMinDegree = min;
    myMaxDegree = max;
    myRefMBeanClassName = mbeanClassName;
  }
}
