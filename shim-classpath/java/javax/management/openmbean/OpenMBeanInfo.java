/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management.openmbean;

public interface OpenMBeanInfo {
  boolean equals(java.lang.Object object);
  javax.management.MBeanAttributeInfo[] getAttributes();
  java.lang.String getClassName();
  javax.management.MBeanConstructorInfo[] getConstructors();
  java.lang.String getDescription();
  javax.management.MBeanNotificationInfo[] getNotifications();
  javax.management.MBeanOperationInfo[] getOperations();
  int hashCode();
  java.lang.String toString();
}
