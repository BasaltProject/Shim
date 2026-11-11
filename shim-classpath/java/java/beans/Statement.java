/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.beans;

import java.util.*;
import java.lang.reflect.*;

public class Statement {
  static final ExceptionListener defaultExceptionListener = new ExceptionListener() {
    public void exceptionThrown(Exception e) {
      System.err.println(e);
      System.err.println("Continuing ...");
    }
  };
  private final Object target;
  private final String methodName;
  private final Object[] arguments;
  public Statement(java.lang.Object p0, java.lang.String p1, java.lang.Object[] p2) {
    this.target = p0;
    this.methodName = p1;
    this.arguments = (p2 == null) ? new Object[0] : p2.clone();
  }
  public java.lang.String toString() {
    StringBuilder sb = new StringBuilder();
    String unrecognized = "<unrecognized>";
    Object t = getTarget();
    String n = instanceName(t);
    sb.append(n).append(".").append(getMethodName()).append("(");
    Object[] args = getArguments();
    for (int i = 0; i < args.length; i++) {
      if (i > 0) {
        sb.append(", ");
      }
      sb.append(instanceName(args[i]));
    }
    sb.append(");");
    return sb.toString();
  }
  static String instanceName(Object o) {
    if (o == null) {
      return "null";
    }
    if (o.getClass() == String.class) {
      return "\"" + o + "\"";
    }
    return o.getClass().getSimpleName();
  }
  public void execute() throws java.lang.Exception {
    invoke();
  }
  public java.lang.Object getTarget() {
    return target;
  }
  public java.lang.String getMethodName() {
    return methodName;
  }
  public java.lang.Object[] getArguments() {
    return arguments.clone();
  }

  Object execute2() throws Exception {
    return invoke();
  }

  Object invoke() throws Exception {
    Object t = target;
    if (t == null || methodName == null) {
      throw new NullPointerException((t == null ? "target" : "methodName") + " should not be null");
    }
    Object[] args = arguments;
    if (t instanceof Class && "new".equals(methodName)) {
      Class<?> cls = (Class<?>) t;
      for (Constructor<?> c : cls.getConstructors()) {
        if (matches(c.getParameterTypes(), args)) {
          return c.newInstance(args);
        }
      }
      throw new NoSuchMethodException(toString());
    }
    Class<?> type = t instanceof Class ? (Class<?>) t : t.getClass();
    for (Method m : type.getMethods()) {
      if (m.getName().equals(methodName) && matches(m.getParameterTypes(), args)) {
        return m.invoke(t instanceof Class && !Modifier.isStatic(m.getModifiers()) ? t : (Modifier.isStatic(m.getModifiers()) ? null : t), args);
      }
    }
    if (t instanceof Class) {
      for (Method m : t.getClass().getMethods()) {
        if (m.getName().equals(methodName) && matches(m.getParameterTypes(), args)) {
          return m.invoke(t, args);
        }
      }
    }
    throw new NoSuchMethodException(toString());
  }

  private static boolean matches(Class<?>[] params, Object[] args) {
    if (params.length != args.length) {
      return false;
    }
    for (int i = 0; i < params.length; i++) {
      Class<?> p = params[i];
      if (args[i] == null) {
        if (p.isPrimitive()) {
          return false;
        }
        continue;
      }
      Class<?> a = args[i].getClass();
      if (p.isPrimitive()) {
        if (!(p == int.class && a == Integer.class || p == long.class && a == Long.class
            || p == boolean.class && a == Boolean.class || p == double.class && a == Double.class
            || p == float.class && a == Float.class || p == char.class && a == Character.class
            || p == short.class && a == Short.class || p == byte.class && a == Byte.class)) {
          return false;
        }
      } else if (!p.isAssignableFrom(a)) {
        return false;
      }
    }
    return true;
  }
}
