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

public class Beans {
  private static boolean designTime;
  private static boolean guiAvailable = false;
  public Beans() {
  }
  public static void setDesignTime(boolean p0) {
    designTime = p0;
  }
  public static boolean isDesignTime() {
    return designTime;
  }
  public static boolean isGuiAvailable() {
    return guiAvailable;
  }
  public static void setGuiAvailable(boolean p0) {
    guiAvailable = p0;
  }
  public static java.lang.Object getInstanceOf(java.lang.Object p0, java.lang.Class<?> p1) {
    return p0;
  }
  public static java.lang.Object instantiate(java.lang.ClassLoader p0, java.lang.String p1) throws java.io.IOException, java.lang.ClassNotFoundException {
    return instantiate(p0, p1, null, null);
  }
  public static java.lang.Object instantiate(java.lang.ClassLoader p0, java.lang.String p1, java.beans.beancontext.BeanContext p2) throws java.io.IOException, java.lang.ClassNotFoundException {
    return instantiate(p0, p1, p2, null);
  }
  public static java.lang.Object instantiate(java.lang.ClassLoader p0, java.lang.String p1, java.beans.beancontext.BeanContext p2, java.beans.AppletInitializer p3) throws java.io.IOException, java.lang.ClassNotFoundException {
    if (p0 == null) {
      p0 = ClassLoader.getSystemClassLoader();
    }
    try {
      Class<?> cls = Class.forName(p1, true, p0);
      return cls.getDeclaredConstructor().newInstance();
    } catch (ClassNotFoundException e) {
      throw e;
    } catch (Exception e) {
      throw new ClassNotFoundException(p1 + ": " + e, e);
    }
  }
  public static boolean isInstanceOf(java.lang.Object p0, java.lang.Class<?> p1) {
    return p1.isInstance(p0);
  }
}
