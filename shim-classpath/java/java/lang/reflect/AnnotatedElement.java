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
import java.util.Objects;

public interface AnnotatedElement {
  public default boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
    return getAnnotation(annotationClass) != null;
  }

  public <T extends Annotation> T getAnnotation(Class<T> annotationClass);

  public Annotation[] getAnnotations();

  public Annotation[] getDeclaredAnnotations();

  @SuppressWarnings("unchecked")
  public default <T extends Annotation> T getDeclaredAnnotation(Class<T> annotationClass) {
    Objects.requireNonNull(annotationClass);
    for (Annotation ann : getDeclaredAnnotations()) {
      if (annotationClass.equals(ann.annotationType())) {
        return (T) ann;
      }
    }
    return null;
  }

  public default <T extends Annotation> T[] getAnnotationsByType(Class<T> annotationClass) {
    Objects.requireNonNull(annotationClass);
    T[] result = getDeclaredAnnotationsByType(annotationClass);
    if (result.length == 0 && this instanceof Class
        && annotationClass.getDeclaredAnnotation(java.lang.annotation.Inherited.class) != null) {
      Class<?> superClass = ((Class<?>) this).getSuperclass();
      if (superClass != null) {
        result = superClass.getAnnotationsByType(annotationClass);
      }
    }
    return result;
  }

  public default <T extends Annotation> T[] getDeclaredAnnotationsByType(Class<T> annotationClass) {
    Objects.requireNonNull(annotationClass);
    return shim.AnnotationSupport.directlyAndIndirectlyPresent(getDeclaredAnnotations(), annotationClass);
  }
}
