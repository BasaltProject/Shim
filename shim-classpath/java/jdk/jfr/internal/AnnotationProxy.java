/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.jfr.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;
import jdk.jfr.AnnotationElement;

public final class AnnotationProxy {
  private AnnotationProxy() {
  }

  @SuppressWarnings("unchecked")
  public static <A extends Annotation> A create(final Class<A> type, final AnnotationElement element) {
    return (A) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] {type}, new InvocationHandler() {
      public Object invoke(Object proxy, Method m, Object[] args) throws Throwable {
        String n = m.getName();
        if (m.getParameterCount() == 0) {
          if (n.equals("annotationType")) {
            return type;
          }
          if (n.equals("toString")) {
            return "@" + type.getName() + element.getValues();
          }
          if (n.equals("hashCode")) {
            return element.getValues().hashCode();
          }
          if (element.hasValue(n)) {
            return coerce(element.getValue(n), m.getReturnType());
          }
          Object d = m.getDefaultValue();
          if (d != null) {
            return d;
          }
        } else if (n.equals("equals")) {
          return proxy == args[0];
        }
        throw new IllegalStateException("No value for " + n);
      }
    });
  }

  private static Object coerce(Object v, Class<?> rt) {
    if (rt.isArray() && v instanceof Object[] == false && !v.getClass().isArray()) {
      Object arr = java.lang.reflect.Array.newInstance(rt.getComponentType(), 1);
      java.lang.reflect.Array.set(arr, 0, v);
      return arr;
    }
    if (rt.isArray() && v instanceof Object[]) {
      Object[] src = (Object[]) v;
      Object arr = java.lang.reflect.Array.newInstance(rt.getComponentType(), src.length);
      for (int i = 0; i < src.length; i++) {
        java.lang.reflect.Array.set(arr, i, src[i]);
      }
      return arr;
    }
    return v;
  }

  public static boolean isMetadata(Class<? extends Annotation> a) {
    return a.getAnnotation(jdk.jfr.MetadataDefinition.class) != null;
  }

  public static List<String> noNames() {
    return Arrays.asList(new String[0]);
  }
}
