/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management.remote.rmi;

public interface RMIConnection extends java.io.Closeable, java.rmi.Remote {
  void addNotificationListener(javax.management.ObjectName objectName, javax.management.ObjectName objectName2, java.rmi.MarshalledObject marshalledObject, java.rmi.MarshalledObject marshalledObject2, javax.security.auth.Subject subject) throws javax.management.InstanceNotFoundException, java.io.IOException;
  java.lang.Integer[] addNotificationListeners(javax.management.ObjectName[] objectNames, java.rmi.MarshalledObject[] marshalledObjects, javax.security.auth.Subject[] subjects) throws javax.management.InstanceNotFoundException, java.io.IOException;
  void close() throws java.io.IOException;
  javax.management.ObjectInstance createMBean(java.lang.String string, javax.management.ObjectName objectName, java.rmi.MarshalledObject marshalledObject, java.lang.String[] strings, javax.security.auth.Subject subject) throws javax.management.ReflectionException, javax.management.InstanceAlreadyExistsException, javax.management.MBeanRegistrationException, javax.management.MBeanException, javax.management.NotCompliantMBeanException, java.io.IOException;
  javax.management.ObjectInstance createMBean(java.lang.String string, javax.management.ObjectName objectName, javax.management.ObjectName objectName2, java.rmi.MarshalledObject marshalledObject, java.lang.String[] strings, javax.security.auth.Subject subject) throws javax.management.ReflectionException, javax.management.InstanceAlreadyExistsException, javax.management.MBeanRegistrationException, javax.management.MBeanException, javax.management.NotCompliantMBeanException, javax.management.InstanceNotFoundException, java.io.IOException;
  javax.management.ObjectInstance createMBean(java.lang.String string, javax.management.ObjectName objectName, javax.management.ObjectName objectName2, javax.security.auth.Subject subject) throws javax.management.ReflectionException, javax.management.InstanceAlreadyExistsException, javax.management.MBeanRegistrationException, javax.management.MBeanException, javax.management.NotCompliantMBeanException, javax.management.InstanceNotFoundException, java.io.IOException;
  javax.management.ObjectInstance createMBean(java.lang.String string, javax.management.ObjectName objectName, javax.security.auth.Subject subject) throws javax.management.ReflectionException, javax.management.InstanceAlreadyExistsException, javax.management.MBeanRegistrationException, javax.management.MBeanException, javax.management.NotCompliantMBeanException, java.io.IOException;
  javax.management.remote.NotificationResult fetchNotifications(long l, int i, long l2) throws java.io.IOException;
  java.lang.Object getAttribute(javax.management.ObjectName objectName, java.lang.String string, javax.security.auth.Subject subject) throws javax.management.MBeanException, javax.management.AttributeNotFoundException, javax.management.InstanceNotFoundException, javax.management.ReflectionException, java.io.IOException;
  javax.management.AttributeList getAttributes(javax.management.ObjectName objectName, java.lang.String[] strings, javax.security.auth.Subject subject) throws javax.management.InstanceNotFoundException, javax.management.ReflectionException, java.io.IOException;
  java.lang.String getConnectionId() throws java.io.IOException;
  java.lang.String getDefaultDomain(javax.security.auth.Subject subject) throws java.io.IOException;
  java.lang.String[] getDomains(javax.security.auth.Subject subject) throws java.io.IOException;
  java.lang.Integer getMBeanCount(javax.security.auth.Subject subject) throws java.io.IOException;
  javax.management.MBeanInfo getMBeanInfo(javax.management.ObjectName objectName, javax.security.auth.Subject subject) throws javax.management.InstanceNotFoundException, javax.management.IntrospectionException, javax.management.ReflectionException, java.io.IOException;
  javax.management.ObjectInstance getObjectInstance(javax.management.ObjectName objectName, javax.security.auth.Subject subject) throws javax.management.InstanceNotFoundException, java.io.IOException;
  java.lang.Object invoke(javax.management.ObjectName objectName, java.lang.String string, java.rmi.MarshalledObject marshalledObject, java.lang.String[] strings, javax.security.auth.Subject subject) throws javax.management.InstanceNotFoundException, javax.management.MBeanException, javax.management.ReflectionException, java.io.IOException;
  boolean isInstanceOf(javax.management.ObjectName objectName, java.lang.String string, javax.security.auth.Subject subject) throws javax.management.InstanceNotFoundException, java.io.IOException;
  boolean isRegistered(javax.management.ObjectName objectName, javax.security.auth.Subject subject) throws java.io.IOException;
  java.util.Set<javax.management.ObjectInstance> queryMBeans(javax.management.ObjectName objectName, java.rmi.MarshalledObject marshalledObject, javax.security.auth.Subject subject) throws java.io.IOException;
  java.util.Set<javax.management.ObjectName> queryNames(javax.management.ObjectName objectName, java.rmi.MarshalledObject marshalledObject, javax.security.auth.Subject subject) throws java.io.IOException;
  void removeNotificationListener(javax.management.ObjectName objectName, javax.management.ObjectName objectName2, java.rmi.MarshalledObject marshalledObject, java.rmi.MarshalledObject marshalledObject2, javax.security.auth.Subject subject) throws javax.management.InstanceNotFoundException, javax.management.ListenerNotFoundException, java.io.IOException;
  void removeNotificationListener(javax.management.ObjectName objectName, javax.management.ObjectName objectName2, javax.security.auth.Subject subject) throws javax.management.InstanceNotFoundException, javax.management.ListenerNotFoundException, java.io.IOException;
  void removeNotificationListeners(javax.management.ObjectName objectName, java.lang.Integer[] integers, javax.security.auth.Subject subject) throws javax.management.InstanceNotFoundException, javax.management.ListenerNotFoundException, java.io.IOException;
  void setAttribute(javax.management.ObjectName objectName, java.rmi.MarshalledObject marshalledObject, javax.security.auth.Subject subject) throws javax.management.InstanceNotFoundException, javax.management.AttributeNotFoundException, javax.management.InvalidAttributeValueException, javax.management.MBeanException, javax.management.ReflectionException, java.io.IOException;
  javax.management.AttributeList setAttributes(javax.management.ObjectName objectName, java.rmi.MarshalledObject marshalledObject, javax.security.auth.Subject subject) throws javax.management.InstanceNotFoundException, javax.management.ReflectionException, java.io.IOException;
  void unregisterMBean(javax.management.ObjectName objectName, javax.security.auth.Subject subject) throws javax.management.InstanceNotFoundException, javax.management.MBeanRegistrationException, java.io.IOException;
}
