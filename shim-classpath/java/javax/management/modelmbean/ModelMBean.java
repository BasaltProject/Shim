/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management.modelmbean;

public interface ModelMBean extends javax.management.DynamicMBean, javax.management.PersistentMBean, javax.management.modelmbean.ModelMBeanNotificationBroadcaster {
  void setManagedResource(java.lang.Object object, java.lang.String string) throws javax.management.MBeanException, javax.management.RuntimeOperationsException, javax.management.InstanceNotFoundException, javax.management.modelmbean.InvalidTargetObjectTypeException;
  void setModelMBeanInfo(javax.management.modelmbean.ModelMBeanInfo modelMBeanInfo) throws javax.management.MBeanException, javax.management.RuntimeOperationsException;
}
