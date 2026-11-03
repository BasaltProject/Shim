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

import java.util.*;
import java.lang.reflect.*;

public class EventHandler implements java.lang.reflect.InvocationHandler {
  private Object target;
  private String action;
  private final String eventPropertyName;
  private final String listenerMethodName;
  public EventHandler(java.lang.Object p0, java.lang.String p1, java.lang.String p2, java.lang.String p3) {
    this.target = p0;
    this.action = p1;
    this.eventPropertyName = p2;
    this.listenerMethodName = p3;
  }
  public java.lang.Object invoke(java.lang.Object proxy, java.lang.reflect.Method method, java.lang.Object[] arguments) {
    String methodName = method.getName();
    if (method.getDeclaringClass() == Object.class) {
      if (methodName.equals("hashCode")) {
        return Integer.valueOf(System.identityHashCode(proxy));
      } else if (methodName.equals("equals")) {
        return (proxy == arguments[0] ? Boolean.TRUE : Boolean.FALSE);
      } else if (methodName.equals("toString")) {
        return proxy.getClass().getName() + '@' + Integer.toHexString(proxy.hashCode());
      }
    }
    if (listenerMethodName == null || listenerMethodName.equals(methodName)) {
      try {
        Object[] args = new Object[0];
        Object value = arguments != null && arguments.length > 0 ? arguments[0] : null;
        if (eventPropertyName != null && value != null) {
          value = new Expression(value, "get" + Character.toUpperCase(eventPropertyName.charAt(0))
              + eventPropertyName.substring(1), new Object[0]).getValue();
        }
        if (eventPropertyName != null) {
          args = new Object[] {value};
        } else if (arguments != null && arguments.length > 0) {
          args = new Object[] {arguments[0]};
        }
        try {
          return new Statement(target, action, args).execute2();
        } catch (NoSuchMethodException e) {
          return new Statement(target, action, new Object[0]).execute2();
        }
      } catch (RuntimeException e) {
        throw e;
      } catch (java.lang.reflect.InvocationTargetException e) {
        Throwable th = e.getTargetException();
        if (th instanceof RuntimeException) {
          throw (RuntimeException) th;
        }
        throw new RuntimeException(th);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    return null;
  }
  public static <T> T create(java.lang.Class<T> p0, java.lang.Object p1, java.lang.String p2, java.lang.String p3) {
    return create(p0, p1, p2, p3, null);
  }
  public static <T> T create(java.lang.Class<T> p0, java.lang.Object p1, java.lang.String p2, java.lang.String p3, java.lang.String p4) {
    if (p1 == null) {
      throw new NullPointerException("target must be non-null");
    }
    if (p2 == null) {
      throw new NullPointerException("action must be non-null");
    }
    if (p0 == null) {
      throw new NullPointerException("listenerInterface must be non-null");
    }
    EventHandler handler = new EventHandler(p1, p2, p3, p4);
    return (T) Proxy.newProxyInstance(p1.getClass().getClassLoader(), new Class<?>[] {p0}, handler);
  }
  public static <T> T create(java.lang.Class<T> p0, java.lang.Object p1, java.lang.String p2) {
    return create(p0, p1, p2, null, null);
  }
  public java.lang.Object getTarget() {
    return target;
  }
  public java.lang.String getEventPropertyName() {
    return eventPropertyName;
  }
  public java.lang.String getAction() {
    return action;
  }
  public java.lang.String getListenerMethodName() {
    return listenerMethodName;
  }
}
