/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management;

public interface MBeanServer extends MBeanServerConnection {
  ObjectInstance createMBean(String p0, ObjectName p1) throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException;

  ObjectInstance createMBean(String p0, ObjectName p1, ObjectName p2) throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException, InstanceNotFoundException;

  ObjectInstance createMBean(String p0, ObjectName p1, Object[] p2, String[] p3) throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException;

  ObjectInstance createMBean(String p0, ObjectName p1, ObjectName p2, Object[] p3, String[] p4) throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException, InstanceNotFoundException;

  ObjectInstance registerMBean(Object p0, ObjectName p1) throws InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException;

  void unregisterMBean(ObjectName p0) throws InstanceNotFoundException, MBeanRegistrationException;

  ObjectInstance getObjectInstance(ObjectName p0) throws InstanceNotFoundException;

  java.util.Set<ObjectInstance> queryMBeans(ObjectName p0, QueryExp p1);

  java.util.Set<ObjectName> queryNames(ObjectName p0, QueryExp p1);

  boolean isRegistered(ObjectName p0);

  Integer getMBeanCount();

  Object getAttribute(ObjectName p0, String p1) throws MBeanException, AttributeNotFoundException, InstanceNotFoundException, ReflectionException;

  AttributeList getAttributes(ObjectName p0, String[] p1) throws InstanceNotFoundException, ReflectionException;

  void setAttribute(ObjectName p0, Attribute p1) throws InstanceNotFoundException, AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException;

  AttributeList setAttributes(ObjectName p0, AttributeList p1) throws InstanceNotFoundException, ReflectionException;

  Object invoke(ObjectName p0, String p1, Object[] p2, String[] p3) throws InstanceNotFoundException, MBeanException, ReflectionException;

  String getDefaultDomain();

  String[] getDomains();

  void addNotificationListener(ObjectName p0, NotificationListener p1, NotificationFilter p2, Object p3) throws InstanceNotFoundException;

  void addNotificationListener(ObjectName p0, ObjectName p1, NotificationFilter p2, Object p3) throws InstanceNotFoundException;

  void removeNotificationListener(ObjectName p0, ObjectName p1) throws InstanceNotFoundException, ListenerNotFoundException;

  void removeNotificationListener(ObjectName p0, ObjectName p1, NotificationFilter p2, Object p3) throws InstanceNotFoundException, ListenerNotFoundException;

  void removeNotificationListener(ObjectName p0, NotificationListener p1) throws InstanceNotFoundException, ListenerNotFoundException;

  void removeNotificationListener(ObjectName p0, NotificationListener p1, NotificationFilter p2, Object p3) throws InstanceNotFoundException, ListenerNotFoundException;

  MBeanInfo getMBeanInfo(ObjectName p0) throws InstanceNotFoundException, IntrospectionException, ReflectionException;

  boolean isInstanceOf(ObjectName p0, String p1) throws InstanceNotFoundException;

  Object instantiate(String p0) throws ReflectionException, MBeanException;

  Object instantiate(String p0, ObjectName p1) throws ReflectionException, MBeanException, InstanceNotFoundException;

  Object instantiate(String p0, Object[] p1, String[] p2) throws ReflectionException, MBeanException;

  Object instantiate(String p0, ObjectName p1, Object[] p2, String[] p3) throws ReflectionException, MBeanException, InstanceNotFoundException;

  default java.io.ObjectInputStream deserialize(ObjectName p0, byte[] p1) throws InstanceNotFoundException, OperationsException {
    throw new UnsupportedOperationException();
  }

  default java.io.ObjectInputStream deserialize(String p0, byte[] p1) throws OperationsException, ReflectionException {
    throw new UnsupportedOperationException();
  }

  default java.io.ObjectInputStream deserialize(String p0, ObjectName p1, byte[] p2) throws InstanceNotFoundException, OperationsException, ReflectionException {
    throw new UnsupportedOperationException();
  }

  ClassLoader getClassLoaderFor(ObjectName p0) throws InstanceNotFoundException;

  ClassLoader getClassLoader(ObjectName p0) throws InstanceNotFoundException;

  javax.management.loading.ClassLoaderRepository getClassLoaderRepository();
}
