/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management.remote.rmi;

public final class RMIConnectionImpl_Stub extends java.rmi.server.RemoteStub implements javax.management.remote.rmi.RMIConnection {
  private static final long serialVersionUID = 2L;


  public RMIConnectionImpl_Stub(java.rmi.server.RemoteRef remoteRef) {
    super(remoteRef);
  }

  public void addNotificationListener(javax.management.ObjectName objectName, javax.management.ObjectName objectName2, java.rmi.MarshalledObject marshalledObject, java.rmi.MarshalledObject marshalledObject2, javax.security.auth.Subject subject) throws java.io.IOException, javax.management.InstanceNotFoundException {
    try {
      call("addNotificationListener", new Class<?>[] {javax.management.ObjectName.class, javax.management.ObjectName.class, java.rmi.MarshalledObject.class, java.rmi.MarshalledObject.class, javax.security.auth.Subject.class}, new Object[] {objectName, objectName2, marshalledObject, marshalledObject2, subject});
    } catch (Exception e) {
      if (e instanceof RuntimeException) {
        throw (RuntimeException) e;
      }
      if (e instanceof java.io.IOException) {
        throw (java.io.IOException) e;
      }
      if (e instanceof javax.management.InstanceNotFoundException) {
        throw (javax.management.InstanceNotFoundException) e;
      }
      throw new java.rmi.UnexpectedException("undeclared checked exception", e);
    }
  }
  public java.lang.Integer[] addNotificationListeners(javax.management.ObjectName[] objectNames, java.rmi.MarshalledObject[] marshalledObjects, javax.security.auth.Subject[] subjects) throws java.io.IOException, javax.management.InstanceNotFoundException {
    try {
      return (java.lang.Integer[]) call("addNotificationListeners", new Class<?>[] {javax.management.ObjectName[].class, java.rmi.MarshalledObject[].class, javax.security.auth.Subject[].class}, new Object[] {objectNames, marshalledObjects, subjects});
    } catch (Exception e) {
      if (e instanceof RuntimeException) {
        throw (RuntimeException) e;
      }
      if (e instanceof java.io.IOException) {
        throw (java.io.IOException) e;
      }
      if (e instanceof javax.management.InstanceNotFoundException) {
        throw (javax.management.InstanceNotFoundException) e;
      }
      throw new java.rmi.UnexpectedException("undeclared checked exception", e);
    }
  }
  public void close() throws java.io.IOException {
    try {
      call("close", new Class<?>[] {}, new Object[] {});
    } catch (Exception e) {
      if (e instanceof RuntimeException) {
        throw (RuntimeException) e;
      }
      if (e instanceof java.io.IOException) {
        throw (java.io.IOException) e;
      }
      throw new java.rmi.UnexpectedException("undeclared checked exception", e);
    }
  }
  public javax.management.ObjectInstance createMBean(java.lang.String string, javax.management.ObjectName objectName, java.rmi.MarshalledObject marshalledObject, java.lang.String[] strings, javax.security.auth.Subject subject) throws java.io.IOException, javax.management.InstanceAlreadyExistsException, javax.management.MBeanException, javax.management.MBeanRegistrationException, javax.management.NotCompliantMBeanException, javax.management.ReflectionException {
    try {
      return (javax.management.ObjectInstance) call("createMBean", new Class<?>[] {java.lang.String.class, javax.management.ObjectName.class, java.rmi.MarshalledObject.class, java.lang.String[].class, javax.security.auth.Subject.class}, new Object[] {string, objectName, marshalledObject, strings, subject});
    } catch (Exception e) {
      if (e instanceof RuntimeException) {
        throw (RuntimeException) e;
      }
      if (e instanceof java.io.IOException) {
        throw (java.io.IOException) e;
      }
      if (e instanceof javax.management.InstanceAlreadyExistsException) {
        throw (javax.management.InstanceAlreadyExistsException) e;
      }
      if (e instanceof javax.management.MBeanException) {
        throw (javax.management.MBeanException) e;
      }
      if (e instanceof javax.management.MBeanRegistrationException) {
        throw (javax.management.MBeanRegistrationException) e;
      }
      if (e instanceof javax.management.NotCompliantMBeanException) {
        throw (javax.management.NotCompliantMBeanException) e;
      }
      if (e instanceof javax.management.ReflectionException) {
        throw (javax.management.ReflectionException) e;
      }
      throw new java.rmi.UnexpectedException("undeclared checked exception", e);
    }
  }
  public javax.management.ObjectInstance createMBean(java.lang.String string, javax.management.ObjectName objectName, javax.management.ObjectName objectName2, java.rmi.MarshalledObject marshalledObject, java.lang.String[] strings, javax.security.auth.Subject subject) throws java.io.IOException, javax.management.InstanceAlreadyExistsException, javax.management.InstanceNotFoundException, javax.management.MBeanException, javax.management.MBeanRegistrationException, javax.management.NotCompliantMBeanException, javax.management.ReflectionException {
    try {
      return (javax.management.ObjectInstance) call("createMBean", new Class<?>[] {java.lang.String.class, javax.management.ObjectName.class, javax.management.ObjectName.class, java.rmi.MarshalledObject.class, java.lang.String[].class, javax.security.auth.Subject.class}, new Object[] {string, objectName, objectName2, marshalledObject, strings, subject});
    } catch (Exception e) {
      if (e instanceof RuntimeException) {
        throw (RuntimeException) e;
      }
      if (e instanceof java.io.IOException) {
        throw (java.io.IOException) e;
      }
      if (e instanceof javax.management.InstanceAlreadyExistsException) {
        throw (javax.management.InstanceAlreadyExistsException) e;
      }
      if (e instanceof javax.management.InstanceNotFoundException) {
        throw (javax.management.InstanceNotFoundException) e;
      }
      if (e instanceof javax.management.MBeanException) {
        throw (javax.management.MBeanException) e;
      }
      if (e instanceof javax.management.MBeanRegistrationException) {
        throw (javax.management.MBeanRegistrationException) e;
      }
      if (e instanceof javax.management.NotCompliantMBeanException) {
        throw (javax.management.NotCompliantMBeanException) e;
      }
      if (e instanceof javax.management.ReflectionException) {
        throw (javax.management.ReflectionException) e;
      }
      throw new java.rmi.UnexpectedException("undeclared checked exception", e);
    }
  }
  public javax.management.ObjectInstance createMBean(java.lang.String string, javax.management.ObjectName objectName, javax.management.ObjectName objectName2, javax.security.auth.Subject subject) throws java.io.IOException, javax.management.InstanceAlreadyExistsException, javax.management.InstanceNotFoundException, javax.management.MBeanException, javax.management.MBeanRegistrationException, javax.management.NotCompliantMBeanException, javax.management.ReflectionException {
    try {
      return (javax.management.ObjectInstance) call("createMBean", new Class<?>[] {java.lang.String.class, javax.management.ObjectName.class, javax.management.ObjectName.class, javax.security.auth.Subject.class}, new Object[] {string, objectName, objectName2, subject});
    } catch (Exception e) {
      if (e instanceof RuntimeException) {
        throw (RuntimeException) e;
      }
      if (e instanceof java.io.IOException) {
        throw (java.io.IOException) e;
      }
      if (e instanceof javax.management.InstanceAlreadyExistsException) {
        throw (javax.management.InstanceAlreadyExistsException) e;
      }
      if (e instanceof javax.management.InstanceNotFoundException) {
        throw (javax.management.InstanceNotFoundException) e;
      }
      if (e instanceof javax.management.MBeanException) {
        throw (javax.management.MBeanException) e;
      }
      if (e instanceof javax.management.MBeanRegistrationException) {
        throw (javax.management.MBeanRegistrationException) e;
      }
      if (e instanceof javax.management.NotCompliantMBeanException) {
        throw (javax.management.NotCompliantMBeanException) e;
      }
      if (e instanceof javax.management.ReflectionException) {
        throw (javax.management.ReflectionException) e;
      }
      throw new java.rmi.UnexpectedException("undeclared checked exception", e);
    }
  }
  public javax.management.ObjectInstance createMBean(java.lang.String string, javax.management.ObjectName objectName, javax.security.auth.Subject subject) throws java.io.IOException, javax.management.InstanceAlreadyExistsException, javax.management.MBeanException, javax.management.MBeanRegistrationException, javax.management.NotCompliantMBeanException, javax.management.ReflectionException {
    try {
      return (javax.management.ObjectInstance) call("createMBean", new Class<?>[] {java.lang.String.class, javax.management.ObjectName.class, javax.security.auth.Subject.class}, new Object[] {string, objectName, subject});
    } catch (Exception e) {
      if (e instanceof RuntimeException) {
        throw (RuntimeException) e;
      }
      if (e instanceof java.io.IOException) {
        throw (java.io.IOException) e;
      }
      if (e instanceof javax.management.InstanceAlreadyExistsException) {
        throw (javax.management.InstanceAlreadyExistsException) e;
      }
      if (e instanceof javax.management.MBeanException) {
        throw (javax.management.MBeanException) e;
      }
      if (e instanceof javax.management.MBeanRegistrationException) {
        throw (javax.management.MBeanRegistrationException) e;
      }
      if (e instanceof javax.management.NotCompliantMBeanException) {
        throw (javax.management.NotCompliantMBeanException) e;
      }
      if (e instanceof javax.management.ReflectionException) {
        throw (javax.management.ReflectionException) e;
      }
      throw new java.rmi.UnexpectedException("undeclared checked exception", e);
    }
  }
  public javax.management.remote.NotificationResult fetchNotifications(long l, int i, long l2) throws java.io.IOException {
    try {
      return (javax.management.remote.NotificationResult) call("fetchNotifications", new Class<?>[] {long.class, int.class, long.class}, new Object[] {l, i, l2});
    } catch (Exception e) {
      if (e instanceof RuntimeException) {
        throw (RuntimeException) e;
      }
      if (e instanceof java.io.IOException) {
        throw (java.io.IOException) e;
      }
      throw new java.rmi.UnexpectedException("undeclared checked exception", e);
    }
  }
  public java.lang.Object getAttribute(javax.management.ObjectName objectName, java.lang.String string, javax.security.auth.Subject subject) throws java.io.IOException, javax.management.AttributeNotFoundException, javax.management.InstanceNotFoundException, javax.management.MBeanException, javax.management.ReflectionException {
    try {
      return (java.lang.Object) call("getAttribute", new Class<?>[] {javax.management.ObjectName.class, java.lang.String.class, javax.security.auth.Subject.class}, new Object[] {objectName, string, subject});
    } catch (Exception e) {
      if (e instanceof RuntimeException) {
        throw (RuntimeException) e;
      }
      if (e instanceof java.io.IOException) {
        throw (java.io.IOException) e;
      }
      if (e instanceof javax.management.AttributeNotFoundException) {
        throw (javax.management.AttributeNotFoundException) e;
      }
      if (e instanceof javax.management.InstanceNotFoundException) {
        throw (javax.management.InstanceNotFoundException) e;
      }
      if (e instanceof javax.management.MBeanException) {
        throw (javax.management.MBeanException) e;
      }
      if (e instanceof javax.management.ReflectionException) {
        throw (javax.management.ReflectionException) e;
      }
      throw new java.rmi.UnexpectedException("undeclared checked exception", e);
    }
  }
  public javax.management.AttributeList getAttributes(javax.management.ObjectName objectName, java.lang.String[] strings, javax.security.auth.Subject subject) throws java.io.IOException, javax.management.InstanceNotFoundException, javax.management.ReflectionException {
    try {
      return (javax.management.AttributeList) call("getAttributes", new Class<?>[] {javax.management.ObjectName.class, java.lang.String[].class, javax.security.auth.Subject.class}, new Object[] {objectName, strings, subject});
    } catch (Exception e) {
      if (e instanceof RuntimeException) {
        throw (RuntimeException) e;
      }
      if (e instanceof java.io.IOException) {
        throw (java.io.IOException) e;
      }
      if (e instanceof javax.management.InstanceNotFoundException) {
        throw (javax.management.InstanceNotFoundException) e;
      }
      if (e instanceof javax.management.ReflectionException) {
        throw (javax.management.ReflectionException) e;
      }
      throw new java.rmi.UnexpectedException("undeclared checked exception", e);
    }
  }
  public java.lang.String getConnectionId() throws java.io.IOException {
    try {
      return (java.lang.String) call("getConnectionId", new Class<?>[] {}, new Object[] {});
    } catch (Exception e) {
      if (e instanceof RuntimeException) {
        throw (RuntimeException) e;
      }
      if (e instanceof java.io.IOException) {
        throw (java.io.IOException) e;
      }
      throw new java.rmi.UnexpectedException("undeclared checked exception", e);
    }
  }
  public java.lang.String getDefaultDomain(javax.security.auth.Subject subject) throws java.io.IOException {
    try {
      return (java.lang.String) call("getDefaultDomain", new Class<?>[] {javax.security.auth.Subject.class}, new Object[] {subject});
    } catch (Exception e) {
      if (e instanceof RuntimeException) {
        throw (RuntimeException) e;
      }
      if (e instanceof java.io.IOException) {
        throw (java.io.IOException) e;
      }
      throw new java.rmi.UnexpectedException("undeclared checked exception", e);
    }
  }
  public java.lang.String[] getDomains(javax.security.auth.Subject subject) throws java.io.IOException {
    try {
      return (java.lang.String[]) call("getDomains", new Class<?>[] {javax.security.auth.Subject.class}, new Object[] {subject});
    } catch (Exception e) {
      if (e instanceof RuntimeException) {
        throw (RuntimeException) e;
      }
      if (e instanceof java.io.IOException) {
        throw (java.io.IOException) e;
      }
      throw new java.rmi.UnexpectedException("undeclared checked exception", e);
    }
  }
  public java.lang.Integer getMBeanCount(javax.security.auth.Subject subject) throws java.io.IOException {
    try {
      return (java.lang.Integer) call("getMBeanCount", new Class<?>[] {javax.security.auth.Subject.class}, new Object[] {subject});
    } catch (Exception e) {
      if (e instanceof RuntimeException) {
        throw (RuntimeException) e;
      }
      if (e instanceof java.io.IOException) {
        throw (java.io.IOException) e;
      }
      throw new java.rmi.UnexpectedException("undeclared checked exception", e);
    }
  }
  public javax.management.MBeanInfo getMBeanInfo(javax.management.ObjectName objectName, javax.security.auth.Subject subject) throws java.io.IOException, javax.management.InstanceNotFoundException, javax.management.IntrospectionException, javax.management.ReflectionException {
    try {
      return (javax.management.MBeanInfo) call("getMBeanInfo", new Class<?>[] {javax.management.ObjectName.class, javax.security.auth.Subject.class}, new Object[] {objectName, subject});
    } catch (Exception e) {
      if (e instanceof RuntimeException) {
        throw (RuntimeException) e;
      }
      if (e instanceof java.io.IOException) {
        throw (java.io.IOException) e;
      }
      if (e instanceof javax.management.InstanceNotFoundException) {
        throw (javax.management.InstanceNotFoundException) e;
      }
      if (e instanceof javax.management.IntrospectionException) {
        throw (javax.management.IntrospectionException) e;
      }
      if (e instanceof javax.management.ReflectionException) {
        throw (javax.management.ReflectionException) e;
      }
      throw new java.rmi.UnexpectedException("undeclared checked exception", e);
    }
  }
  public javax.management.ObjectInstance getObjectInstance(javax.management.ObjectName objectName, javax.security.auth.Subject subject) throws java.io.IOException, javax.management.InstanceNotFoundException {
    try {
      return (javax.management.ObjectInstance) call("getObjectInstance", new Class<?>[] {javax.management.ObjectName.class, javax.security.auth.Subject.class}, new Object[] {objectName, subject});
    } catch (Exception e) {
      if (e instanceof RuntimeException) {
        throw (RuntimeException) e;
      }
      if (e instanceof java.io.IOException) {
        throw (java.io.IOException) e;
      }
      if (e instanceof javax.management.InstanceNotFoundException) {
        throw (javax.management.InstanceNotFoundException) e;
      }
      throw new java.rmi.UnexpectedException("undeclared checked exception", e);
    }
  }
  public java.lang.Object invoke(javax.management.ObjectName objectName, java.lang.String string, java.rmi.MarshalledObject marshalledObject, java.lang.String[] strings, javax.security.auth.Subject subject) throws java.io.IOException, javax.management.InstanceNotFoundException, javax.management.MBeanException, javax.management.ReflectionException {
    try {
      return (java.lang.Object) call("invoke", new Class<?>[] {javax.management.ObjectName.class, java.lang.String.class, java.rmi.MarshalledObject.class, java.lang.String[].class, javax.security.auth.Subject.class}, new Object[] {objectName, string, marshalledObject, strings, subject});
    } catch (Exception e) {
      if (e instanceof RuntimeException) {
        throw (RuntimeException) e;
      }
      if (e instanceof java.io.IOException) {
        throw (java.io.IOException) e;
      }
      if (e instanceof javax.management.InstanceNotFoundException) {
        throw (javax.management.InstanceNotFoundException) e;
      }
      if (e instanceof javax.management.MBeanException) {
        throw (javax.management.MBeanException) e;
      }
      if (e instanceof javax.management.ReflectionException) {
        throw (javax.management.ReflectionException) e;
      }
      throw new java.rmi.UnexpectedException("undeclared checked exception", e);
    }
  }
  public boolean isInstanceOf(javax.management.ObjectName objectName, java.lang.String string, javax.security.auth.Subject subject) throws java.io.IOException, javax.management.InstanceNotFoundException {
    try {
      return ((Boolean) call("isInstanceOf", new Class<?>[] {javax.management.ObjectName.class, java.lang.String.class, javax.security.auth.Subject.class}, new Object[] {objectName, string, subject})).booleanValue();
    } catch (Exception e) {
      if (e instanceof RuntimeException) {
        throw (RuntimeException) e;
      }
      if (e instanceof java.io.IOException) {
        throw (java.io.IOException) e;
      }
      if (e instanceof javax.management.InstanceNotFoundException) {
        throw (javax.management.InstanceNotFoundException) e;
      }
      throw new java.rmi.UnexpectedException("undeclared checked exception", e);
    }
  }
  public boolean isRegistered(javax.management.ObjectName objectName, javax.security.auth.Subject subject) throws java.io.IOException {
    try {
      return ((Boolean) call("isRegistered", new Class<?>[] {javax.management.ObjectName.class, javax.security.auth.Subject.class}, new Object[] {objectName, subject})).booleanValue();
    } catch (Exception e) {
      if (e instanceof RuntimeException) {
        throw (RuntimeException) e;
      }
      if (e instanceof java.io.IOException) {
        throw (java.io.IOException) e;
      }
      throw new java.rmi.UnexpectedException("undeclared checked exception", e);
    }
  }
  public java.util.Set queryMBeans(javax.management.ObjectName objectName, java.rmi.MarshalledObject marshalledObject, javax.security.auth.Subject subject) throws java.io.IOException {
    try {
      return (java.util.Set) call("queryMBeans", new Class<?>[] {javax.management.ObjectName.class, java.rmi.MarshalledObject.class, javax.security.auth.Subject.class}, new Object[] {objectName, marshalledObject, subject});
    } catch (Exception e) {
      if (e instanceof RuntimeException) {
        throw (RuntimeException) e;
      }
      if (e instanceof java.io.IOException) {
        throw (java.io.IOException) e;
      }
      throw new java.rmi.UnexpectedException("undeclared checked exception", e);
    }
  }
  public java.util.Set queryNames(javax.management.ObjectName objectName, java.rmi.MarshalledObject marshalledObject, javax.security.auth.Subject subject) throws java.io.IOException {
    try {
      return (java.util.Set) call("queryNames", new Class<?>[] {javax.management.ObjectName.class, java.rmi.MarshalledObject.class, javax.security.auth.Subject.class}, new Object[] {objectName, marshalledObject, subject});
    } catch (Exception e) {
      if (e instanceof RuntimeException) {
        throw (RuntimeException) e;
      }
      if (e instanceof java.io.IOException) {
        throw (java.io.IOException) e;
      }
      throw new java.rmi.UnexpectedException("undeclared checked exception", e);
    }
  }
  public void removeNotificationListener(javax.management.ObjectName objectName, javax.management.ObjectName objectName2, java.rmi.MarshalledObject marshalledObject, java.rmi.MarshalledObject marshalledObject2, javax.security.auth.Subject subject) throws java.io.IOException, javax.management.InstanceNotFoundException, javax.management.ListenerNotFoundException {
    try {
      call("removeNotificationListener", new Class<?>[] {javax.management.ObjectName.class, javax.management.ObjectName.class, java.rmi.MarshalledObject.class, java.rmi.MarshalledObject.class, javax.security.auth.Subject.class}, new Object[] {objectName, objectName2, marshalledObject, marshalledObject2, subject});
    } catch (Exception e) {
      if (e instanceof RuntimeException) {
        throw (RuntimeException) e;
      }
      if (e instanceof java.io.IOException) {
        throw (java.io.IOException) e;
      }
      if (e instanceof javax.management.InstanceNotFoundException) {
        throw (javax.management.InstanceNotFoundException) e;
      }
      if (e instanceof javax.management.ListenerNotFoundException) {
        throw (javax.management.ListenerNotFoundException) e;
      }
      throw new java.rmi.UnexpectedException("undeclared checked exception", e);
    }
  }
  public void removeNotificationListener(javax.management.ObjectName objectName, javax.management.ObjectName objectName2, javax.security.auth.Subject subject) throws java.io.IOException, javax.management.InstanceNotFoundException, javax.management.ListenerNotFoundException {
    try {
      call("removeNotificationListener", new Class<?>[] {javax.management.ObjectName.class, javax.management.ObjectName.class, javax.security.auth.Subject.class}, new Object[] {objectName, objectName2, subject});
    } catch (Exception e) {
      if (e instanceof RuntimeException) {
        throw (RuntimeException) e;
      }
      if (e instanceof java.io.IOException) {
        throw (java.io.IOException) e;
      }
      if (e instanceof javax.management.InstanceNotFoundException) {
        throw (javax.management.InstanceNotFoundException) e;
      }
      if (e instanceof javax.management.ListenerNotFoundException) {
        throw (javax.management.ListenerNotFoundException) e;
      }
      throw new java.rmi.UnexpectedException("undeclared checked exception", e);
    }
  }
  public void removeNotificationListeners(javax.management.ObjectName objectName, java.lang.Integer[] integers, javax.security.auth.Subject subject) throws java.io.IOException, javax.management.InstanceNotFoundException, javax.management.ListenerNotFoundException {
    try {
      call("removeNotificationListeners", new Class<?>[] {javax.management.ObjectName.class, java.lang.Integer[].class, javax.security.auth.Subject.class}, new Object[] {objectName, integers, subject});
    } catch (Exception e) {
      if (e instanceof RuntimeException) {
        throw (RuntimeException) e;
      }
      if (e instanceof java.io.IOException) {
        throw (java.io.IOException) e;
      }
      if (e instanceof javax.management.InstanceNotFoundException) {
        throw (javax.management.InstanceNotFoundException) e;
      }
      if (e instanceof javax.management.ListenerNotFoundException) {
        throw (javax.management.ListenerNotFoundException) e;
      }
      throw new java.rmi.UnexpectedException("undeclared checked exception", e);
    }
  }
  public void setAttribute(javax.management.ObjectName objectName, java.rmi.MarshalledObject marshalledObject, javax.security.auth.Subject subject) throws java.io.IOException, javax.management.AttributeNotFoundException, javax.management.InstanceNotFoundException, javax.management.InvalidAttributeValueException, javax.management.MBeanException, javax.management.ReflectionException {
    try {
      call("setAttribute", new Class<?>[] {javax.management.ObjectName.class, java.rmi.MarshalledObject.class, javax.security.auth.Subject.class}, new Object[] {objectName, marshalledObject, subject});
    } catch (Exception e) {
      if (e instanceof RuntimeException) {
        throw (RuntimeException) e;
      }
      if (e instanceof java.io.IOException) {
        throw (java.io.IOException) e;
      }
      if (e instanceof javax.management.AttributeNotFoundException) {
        throw (javax.management.AttributeNotFoundException) e;
      }
      if (e instanceof javax.management.InstanceNotFoundException) {
        throw (javax.management.InstanceNotFoundException) e;
      }
      if (e instanceof javax.management.InvalidAttributeValueException) {
        throw (javax.management.InvalidAttributeValueException) e;
      }
      if (e instanceof javax.management.MBeanException) {
        throw (javax.management.MBeanException) e;
      }
      if (e instanceof javax.management.ReflectionException) {
        throw (javax.management.ReflectionException) e;
      }
      throw new java.rmi.UnexpectedException("undeclared checked exception", e);
    }
  }
  public javax.management.AttributeList setAttributes(javax.management.ObjectName objectName, java.rmi.MarshalledObject marshalledObject, javax.security.auth.Subject subject) throws java.io.IOException, javax.management.InstanceNotFoundException, javax.management.ReflectionException {
    try {
      return (javax.management.AttributeList) call("setAttributes", new Class<?>[] {javax.management.ObjectName.class, java.rmi.MarshalledObject.class, javax.security.auth.Subject.class}, new Object[] {objectName, marshalledObject, subject});
    } catch (Exception e) {
      if (e instanceof RuntimeException) {
        throw (RuntimeException) e;
      }
      if (e instanceof java.io.IOException) {
        throw (java.io.IOException) e;
      }
      if (e instanceof javax.management.InstanceNotFoundException) {
        throw (javax.management.InstanceNotFoundException) e;
      }
      if (e instanceof javax.management.ReflectionException) {
        throw (javax.management.ReflectionException) e;
      }
      throw new java.rmi.UnexpectedException("undeclared checked exception", e);
    }
  }
  public void unregisterMBean(javax.management.ObjectName objectName, javax.security.auth.Subject subject) throws java.io.IOException, javax.management.InstanceNotFoundException, javax.management.MBeanRegistrationException {
    try {
      call("unregisterMBean", new Class<?>[] {javax.management.ObjectName.class, javax.security.auth.Subject.class}, new Object[] {objectName, subject});
    } catch (Exception e) {
      if (e instanceof RuntimeException) {
        throw (RuntimeException) e;
      }
      if (e instanceof java.io.IOException) {
        throw (java.io.IOException) e;
      }
      if (e instanceof javax.management.InstanceNotFoundException) {
        throw (javax.management.InstanceNotFoundException) e;
      }
      if (e instanceof javax.management.MBeanRegistrationException) {
        throw (javax.management.MBeanRegistrationException) e;
      }
      throw new java.rmi.UnexpectedException("undeclared checked exception", e);
    }
  }

  private Object call(String name, Class<?>[] types, Object[] args) throws Exception {
    java.lang.reflect.Method m = RMIConnection.class.getMethod(name, types);
    return ref.invoke(this, m, args, sun.rmi.server.Util.computeMethodHash(m));
  }
}
