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
import java.util.HashSet;
import java.util.Set;

public final class PublicMethods {
  private PublicMethods() { }

  public static Method[] get(Class<?> type) {
    return get(type, null, null);
  }

  private static Method[] get(Class<?> type, String name, Class<?>[] parameters) {
    if (type.isPrimitive()) return new Method[0];
    ArrayList<Method> methods = new ArrayList<Method>();
    collect(type, name, parameters, false, new HashSet<Class<?>>(), methods);
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

  private static void collect(Class<?> type, String name, Class<?>[] parameters,
                              boolean inherited, Set<Class<?>> visited, ArrayList<Method> methods) {
    if (!visited.add(type)) return;
    // Visit a shared interface only once. Walking every inheritance path makes
    // ordinary reflection exponential on deep interface diamonds.
    // Public lookup must not resolve private implementation signatures.
    Method[] declared = name == null ? Classes.getMethods(Classes.toVMClass(type), true)
      : Classes.getPublicMethodsNamed(Classes.toVMClass(type), name);
    for (Method method : declared) {
      if (inherited && type.isInterface() && Modifier.isStatic(method.getModifiers())) continue;
      if (Modifier.isPublic(method.getModifiers())
          && (name == null || (name.equals(method.getName())
              && sameParameters(parameters, method.getParameterTypes())))) methods.add(method);
    }
    if (!type.isInterface() && type.getSuperclass() != null) {
      collect(type.getSuperclass(), name, parameters, true, visited, methods);
    }
    for (Class<?> parent : type.getInterfaces()) collect(parent, name, parameters, true, visited, methods);
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
