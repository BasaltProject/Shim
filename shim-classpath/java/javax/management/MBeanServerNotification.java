/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management;

public class MBeanServerNotification extends Notification {
  private static final long serialVersionUID = 2876477500475969677L;

  public static final String REGISTRATION_NOTIFICATION = "JMX.mbean.registered";
  public static final String UNREGISTRATION_NOTIFICATION = "JMX.mbean.unregistered";

  private final ObjectName objectName;

  public MBeanServerNotification(String type, Object source, long sequenceNumber, ObjectName objectName) {
    super(type, source, sequenceNumber);
    this.objectName = objectName;
  }

  public ObjectName getMBeanName() {
    return objectName;
  }

  @Override
  public String toString() {
    return super.toString() + "[mbeanName=" + objectName + "]";
  }
}
