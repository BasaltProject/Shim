/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package shim;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

public final class PublicMethods {
  private PublicMethods() { }

  public static Method[] get(Class<?> type) {
    return get(type, null, null);
  }

  private static Method[] get(Class<?> type, String name, Class<?>[] parameters) {
    if (type.isPrimitive()) return new Method[0];
    ArrayList<Method> methods = new ArrayList<Method>();
    for (Method method : type.getDeclaredMethods()) {
      if (Modifier.isPublic(method.getModifiers())
          && (name == null || (name.equals(method.getName())
              && sameParameters(parameters, method.getParameterTypes())))) methods.add(method);
    }
    if (!type.isInterface() && type.getSuperclass() != null) {
      for (Method method : get(type.getSuperclass(), name, parameters)) methods.add(method);
    }
    for (Class<?> parent : type.getInterfaces()) {
      for (Method method : get(parent, name, parameters)) {
        if (!Modifier.isStatic(method.getModifiers())) methods.add(method);
      }
    }
    ArrayList<Method> selected = new ArrayList<Method>();
    for (int i = 0; i < methods.size(); ++i) {
      Method method = methods.get(i);
      boolean hidden = false;
      for (int j = 0; j < methods.size(); ++j) {
        if (i == j) continue;
        Method other = methods.get(j);
        if (sameSignature(method, other) && moreSpecific(other, method)
            && (!method.equals(other) || j < i)) {
          hidden = true;
          break;
        }
      }
      if (!hidden) selected.add(method);
    }
    return selected.toArray(new Method[selected.size()]);
  }

  private static boolean sameSignature(Method a, Method b) {
    return a.getName().equals(b.getName())
      && a.getReturnType() == b.getReturnType()
      && sameParameters(a.getParameterTypes(), b.getParameterTypes());
  }

  private static boolean sameParameters(Class<?>[] a, Class<?>[] b) {
    if (a.length != b.length) return false;
    for (int i = 0; i < a.length; ++i) {
      if (a[i] != b[i]) return false;
    }
    return true;
  }

  private static boolean moreSpecific(Method a, Method b) {
    Class<?> owner = a.getDeclaringClass();
    Class<?> parent = b.getDeclaringClass();
    if (owner.isInterface() != parent.isInterface()) return !owner.isInterface();
    return parent.isAssignableFrom(owner);
  }

  public static Method find(Class<?> type, String name, Class<?>[] parameters)
    throws NoSuchMethodException
  {
    if (parameters == null) parameters = new Class<?>[0];
    return find(get(type, name, parameters), name, parameters);
  }

  public static Method find(Method[] methods, String name, Class<?>[] parameters)
    throws NoSuchMethodException
  {
    if (parameters == null) parameters = new Class<?>[0];
    ArrayList<Method> matches = new ArrayList<Method>();
    for (Method method : methods) {
      if (method.getName().equals(name)
          && sameParameters(parameters, method.getParameterTypes())) matches.add(method);
    }
    for (Method method : matches) {
      boolean hidden = false;
      Class<?> result = method.getReturnType();
      for (Method other : matches) {
        Class<?> subtype = other.getReturnType();
        if (result != subtype && result.isAssignableFrom(subtype)) {
          hidden = true;
          break;
        }
      }
      if (!hidden) return method;
    }
    throw new NoSuchMethodException(name);
  }
}
