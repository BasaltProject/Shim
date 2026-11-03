/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management.relation;

import java.io.Serializable;

public class RoleResult implements Serializable {
  private static final long serialVersionUID = -6304063118040985512L;

  private RoleList myRoleList = null;
  private RoleUnresolvedList myRoleUnresList = null;

  public RoleResult(RoleList list, RoleUnresolvedList unresolvedList) {
    setRoles(list);
    setRolesUnresolved(unresolvedList);
  }

  public RoleList getRoles() {
    return myRoleList;
  }

  public RoleUnresolvedList getRolesUnresolved() {
    return myRoleUnresList;
  }

  public void setRoles(RoleList list) {
    if (list != null) {
      myRoleList = new RoleList();
      for (java.util.Iterator<Role> roleIter = list.asList().iterator(); roleIter.hasNext();) {
        Role currRole = roleIter.next();
        myRoleList.add((Role) (currRole.clone()));
      }
    } else {
      myRoleList = null;
    }
  }

  public void setRolesUnresolved(RoleUnresolvedList unresolvedList) {
    if (unresolvedList != null) {
      myRoleUnresList = new RoleUnresolvedList();
      for (java.util.Iterator<RoleUnresolved> roleUnresIter = unresolvedList.asList().iterator(); roleUnresIter.hasNext();) {
        RoleUnresolved currRoleUnres = roleUnresIter.next();
        myRoleUnresList.add((RoleUnresolved) (currRoleUnres.clone()));
      }
    } else {
      myRoleUnresList = null;
    }
  }
}
