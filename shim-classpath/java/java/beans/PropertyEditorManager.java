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
    try {
      Class<?> found = Class.forName(p0.getName() + "Editor", true, p0.getClassLoader());
      return (PropertyEditor) found.getDeclaredConstructor().newInstance();
    } catch (Exception e) {
      return null;
    }
  }
  public static java.lang.String[] getEditorSearchPath() {
    return searchPath.clone();
  }
  public static void setEditorSearchPath(java.lang.String[] p0) {
    searchPath = p0 == null ? new String[0] : p0.clone();
  }
}
