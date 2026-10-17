/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management;

public interface MBeanServerConnection {
  ObjectInstance createMBean(String p0, ObjectName p1) throws ReflectionException, InstanceAlreadyExistsException,
      MBeanRegistrationException, MBeanException, NotCompliantMBeanException, java.io.IOException;

  ObjectInstance createMBean(String p0, ObjectName p1, ObjectName p2) throws ReflectionException,
      InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException, InstanceNotFoundException, java.io.IOException;

  ObjectInstance createMBean(String p0, ObjectName p1, Object[] p2, String[] p3) throws ReflectionException,
      InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException, java.io.IOException;

  ObjectInstance createMBean(String p0, ObjectName p1, ObjectName p2, Object[] p3, String[] p4)
      throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException, InstanceNotFoundException, java.io.IOException;

  void unregisterMBean(ObjectName p0) throws InstanceNotFoundException, MBeanRegistrationException, java.io.IOException;

  ObjectInstance getObjectInstance(ObjectName p0) throws InstanceNotFoundException, java.io.IOException;

  java.util.Set<ObjectInstance> queryMBeans(ObjectName p0, QueryExp p1) throws java.io.IOException;

  java.util.Set<ObjectName> queryNames(ObjectName p0, QueryExp p1) throws java.io.IOException;

  boolean isRegistered(ObjectName p0) throws java.io.IOException;

  Integer getMBeanCount() throws java.io.IOException;

  Object getAttribute(ObjectName p0, String p1) throws MBeanException, AttributeNotFoundException,
      InstanceNotFoundException, ReflectionException, java.io.IOException;

  AttributeList getAttributes(ObjectName p0, String[] p1) throws InstanceNotFoundException, ReflectionException,
      java.io.IOException;

  void setAttribute(ObjectName p0, Attribute p1) throws InstanceNotFoundException, AttributeNotFoundException,
      InvalidAttributeValueException, MBeanException, ReflectionException, java.io.IOException;

  AttributeList setAttributes(ObjectName p0, AttributeList p1) throws InstanceNotFoundException, ReflectionException,
      java.io.IOException;

  Object invoke(ObjectName p0, String p1, Object[] p2, String[] p3) throws InstanceNotFoundException, MBeanException,
      ReflectionException, java.io.IOException;

  String getDefaultDomain() throws java.io.IOException;

  String[] getDomains() throws java.io.IOException;

  void addNotificationListener(ObjectName p0, NotificationListener p1, NotificationFilter p2, Object p3)
      throws InstanceNotFoundException, java.io.IOException;

  void addNotificationListener(ObjectName p0, ObjectName p1, NotificationFilter p2, Object p3)
      throws InstanceNotFoundException, java.io.IOException;

  void removeNotificationListener(ObjectName p0, ObjectName p1) throws InstanceNotFoundException,
      ListenerNotFoundException, java.io.IOException;

  void removeNotificationListener(ObjectName p0, ObjectName p1, NotificationFilter p2, Object p3)
      throws InstanceNotFoundException, ListenerNotFoundException, java.io.IOException;

  void removeNotificationListener(ObjectName p0, NotificationListener p1) throws InstanceNotFoundException,
      ListenerNotFoundException, java.io.IOException;

  void removeNotificationListener(ObjectName p0, NotificationListener p1, NotificationFilter p2, Object p3)
      throws InstanceNotFoundException, ListenerNotFoundException, java.io.IOException;

  MBeanInfo getMBeanInfo(ObjectName p0) throws InstanceNotFoundException, IntrospectionException,
      ReflectionException, java.io.IOException;

  boolean isInstanceOf(ObjectName p0, String p1) throws InstanceNotFoundException, java.io.IOException;
}
