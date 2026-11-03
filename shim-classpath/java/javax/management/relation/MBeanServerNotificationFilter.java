/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management.relation;

import java.util.List;
import java.util.Vector;
import javax.management.MBeanServerNotification;
import javax.management.Notification;
import javax.management.NotificationFilterSupport;
import javax.management.ObjectName;

public class MBeanServerNotificationFilter extends NotificationFilterSupport {
  private static final long serialVersionUID = 2605900539589789736L;

  private List<ObjectName> selectedNames = new Vector<ObjectName>();
  private List<ObjectName> deselectedNames = null;

  public MBeanServerNotificationFilter() {
    super();
    enableType(MBeanServerNotification.REGISTRATION_NOTIFICATION);
    enableType(MBeanServerNotification.UNREGISTRATION_NOTIFICATION);
  }

  public synchronized void disableAllObjectNames() {
    selectedNames = new Vector<ObjectName>();
    deselectedNames = null;
  }

  public synchronized void disableObjectName(ObjectName objectName) throws IllegalArgumentException {
    if (objectName == null) {
      throw new IllegalArgumentException("Invalid parameter");
    }
    if (selectedNames != null) {
      if (selectedNames.size() != 0) {
        selectedNames.remove(objectName);
      }
    }
    if (deselectedNames != null) {
      if (!(deselectedNames.contains(objectName))) {
        deselectedNames.add(objectName);
      }
    }
  }

  public synchronized void enableAllObjectNames() {
    selectedNames = null;
    deselectedNames = new Vector<ObjectName>();
  }

  public synchronized void enableObjectName(ObjectName objectName) throws IllegalArgumentException {
    if (objectName == null) {
      throw new IllegalArgumentException("Invalid parameter");
    }
    if (selectedNames != null) {
      if (!(selectedNames.contains(objectName))) {
        selectedNames.add(objectName);
      }
    }
    if (deselectedNames != null) {
      deselectedNames.remove(objectName);
    }
  }

  public synchronized Vector<ObjectName> getEnabledObjectNames() {
    if (selectedNames != null) {
      return new Vector<ObjectName>(selectedNames);
    } else {
      return null;
    }
  }

  public synchronized Vector<ObjectName> getDisabledObjectNames() {
    if (deselectedNames != null) {
      return new Vector<ObjectName>(deselectedNames);
    } else {
      return null;
    }
  }

  public synchronized boolean isNotificationEnabled(Notification notif) throws IllegalArgumentException {
    if (notif == null) {
      throw new IllegalArgumentException("Invalid parameter.");
    }
    String ntfType = notif.getType();
    Vector<String> enabledTypes = getEnabledTypes();
    if (!(enabledTypes.contains(ntfType))) {
      return false;
    }
    MBeanServerNotification mbsNtf = (MBeanServerNotification) notif;
    ObjectName objName = mbsNtf.getMBeanName();
    boolean isSelectedFlg = false;
    if (selectedNames != null) {
      if (selectedNames.size() == 0) {
        return false;
      }
      isSelectedFlg = selectedNames.contains(objName);
      if (!isSelectedFlg) {
        return false;
      }
    }
    if (deselectedNames != null) {
      boolean isDeselectedFlg = deselectedNames.contains(objName);
      if (isDeselectedFlg) {
        return false;
      }
    }
    return true;
  }
}
