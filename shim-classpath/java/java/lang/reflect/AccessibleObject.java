/*
 * Based on Avian (https://github.com/ReadyTalk/avian).
 * Original work Copyright (c) 2008-2015, Avian Contributors.
 * Original work licensed under ISC; see LICENSE.txt:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE.txt
 *
 * Modifications Copyright (c) 2026 BasaltMC.
 * Modifications licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: ISC AND MIT
 */

/* Copyright (c) 2008-2015, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.lang.reflect;

import java.lang.annotation.Annotation;

public class AccessibleObject implements AnnotatedElement {
  protected static final int Accessible = 1 << 0;

  // Access and property flags for Member descendants
  protected static final int ACC_VARARGS = 0x0080;
  protected static final int ACC_SYNTHETIC = 0x1000;
  
  public boolean isAnnotationPresent
    (Class<? extends Annotation> class_)
  {
    return getAnnotation(class_) != null;
  }

  private static java.util.Map<AccessibleObject, Boolean> overrides;

  protected AccessibleObject() {
  }

  @Deprecated(since = "9")
  public boolean isAccessible() {
    synchronized (AccessibleObject.class) {
      return overrides != null && overrides.containsKey(this);
    }
  }

  public void setAccessible(boolean v) {
    synchronized (AccessibleObject.class) {
      if (overrides == null) {
        overrides = new java.util.WeakHashMap<AccessibleObject, Boolean>();
      }
      if (v) {
        overrides.put(this, Boolean.TRUE);
      } else {
        overrides.remove(this);
      }
    }
  }

  public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
    throw new AssertionError("All subclasses should override this method");
  }

  public Annotation[] getAnnotations() {
    return getDeclaredAnnotations();
  }

  public Annotation[] getDeclaredAnnotations() {
    throw new AssertionError("All subclasses should override this method");
  }

  public <T extends Annotation> T[] getAnnotationsByType(Class<T> annotationClass) {
    return AnnotatedElement.super.getAnnotationsByType(annotationClass);
  }

  public <T extends Annotation> T getDeclaredAnnotation(Class<T> annotationClass) {
    return AnnotatedElement.super.getDeclaredAnnotation(annotationClass);
  }

  public <T extends Annotation> T[] getDeclaredAnnotationsByType(Class<T> annotationClass) {
    return AnnotatedElement.super.getDeclaredAnnotationsByType(annotationClass);
  }

  public static void setAccessible(AccessibleObject[] array, boolean v) {
    for (AccessibleObject o: array) o.setAccessible(v);
  }

  public boolean canAccess(Object obj) {
    if (isAccessible()) {
      return true;
    }
    if (this instanceof Member) {
      Member m = (Member) this;
      if (!Modifier.isStatic(m.getModifiers())) {
        if (obj == null) {
          throw new IllegalArgumentException("null object for instance member");
        }
        if (!m.getDeclaringClass().isInstance(obj)) {
          throw new IllegalArgumentException("object is not an instance of declaring class");
        }
      }
    }
    return true;
  }

  public boolean trySetAccessible() {
    try {
      setAccessible(true);
    } catch (InaccessibleObjectException e) {
      return false;
    }
    return true;
  }

  private static volatile java.util.Set<String> addedOpens;

  private static java.util.Set<String> addedOpens() {
    java.util.Set<String> set = addedOpens;
    if (set == null) {
      set = new java.util.HashSet<String>();
      String prop = System.getProperty("shim.add-opens");
      if (prop != null) {
        for (String e : prop.split(",")) {
          int slash = e.indexOf('/');
          int eq = e.indexOf('=');
          if (slash > 0) {
            set.add(e.substring(slash + 1, eq > slash ? eq : e.length()));
          }
        }
      }
      addedOpens = set;
    }
    return set;
  }

  static void checkCanSetAccessible(AccessibleObject target, Class<?> declaring, int modifiers) {
    Class<?> caller = null;
    for (Class<?> c : StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
        .walk(f -> f.map(StackWalker.StackFrame::getDeclaringClass).toList())) {
      if (c == AccessibleObject.class || c == Field.class || c == Method.class || c == Constructor.class
          || c == StackWalker.class) {
        continue;
      }
      caller = c;
      break;
    }
    if (caller == null || caller.getClassLoader() == null || declaring.getClassLoader() != null) {
      return;
    }
    String pkg = declaring.getPackageName();
    if (pkg.startsWith("shim") || addedOpens().contains(pkg)) {
      return;
    }
    Module dm = declaring.getModule();
    Module cm = caller.getModule();
    if (dm == cm || dm.isOpen(pkg, cm)) {
      return;
    }
    boolean publicMember = Modifier.isPublic(modifiers) && Modifier.isPublic(declaring.getModifiers());
    if (publicMember && dm.isExported(pkg, cm)) {
      return;
    }
    String kind = target instanceof Field ? "field " : target instanceof Method ? "method " : "constructor ";
    throw new InaccessibleObjectException("Unable to make " + kind + target + " accessible: module "
        + dm.getName() + " does not \"" + (publicMember ? "exports" : "opens") + " " + pkg + "\" to " + cm);
  }

  static void checkInvoke(AccessibleObject target, Class<?> declaring) throws IllegalAccessException {
    if (declaring.getClassLoader() != null || !declaring.getPackageName().startsWith("jdk.internal.")) {
      return;
    }
    Class<?> caller = null;
    for (Class<?> c : StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
        .walk(f -> f.map(StackWalker.StackFrame::getDeclaringClass).toList())) {
      if (c == AccessibleObject.class || c == Field.class || c == Method.class || c == Constructor.class
          || c == StackWalker.class) {
        continue;
      }
      caller = c;
      break;
    }
    if (caller == null || caller.getClassLoader() == null) {
      return;
    }
    String pkg = declaring.getPackageName();
    Module dm = declaring.getModule();
    Module cm = caller.getModule();
    if (dm == cm || addedOpens().contains(pkg) || dm.isExported(pkg, cm)) {
      return;
    }
    throw new IllegalAccessException("class " + caller.getName() + " cannot access class " + declaring.getName()
        + " (in module " + dm.getName() + ") because module " + dm.getName() + " does not export " + pkg + " to "
        + cm);
  }
}
