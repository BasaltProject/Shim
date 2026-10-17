/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class MBeanServerInvocationHandler implements InvocationHandler {
  private final MBeanServerConnection connection;
  private final ObjectName objectName;
  private final boolean isMXBean;

  public MBeanServerInvocationHandler(MBeanServerConnection connection, ObjectName objectName) {
    this(connection, objectName, false);
  }

  public MBeanServerInvocationHandler(MBeanServerConnection connection, ObjectName objectName, boolean isMXBean) {
    if (connection == null) {
      throw new IllegalArgumentException("Null connection");
    }
    if (Proxy.isProxyClass(connection.getClass())) {
      if (MBeanServerInvocationHandler.class.isAssignableFrom(Proxy.getInvocationHandler(connection).getClass())) {
        throw new IllegalArgumentException("Wrapping MBeanServerConnection around MBeanServerConnection "
            + "proxy");
      }
    }
    if (objectName == null) {
      throw new IllegalArgumentException("Null object name");
    }
    this.connection = connection;
    this.objectName = objectName;
    this.isMXBean = isMXBean;
  }

  public MBeanServerConnection getMBeanServerConnection() {
    return connection;
  }

  public ObjectName getObjectName() {
    return objectName;
  }

  public boolean isMXBean() {
    return isMXBean;
  }

  public static <T> T newProxyInstance(MBeanServerConnection connection, ObjectName objectName,
      Class<T> interfaceClass, boolean notificationBroadcaster) {
    return JMX.newMBeanProxy(connection, objectName, interfaceClass, notificationBroadcaster);
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    Class<?> methodClass = method.getDeclaringClass();
    if (methodClass.equals(NotificationBroadcaster.class) || methodClass.equals(NotificationEmitter.class)) {
      return invokeBroadcasterMethod(proxy, method, args);
    }
    if (shouldDoLocally(proxy, method)) {
      return doLocally(proxy, method, args);
    }
    try {
      return doRemote(method, args);
    } catch (MBeanException e) {
      throw e.getTargetException();
    } catch (RuntimeMBeanException e) {
      throw e.getTargetException();
    } catch (RuntimeErrorException e) {
      throw e.getTargetError();
    }
  }

  private Object doRemote(Method method, Object[] args) throws Throwable {
    String methodName = method.getName();
    Class<?>[] paramTypes = method.getParameterTypes();
    Class<?> returnType = method.getReturnType();
    if (args == null) {
      args = new Object[0];
    }
    if (methodName.startsWith("get") && methodName.length() > 3 && paramTypes.length == 0
        && returnType != Void.TYPE) {
      return convert(connection.getAttribute(objectName, methodName.substring(3)), returnType);
    }
    if (methodName.startsWith("is") && methodName.length() > 2 && paramTypes.length == 0
        && (returnType == Boolean.TYPE || returnType == Boolean.class)) {
      return connection.getAttribute(objectName, methodName.substring(2));
    }
    if (methodName.startsWith("set") && methodName.length() > 3 && paramTypes.length == 1
        && returnType == Void.TYPE) {
      connection.setAttribute(objectName, new Attribute(methodName.substring(3), args[0]));
      return null;
    }
    String[] signature = new String[paramTypes.length];
    for (int i = 0; i < paramTypes.length; i++) {
      signature[i] = paramTypes[i].getName();
    }
    return convert(connection.invoke(objectName, methodName, args, signature), returnType);
  }

  private Object convert(Object value, Class<?> returnType) {
    return isMXBean ? shim.management.OpenMapper.fromOpen(value, returnType) : value;
  }

  private boolean shouldDoLocally(Object proxy, Method method) {
    String name = method.getName();
    if (name.equals("hashCode") || name.equals("toString")) {
      return method.getParameterTypes().length == 0;
    }
    if (name.equals("equals")) {
      return method.getParameterTypes().length == 1 && method.getParameterTypes()[0] == Object.class;
    }
    return false;
  }

  private Object doLocally(Object proxy, Method method, Object[] args) {
    String name = method.getName();
    if (name.equals("equals")) {
      if (this == args[0]) {
        return true;
      }
      if (!(args[0] instanceof Proxy)) {
        return false;
      }
      InvocationHandler ihandler = Proxy.getInvocationHandler(args[0]);
      if (ihandler == null || !(ihandler instanceof MBeanServerInvocationHandler)) {
        return false;
      }
      MBeanServerInvocationHandler handler = (MBeanServerInvocationHandler) ihandler;
      return connection.equals(handler.connection) && objectName.equals(handler.objectName)
          && proxy.getClass().equals(args[0].getClass());
    }
    if (name.equals("toString")) {
      return (isMXBean ? "MXBean" : "MBean") + "Proxy(" + connection + "[" + objectName + "])";
    }
    return connection.hashCode() ^ objectName.hashCode();
  }

  private Object invokeBroadcasterMethod(Object proxy, Method method, Object[] args) throws Exception {
    String methodName = method.getName();
    int nargs = args == null ? 0 : args.length;
    if (methodName.equals("addNotificationListener")) {
      NotificationListener listener = (NotificationListener) args[0];
      NotificationFilter filter = nargs > 1 ? (NotificationFilter) args[1] : null;
      Object handback = nargs > 2 ? args[2] : null;
      connection.addNotificationListener(objectName, listener, filter, handback);
      return null;
    } else if (methodName.equals("removeNotificationListener")) {
      NotificationListener listener = (NotificationListener) args[0];
      if (nargs == 1) {
        connection.removeNotificationListener(objectName, listener);
      } else {
        connection.removeNotificationListener(objectName, listener, (NotificationFilter) args[1], args[2]);
      }
      return null;
    } else if (methodName.equals("getNotificationInfo")) {
      return connection.getMBeanInfo(objectName).getNotifications();
    }
    throw new IllegalArgumentException("Bad method: " + methodName);
  }
}
