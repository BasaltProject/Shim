/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management.relation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.management.Notification;
import javax.management.ObjectName;

public class RelationNotification extends Notification {
  private static final long serialVersionUID = -6871117877523310399L;

  public static final String RELATION_BASIC_CREATION = "jmx.relation.creation.basic";
  public static final String RELATION_MBEAN_CREATION = "jmx.relation.creation.mbean";
  public static final String RELATION_BASIC_UPDATE = "jmx.relation.update.basic";
  public static final String RELATION_MBEAN_UPDATE = "jmx.relation.update.mbean";
  public static final String RELATION_BASIC_REMOVAL = "jmx.relation.removal.basic";
  public static final String RELATION_MBEAN_REMOVAL = "jmx.relation.removal.mbean";

  private String myRelId = null;
  private ObjectName myRelObjName = null;
  private List<ObjectName> myUnregMBeanList = null;
  private String myRelTypeName = null;
  private String myRoleName = null;
  private List<ObjectName> myOldRoleValue = null;
  private List<ObjectName> myNewRoleValue = null;

  public RelationNotification(String notifType, Object sourceObj, long sequence, long timeStamp, String message,
      String id, String typeName, ObjectName objectName, List<ObjectName> unregMBeanList)
      throws IllegalArgumentException {
    super(notifType, sourceObj, sequence, timeStamp, message);
    if (!isValidBasicStrict(notifType, sourceObj, id, typeName) || !isValidCreate(notifType)) {
      throw new IllegalArgumentException("Invalid parameter.");
    }
    myRelId = id;
    myRelTypeName = typeName;
    myRelObjName = safeGetObjectName(objectName);
    myUnregMBeanList = safeGetObjectNameList(unregMBeanList);
  }

  public RelationNotification(String notifType, Object sourceObj, long sequence, long timeStamp, String message,
      String id, String typeName, ObjectName objectName, String name, List<ObjectName> newValue,
      List<ObjectName> oldValue) throws IllegalArgumentException {
    super(notifType, sourceObj, sequence, timeStamp, message);
    if (!isValidBasicStrict(notifType, sourceObj, id, typeName) || !isValidUpdate(notifType, name, newValue, oldValue)) {
      throw new IllegalArgumentException("Invalid parameter.");
    }
    myRelId = id;
    myRelTypeName = typeName;
    myRelObjName = safeGetObjectName(objectName);
    myRoleName = name;
    myNewRoleValue = safeGetObjectNameList(newValue);
    myOldRoleValue = safeGetObjectNameList(oldValue);
  }

  public String getRelationId() {
    return myRelId;
  }

  public String getRelationTypeName() {
    return myRelTypeName;
  }

  public ObjectName getObjectName() {
    return myRelObjName;
  }

  public List<ObjectName> getMBeansToUnregister() {
    List<ObjectName> result;
    if (myUnregMBeanList != null) {
      result = new ArrayList<ObjectName>(myUnregMBeanList);
    } else {
      result = Collections.emptyList();
    }
    return result;
  }

  public String getRoleName() {
    String result = null;
    if (myRoleName != null) {
      result = myRoleName;
    }
    return result;
  }

  public List<ObjectName> getOldRoleValue() {
    List<ObjectName> result;
    if (myOldRoleValue != null) {
      result = new ArrayList<ObjectName>(myOldRoleValue);
    } else {
      result = Collections.emptyList();
    }
    return result;
  }

  public List<ObjectName> getNewRoleValue() {
    List<ObjectName> result;
    if (myNewRoleValue != null) {
      result = new ArrayList<ObjectName>(myNewRoleValue);
    } else {
      result = Collections.emptyList();
    }
    return result;
  }

  private boolean isValidBasicStrict(String notifType, Object sourceObj, String id, String typeName) {
    if (sourceObj == null) {
      return false;
    }
    return isValidBasic(notifType, sourceObj, id, typeName);
  }

  private boolean isValidBasic(String notifType, Object sourceObj, String id, String typeName) {
    if (notifType == null || id == null || typeName == null) {
      return false;
    }
    if (sourceObj != null && (!(sourceObj instanceof RelationService) && !(sourceObj instanceof ObjectName))) {
      return false;
    }
    return true;
  }

  private boolean isValidCreate(String notifType) {
    String[] validTypes = {RelationNotification.RELATION_BASIC_CREATION, RelationNotification.RELATION_MBEAN_CREATION,
        RelationNotification.RELATION_BASIC_REMOVAL, RelationNotification.RELATION_MBEAN_REMOVAL};
    for (String validType : validTypes) {
      if (notifType.equals(validType)) {
        return true;
      }
    }
    return false;
  }

  private boolean isValidUpdate(String notifType, String name, List<ObjectName> newValue, List<ObjectName> oldValue) {
    if (!(notifType.equals(RelationNotification.RELATION_BASIC_UPDATE)
        || notifType.equals(RelationNotification.RELATION_MBEAN_UPDATE))) {
      return false;
    }
    if (name == null || newValue == null || oldValue == null) {
      return false;
    }
    return true;
  }

  private ArrayList<ObjectName> safeGetObjectNameList(List<ObjectName> src) {
    ArrayList<ObjectName> dest = null;
    if (src != null) {
      dest = new ArrayList<ObjectName>();
      for (ObjectName item : src) {
        dest.add(ObjectName.getInstance(item));
      }
    }
    return dest;
  }

  private ObjectName safeGetObjectName(ObjectName src) {
    ObjectName dest = null;
    if (src != null) {
      dest = ObjectName.getInstance(src);
    }
    return dest;
  }
}
