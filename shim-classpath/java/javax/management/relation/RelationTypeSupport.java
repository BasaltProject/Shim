/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management.relation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RelationTypeSupport implements RelationType {
  private static final long serialVersionUID = 4611072955724144607L;

  private String myTypeName = null;
  private Map<String, RoleInfo> myRoleName2InfoMap = new HashMap<String, RoleInfo>();
  private boolean myIsInRelServFlg = false;

  public RelationTypeSupport(String relationTypeName, RoleInfo[] roleInfoArray) throws IllegalArgumentException,
      InvalidRelationTypeException {
    if (relationTypeName == null || roleInfoArray == null) {
      throw new IllegalArgumentException("Invalid parameter");
    }
    myTypeName = relationTypeName;
    initialize(roleInfoArray);
  }

  protected RelationTypeSupport(String relationTypeName) {
    if (relationTypeName == null) {
      throw new IllegalArgumentException("Invalid parameter");
    }
    myTypeName = relationTypeName;
  }

  public String getRelationTypeName() {
    return myTypeName;
  }

  public List<RoleInfo> getRoleInfos() {
    return new ArrayList<RoleInfo>(myRoleName2InfoMap.values());
  }

  public RoleInfo getRoleInfo(String roleInfoName) throws IllegalArgumentException, RoleInfoNotFoundException {
    if (roleInfoName == null) {
      throw new IllegalArgumentException("Invalid parameter");
    }
    RoleInfo result = myRoleName2InfoMap.get(roleInfoName);
    if (result == null) {
      StringBuilder excMsgStrB = new StringBuilder();
      excMsgStrB.append("No role info for role ");
      excMsgStrB.append(roleInfoName);
      throw new RoleInfoNotFoundException(excMsgStrB.toString());
    }
    return result;
  }

  protected void addRoleInfo(RoleInfo roleInfo) throws IllegalArgumentException, InvalidRelationTypeException {
    if (roleInfo == null) {
      throw new IllegalArgumentException("Invalid parameter");
    }
    if (myIsInRelServFlg) {
      throw new InvalidRelationTypeException("Relation type cannot be updated as it is declared in the Relation Service");
    }
    String roleName = roleInfo.getName();
    if (myRoleName2InfoMap.containsKey(roleName)) {
      StringBuilder excMsgStrB = new StringBuilder();
      excMsgStrB.append("Two role infos provided for role ");
      excMsgStrB.append(roleName);
      throw new InvalidRelationTypeException(excMsgStrB.toString());
    }
    myRoleName2InfoMap.put(roleName, new RoleInfo(roleInfo));
  }

  void setRelationServiceFlag(boolean flag) {
    myIsInRelServFlg = flag;
  }

  private void initialize(RoleInfo[] roleInfoArray) throws IllegalArgumentException, InvalidRelationTypeException {
    if (roleInfoArray == null) {
      throw new IllegalArgumentException("Invalid parameter");
    }
    for (int i = 0; i < roleInfoArray.length; i++) {
      RoleInfo currRoleInfo = roleInfoArray[i];
      if (currRoleInfo == null) {
        throw new InvalidRelationTypeException("Null role info provided");
      }
      String roleName = currRoleInfo.getName();
      if (myRoleName2InfoMap.containsKey(roleName)) {
        StringBuilder excMsgStrB = new StringBuilder();
        excMsgStrB.append("Two role infos provided for role ");
        excMsgStrB.append(roleName);
        throw new InvalidRelationTypeException(excMsgStrB.toString());
      }
      myRoleName2InfoMap.put(roleName, new RoleInfo(currRoleInfo));
    }
  }
}
