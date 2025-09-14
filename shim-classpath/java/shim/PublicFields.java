/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package shim;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

public final class PublicFields {
  private PublicFields() { }

  public static Field[] get(Class<?> type) {
    ArrayList<Field> fields = new ArrayList<Field>();
    collect(type, fields, new ArrayList<Class<?>>());
    return fields.toArray(new Field[fields.size()]);
  }

  private static void collect(Class<?> type, ArrayList<Field> fields, ArrayList<Class<?>> visited) {
    if (type.isArray() || type.isPrimitive() || visited.contains(type)) return;
    visited.add(type);
    for (Field field : type.getDeclaredFields()) {
      if (Modifier.isPublic(field.getModifiers())) fields.add(field);
    }
    for (Class<?> parent : type.getInterfaces()) collect(parent, fields, visited);
    if (!type.isInterface() && type.getSuperclass() != null) {
      collect(type.getSuperclass(), fields, visited);
    }
  }

  public static Field find(Class<?> type, String name) throws NoSuchFieldException {
    if (name == null) throw new NullPointerException();
    Field field = search(type, name);
    if (field == null) throw new NoSuchFieldException(name);
    return field;
  }

  private static Field search(Class<?> type, String name) {
    if (type.isArray() || type.isPrimitive()) return null;
    for (Field field : type.getDeclaredFields()) {
      if (Modifier.isPublic(field.getModifiers()) && field.getName().equals(name)) return field;
    }
    for (Class<?> parent : type.getInterfaces()) {
      Field field = search(parent, name);
      if (field != null) return field;
    }
    return !type.isInterface() && type.getSuperclass() != null
      ? search(type.getSuperclass(), name) : null;
  }
}
