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

public class PropertyEditorManager {
  private static final Map<Class<?>, Class<?>> registry = new HashMap<>();
  private static String[] searchPath = {"sun.beans.editors"};
  public PropertyEditorManager() {
  }
  public static void registerEditor(java.lang.Class<?> p0, java.lang.Class<?> p1) {
    synchronized (registry) {
      if (p1 == null) {
        registry.remove(p0);
      } else {
        registry.put(p0, p1);
      }
    }
  }
  public static java.beans.PropertyEditor findEditor(java.lang.Class<?> p0) {
    Class<?> c;
    synchronized (registry) {
      c = registry.get(p0);
    }
    if (c != null) {
      try {
        return (PropertyEditor) c.getDeclaredConstructor().newInstance();
      } catch (Exception e) {
        return null;
      }
    }
    PropertyEditor builtin = builtin(p0);
    if (builtin != null) {
      return builtin;
    }
    try {
      Class<?> found = Class.forName(p0.getName() + "Editor", true, p0.getClassLoader());
      return (PropertyEditor) found.getDeclaredConstructor().newInstance();
    } catch (Exception e) {
      return null;
    }
  }
  private static PropertyEditor builtin(Class<?> type) {
    if (type == null) {
      return null;
    }
    if (Enum.class.isAssignableFrom(type) && type != Enum.class) {
      return new com.sun.beans.editors.EnumEditor(type);
    }
    if (type == byte.class || type == Byte.class) {
      return new com.sun.beans.editors.ByteEditor();
    } else if (type == short.class || type == Short.class) {
      return new com.sun.beans.editors.ShortEditor();
    } else if (type == int.class || type == Integer.class) {
      return new com.sun.beans.editors.IntegerEditor();
    } else if (type == long.class || type == Long.class) {
      return new com.sun.beans.editors.LongEditor();
    } else if (type == float.class || type == Float.class) {
      return new com.sun.beans.editors.FloatEditor();
    } else if (type == double.class || type == Double.class) {
      return new com.sun.beans.editors.DoubleEditor();
    } else if (type == boolean.class || type == Boolean.class) {
      return new com.sun.beans.editors.BooleanEditor();
    } else if (type == String.class) {
      return new com.sun.beans.editors.StringEditor();
    }
    return null;
  }

  public static java.lang.String[] getEditorSearchPath() {
    return searchPath.clone();
  }
  public static void setEditorSearchPath(java.lang.String[] p0) {
    searchPath = p0 == null ? new String[0] : p0.clone();
  }
}
