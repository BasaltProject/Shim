/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management.modelmbean;

public interface ModelMBeanInfo {
  java.lang.Object clone();
  javax.management.modelmbean.ModelMBeanAttributeInfo getAttribute(java.lang.String string) throws javax.management.MBeanException, javax.management.RuntimeOperationsException;
  javax.management.MBeanAttributeInfo[] getAttributes();
  java.lang.String getClassName();
  javax.management.MBeanConstructorInfo[] getConstructors();
  java.lang.String getDescription();
  javax.management.Descriptor getDescriptor(java.lang.String string, java.lang.String string2) throws javax.management.MBeanException, javax.management.RuntimeOperationsException;
  javax.management.Descriptor[] getDescriptors(java.lang.String string) throws javax.management.MBeanException, javax.management.RuntimeOperationsException;
  javax.management.Descriptor getMBeanDescriptor() throws javax.management.MBeanException, javax.management.RuntimeOperationsException;
  javax.management.modelmbean.ModelMBeanNotificationInfo getNotification(java.lang.String string) throws javax.management.MBeanException, javax.management.RuntimeOperationsException;
  javax.management.MBeanNotificationInfo[] getNotifications();
  javax.management.modelmbean.ModelMBeanOperationInfo getOperation(java.lang.String string) throws javax.management.MBeanException, javax.management.RuntimeOperationsException;
  javax.management.MBeanOperationInfo[] getOperations();
  void setDescriptor(javax.management.Descriptor descriptor, java.lang.String string) throws javax.management.MBeanException, javax.management.RuntimeOperationsException;
  void setDescriptors(javax.management.Descriptor[] descriptors) throws javax.management.MBeanException, javax.management.RuntimeOperationsException;
  void setMBeanDescriptor(javax.management.Descriptor descriptor) throws javax.management.MBeanException, javax.management.RuntimeOperationsException;
}
