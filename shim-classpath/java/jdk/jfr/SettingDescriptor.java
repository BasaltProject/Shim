/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.jfr;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;
import jdk.jfr.internal.AnnotationProxy;
import jdk.jfr.internal.Types;

public final class SettingDescriptor {
  private final String name;
  private final String typeName;
  private final String defaultValue;
  private final List<AnnotationElement> annotations;

  SettingDescriptor(String name, String typeName, String defaultValue, List<AnnotationElement> annotations) {
    this.name = name;
    this.typeName = typeName;
    this.defaultValue = defaultValue;
    this.annotations = Collections.unmodifiableList(annotations);
  }

  public String getName() {
    return name;
  }

  public String getLabel() {
    AnnotationElement a = find(Label.class.getName());
    return a == null ? null : (String) a.getValue("value");
  }

  public String getDescription() {
    AnnotationElement a = find(Description.class.getName());
    return a == null ? null : (String) a.getValue("value");
  }

  public String getTypeName() {
    return typeName;
  }

  public String getContentType() {
    for (AnnotationElement a : annotations) {
      for (AnnotationElement m : a.getAnnotationElements()) {
        if (m.getTypeName().equals(ContentType.class.getName())) {
          return a.getTypeName();
        }
      }
    }
    return null;
  }

  public long getTypeId() {
    return Types.idOf(typeName);
  }

  public String getDefaultValue() {
    return defaultValue;
  }

  public List<AnnotationElement> getAnnotationElements() {
    return annotations;
  }

  @SuppressWarnings("unchecked")
  public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
    AnnotationElement a = find(annotationType.getName());
    return a == null ? null : AnnotationProxy.create(annotationType, a);
  }

  private AnnotationElement find(String type) {
    for (AnnotationElement a : annotations) {
      if (a.getTypeName().equals(type)) {
        return a;
      }
    }
    return null;
  }

  public String toString() {
    return name + ":" + typeName;
  }
}
