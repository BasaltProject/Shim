/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management.modelmbean;

public interface ModelMBeanNotificationBroadcaster extends javax.management.NotificationBroadcaster {
  void addAttributeChangeNotificationListener(javax.management.NotificationListener notificationListener, java.lang.String string, java.lang.Object object) throws javax.management.MBeanException, javax.management.RuntimeOperationsException, java.lang.IllegalArgumentException;
  void removeAttributeChangeNotificationListener(javax.management.NotificationListener notificationListener, java.lang.String string) throws javax.management.MBeanException, javax.management.RuntimeOperationsException, javax.management.ListenerNotFoundException;
  void sendAttributeChangeNotification(javax.management.Attribute attribute, javax.management.Attribute attribute2) throws javax.management.MBeanException, javax.management.RuntimeOperationsException;
  void sendAttributeChangeNotification(javax.management.AttributeChangeNotification attributeChangeNotification) throws javax.management.MBeanException, javax.management.RuntimeOperationsException;
  void sendNotification(java.lang.String string) throws javax.management.MBeanException, javax.management.RuntimeOperationsException;
  void sendNotification(javax.management.Notification notification) throws javax.management.MBeanException, javax.management.RuntimeOperationsException;
}
