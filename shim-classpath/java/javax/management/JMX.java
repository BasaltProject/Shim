/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management;

import java.lang.reflect.Proxy;

public class JMX {
  public static final String DEFAULT_VALUE_FIELD = "defaultValue";
  public static final String IMMUTABLE_INFO_FIELD = "immutableInfo";
  public static final String INTERFACE_CLASS_NAME_FIELD = "interfaceClassName";
  public static final String LEGAL_VALUES_FIELD = "legalValues";
  public static final String MAX_VALUE_FIELD = "maxValue";
  public static final String MIN_VALUE_FIELD = "minValue";
  public static final String MXBEAN_FIELD = "mxbean";
  public static final String OPEN_TYPE_FIELD = "openType";
  public static final String ORIGINAL_TYPE_FIELD = "originalType";

  private JMX() {
  }

  public static <T> T newMBeanProxy(MBeanServerConnection connection, ObjectName objectName, Class<T> interfaceClass) {
    return newMBeanProxy(connection, objectName, interfaceClass, false);
  }

  public static <T> T newMBeanProxy(MBeanServerConnection connection, ObjectName objectName, Class<T> interfaceClass,
      boolean notificationEmitter) {
    return createProxy(connection, objectName, interfaceClass, notificationEmitter, false);
  }

  public static <T> T newMXBeanProxy(MBeanServerConnection connection, ObjectName objectName,
      Class<T> interfaceClass) {
    return newMXBeanProxy(connection, objectName, interfaceClass, false);
  }

  public static <T> T newMXBeanProxy(MBeanServerConnection connection, ObjectName objectName,
      Class<T> interfaceClass, boolean notificationEmitter) {
    return createProxy(connection, objectName, interfaceClass, notificationEmitter, true);
  }

  public static boolean isMXBeanInterface(Class<?> interfaceClass) {
    if (!interfaceClass.isInterface()) {
      return false;
    }
    if (!java.lang.reflect.Modifier.isPublic(interfaceClass.getModifiers())) {
      return false;
    }
    MXBean a = interfaceClass.getAnnotation(MXBean.class);
    if (a != null) {
      return a.value();
    }
    return interfaceClass.getName().endsWith("MXBean");
  }

  private static <T> T createProxy(MBeanServerConnection connection, ObjectName objectName, Class<T> interfaceClass,
      boolean notificationEmitter, boolean mxbean) {
    try {
      Class<?>[] interfaces = notificationEmitter ? new Class<?>[] {interfaceClass, NotificationEmitter.class}
          : new Class<?>[] {interfaceClass};
      MBeanServerInvocationHandler handler = new MBeanServerInvocationHandler(connection, objectName, mxbean);
      Object proxy = Proxy.newProxyInstance(interfaceClass.getClassLoader(), interfaces, handler);
      return interfaceClass.cast(proxy);
    } catch (RuntimeException e) {
      throw new IllegalArgumentException(e.getMessage(), e);
    }
  }
}
