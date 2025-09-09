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

  @SuppressWarnings("unchecked")
  public default <T extends Annotation> T[] getAnnotationsByType(Class<T> annotationClass) {
    Objects.requireNonNull(annotationClass);
    T direct = getAnnotation(annotationClass);
    if (direct != null) {
      T[] result = (T[]) Array.newInstance(annotationClass, 1);
      result[0] = direct;
      return result;
    }
    return (T[]) Array.newInstance(annotationClass, 0);
  }

  @SuppressWarnings("unchecked")
  public default <T extends Annotation> T[] getDeclaredAnnotationsByType(Class<T> annotationClass) {
    Objects.requireNonNull(annotationClass);
    T direct = getDeclaredAnnotation(annotationClass);
    if (direct != null) {
      T[] result = (T[]) Array.newInstance(annotationClass, 1);
      result[0] = direct;
      return result;
    }
    return (T[]) Array.newInstance(annotationClass, 0);
  }
}
