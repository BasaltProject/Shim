/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.lang.reflect;

import java.lang.annotation.Annotation;
import java.util.Objects;

public final class RecordComponent implements AnnotatedElement {
  private final Class<?> declaringRecord;
  private final String name;
  private final Class<?> type;
  private final Method<?> accessor;

  public RecordComponent(Class<?> declaringRecord, String name, Class<?> type, Method<?> accessor) {
    this.declaringRecord = Objects.requireNonNull(declaringRecord);
    this.name = Objects.requireNonNull(name);
    this.type = Objects.requireNonNull(type);
    this.accessor = accessor;
  }

  public String getName() {
    return name;
  }

  public Class<?> getType() {
    return type;
  }

  public Class<?> getDeclaringRecord() {
    return declaringRecord;
  }

  public Method<?> getAccessor() {
    return accessor;
  }

  public String getGenericSignature() {
    return null;
  }

  public Type getGenericType() {
    return type;
  }

  @Override
  public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
    return getAnnotation(annotationClass) != null;
  }

  @Override
  public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
    if (accessor != null) {
      return accessor.getAnnotation(annotationClass);
    }
    return null;
  }

  @Override
  public Annotation[] getAnnotations() {
    if (accessor != null) {
      return accessor.getAnnotations();
    }
    return new Annotation[0];
  }

  @Override
  public Annotation[] getDeclaredAnnotations() {
    return getAnnotations();
  }

  @Override
  public String toString() {
    return type.getTypeName() + " " + name;
  }
}
