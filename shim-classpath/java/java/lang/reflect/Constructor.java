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

public class Constructor<T> extends Executable {
  public final Method<T> method;

  public Constructor(Method<T> method) {
    this.method = method;
  }

  public boolean equals(Object o) {
    return o instanceof Constructor
      && ((Constructor) o).method.equals(method);
  }

  public boolean isAccessible() {
    return method.isAccessible();
  }

  public void setAccessible(boolean v) {
    method.setAccessible(v);
  }

  public Class<T> getDeclaringClass() {
    return method.getDeclaringClass();
  }

  public Class[] getParameterTypes() {
    return method.getParameterTypes();
  }

  public int getModifiers() {
    return method.getModifiers();
  }

  public boolean isSynthetic() {
    return method.isSynthetic();
  }

  public String getName() {
    return getDeclaringClass().getName();
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    int mod = getModifiers() & Modifier.constructorModifiers();
    if (mod != 0) {
      sb.append(Modifier.toString(mod)).append(' ');
    }
    sb.append(getDeclaringClass().getName()).append('(');
    Class<?>[] params = getParameterTypes();
    for (int i = 0; i < params.length; i++) {
      if (i > 0) sb.append(',');
      sb.append(params[i].getName());
    }
    sb.append(')');
    Class<?>[] exceptions = getExceptionTypes();
    if (exceptions.length > 0) {
      sb.append(" throws ");
      for (int i = 0; i < exceptions.length; i++) {
        if (i > 0) sb.append(',');
        sb.append(exceptions[i].getName());
      }
    }
    return sb.toString();
  }

  public <T extends Annotation> T getAnnotation(Class<T> class_) {
    return method.getAnnotation(class_);
  }

  public Annotation[] getAnnotations() {
    return method.getAnnotations();
  }

  public Annotation[] getDeclaredAnnotations() {
    return method.getDeclaredAnnotations();
  }

  private static native Object make(shim.VMClass c);

  public T newInstance(Object ... arguments)
    throws InvocationTargetException, InstantiationException,
    IllegalAccessException
  {
    T v = (T) make(method.getDeclaringClass().vmClass);
    method.invoke(v, arguments);
    return v;
  }

  public Class<?>[] getExceptionTypes() {
    return method.getExceptionTypes();
  }

  public Annotation[][] getParameterAnnotations() {
    return method.getParameterAnnotations();
  }

  public String toGenericString() {
    StringBuilder sb = new StringBuilder();
    int mod = getModifiers() & Modifier.constructorModifiers();
    if (mod != 0) {
      sb.append(Modifier.toString(mod)).append(' ');
    }
    sb.append(getDeclaringClass().getTypeName()).append('(');
    Class<?>[] params = getParameterTypes();
    for (int i = 0; i < params.length; i++) {
      if (i > 0) sb.append(',');
      sb.append(params[i].getTypeName());
    }
    sb.append(')');
    Class<?>[] exceptions = getExceptionTypes();
    if (exceptions.length > 0) {
      sb.append(" throws ");
      for (int i = 0; i < exceptions.length; i++) {
        if (i > 0) sb.append(',');
        sb.append(exceptions[i].getTypeName());
      }
    }
    return sb.toString();
  }
}
