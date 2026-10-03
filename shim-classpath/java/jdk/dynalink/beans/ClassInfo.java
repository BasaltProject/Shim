/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.dynalink.beans;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

final class ClassInfo {
  private static final ClassValue<ClassInfo> INFO = new ClassValue<ClassInfo>() {
    @Override
    protected ClassInfo computeValue(Class<?> type) {
      return new ClassInfo(type);
    }
  };

  final Class<?> clazz;
  final Map<String, Method> instanceGetters = new LinkedHashMap<>();
  final Map<String, Method> instanceSetters = new LinkedHashMap<>();
  final Map<String, Field> instanceFields = new LinkedHashMap<>();
  final Map<String, Method> staticGetters = new LinkedHashMap<>();
  final Map<String, Method> staticSetters = new LinkedHashMap<>();
  final Map<String, Field> staticFields = new LinkedHashMap<>();
  final Map<String, List<Method>> instanceMethods = new LinkedHashMap<>();
  final Map<String, List<Method>> staticMethods = new LinkedHashMap<>();
  final Map<String, Class<?>> innerClasses = new TreeMap<>();
  final List<Constructor<?>> constructors = new ArrayList<>();

  static ClassInfo of(Class<?> clazz) {
    return INFO.get(clazz);
  }

  private ClassInfo(Class<?> clazz) {
    this.clazz = clazz;
    for (Method m : clazz.getMethods()) {
      final Method acc = accessible(m);
      if (acc == null) {
        continue;
      }
      final boolean isStatic = Modifier.isStatic(m.getModifiers());
      final Map<String, List<Method>> methods = isStatic ? staticMethods : instanceMethods;
      List<Method> list = methods.get(m.getName());
      if (list == null) {
        list = new ArrayList<>();
        methods.put(m.getName(), list);
      }
      list.add(acc);
      final String prop = propertyName(m);
      if (prop != null) {
        final Map<String, Method> target;
        if (m.getName().startsWith("set")) {
          target = isStatic ? staticSetters : instanceSetters;
        } else {
          target = isStatic ? staticGetters : instanceGetters;
        }
        if (!target.containsKey(prop)) {
          target.put(prop, acc);
        }
      }
    }
    for (Field f : clazz.getFields()) {
      if (!isPublicClass(f.getDeclaringClass())) {
        continue;
      }
      if (Modifier.isStatic(f.getModifiers())) {
        staticFields.put(f.getName(), f);
      } else {
        instanceFields.put(f.getName(), f);
      }
    }
    for (Class<?> c : clazz.getClasses()) {
      innerClasses.put(c.getSimpleName(), c);
    }
    for (Constructor<?> c : clazz.getConstructors()) {
      if (isPublicClass(clazz)) {
        constructors.add(c);
      }
    }
  }

  static String propertyName(Method m) {
    final String name = m.getName();
    final int pc = m.getParameterCount();
    if (name.startsWith("get") && name.length() > 3 && pc == 0 && m.getReturnType() != void.class) {
      return decapitalize(name.substring(3));
    }
    if (name.startsWith("is") && name.length() > 2 && pc == 0 && m.getReturnType() == boolean.class) {
      return decapitalize(name.substring(2));
    }
    if (name.startsWith("set") && name.length() > 3 && pc == 1) {
      return decapitalize(name.substring(3));
    }
    return null;
  }

  static String decapitalize(String name) {
    if (name.length() > 1 && Character.isUpperCase(name.charAt(1)) && Character.isUpperCase(name.charAt(0))) {
      return name;
    }
    final char[] chars = name.toCharArray();
    chars[0] = Character.toLowerCase(chars[0]);
    return new String(chars);
  }

  static boolean isPublicClass(Class<?> c) {
    for (Class<?> k = c; k != null; k = k.getDeclaringClass()) {
      if (!Modifier.isPublic(k.getModifiers())) {
        return false;
      }
    }
    return true;
  }

  static Method accessible(Method m) {
    if (isPublicClass(m.getDeclaringClass())) {
      return m;
    }
    final Set<Class<?>> supers = new LinkedHashSet<>();
    collectSupertypes(m.getDeclaringClass(), supers);
    for (Class<?> k : supers) {
      if (isPublicClass(k)) {
        try {
          return k.getMethod(m.getName(), m.getParameterTypes());
        } catch (NoSuchMethodException e) {
          continue;
        }
      }
    }
    return null;
  }

  private static void collectSupertypes(Class<?> c, Set<Class<?>> out) {
    for (Class<?> i : c.getInterfaces()) {
      if (out.add(i)) {
        collectSupertypes(i, out);
      }
    }
    final Class<?> s = c.getSuperclass();
    if (s != null && out.add(s)) {
      collectSupertypes(s, out);
    }
  }

  Set<String> readableInstanceProperties() {
    final Set<String> s = new java.util.LinkedHashSet<>(instanceGetters.keySet());
    s.addAll(instanceFields.keySet());
    return s;
  }

  Set<String> writableInstanceProperties() {
    final Set<String> s = new LinkedHashSet<>(instanceSetters.keySet());
    for (Map.Entry<String, Field> e : instanceFields.entrySet()) {
      if (!Modifier.isFinal(e.getValue().getModifiers())) {
        s.add(e.getKey());
      }
    }
    return s;
  }

  Set<String> readableStaticProperties() {
    final Set<String> s = new java.util.LinkedHashSet<>(staticGetters.keySet());
    s.addAll(staticFields.keySet());
    s.addAll(innerClasses.keySet());
    s.add("class");
    return s;
  }

  Set<String> writableStaticProperties() {
    final Set<String> s = new LinkedHashSet<>(staticSetters.keySet());
    for (Map.Entry<String, Field> e : staticFields.entrySet()) {
      if (!Modifier.isFinal(e.getValue().getModifiers())) {
        s.add(e.getKey());
      }
    }
    return s;
  }
}
