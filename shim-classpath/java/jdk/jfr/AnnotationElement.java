/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.jfr;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import jdk.jfr.internal.AnnotationProxy;
import jdk.jfr.internal.Types;

public final class AnnotationElement {
  private final String typeName;
  private final Map<String, Object> values;
  private final Class<? extends Annotation> annotationType;
  private final List<AnnotationElement> annotations;
  private final List<ValueDescriptor> descriptors;

  public AnnotationElement(Class<? extends Annotation> annotationType, Map<String, Object> values) {
    java.util.Objects.requireNonNull(annotationType);
    java.util.Objects.requireNonNull(values);
    check(annotationType);
    this.annotationType = annotationType;
    this.typeName = annotationType.getName();
    this.values = new LinkedHashMap<String, Object>();
    this.descriptors = new ArrayList<ValueDescriptor>();
    for (Method m : annotationType.getDeclaredMethods()) {
      if (m.getParameterCount() != 0 || java.lang.reflect.Modifier.isStatic(m.getModifiers())) {
        continue;
      }
      Class<?> rt = m.getReturnType();
      Class<?> vt = rt.isArray() ? rt.getComponentType() : rt;
      Object v = values.get(m.getName());
      if (v == null) {
        Object d = m.getDefaultValue();
        if (d == null) {
          throw new IllegalArgumentException("Annotation element '" + m.getName() + "' for "
              + annotationType.getName() + " is missing");
        }
        v = d;
      }
      this.values.put(m.getName(), normalize(v));
      this.descriptors.add(ValueDescriptor.internal(vt.getName(), m.getName(),
          new ArrayList<AnnotationElement>(), false, new ArrayList<ValueDescriptor>(), rt.isArray()));
    }
    List<AnnotationElement> meta = new ArrayList<AnnotationElement>();
    for (Annotation a : annotationType.getAnnotations()) {
      if (AnnotationProxy.isMetadata(a.annotationType())) {
        meta.add(of(a));
      }
    }
    this.annotations = Collections.unmodifiableList(meta);
  }

  AnnotationElement(String typeName, List<ValueDescriptor> descriptors, Map<String, Object> values,
      List<AnnotationElement> meta) {
    this.typeName = typeName;
    this.annotationType = null;
    this.values = values;
    this.descriptors = descriptors;
    this.annotations = Collections.unmodifiableList(meta);
  }

  public AnnotationElement(Class<? extends Annotation> annotationType, Object value) {
    this(annotationType, Collections.singletonMap("value", value));
  }

  public AnnotationElement(Class<? extends Annotation> annotationType) {
    this(annotationType, Collections.<String, Object>emptyMap());
  }

  private static void check(Class<? extends Annotation> t) {
    if (!t.isAnnotation() || t.getAnnotation(jdk.jfr.MetadataDefinition.class) == null) {
      throw new IllegalArgumentException("Only annotation classes annotated with MetadataDefinition can be used");
    }
  }

  private static Object normalize(Object v) {
    if (v instanceof String[] || v instanceof Object[]) {
      return v;
    }
    return v;
  }

  static AnnotationElement of(Annotation a) {
    Class<? extends Annotation> t = a.annotationType();
    Map<String, Object> vals = new LinkedHashMap<String, Object>();
    for (Method m : t.getDeclaredMethods()) {
      if (m.getParameterCount() == 0 && !java.lang.reflect.Modifier.isStatic(m.getModifiers())) {
        try {
          m.setAccessible(true);
          vals.put(m.getName(), m.invoke(a));
        } catch (ReflectiveOperationException e) {
          throw new IllegalStateException(e);
        }
      }
    }
    return new AnnotationElement(t, vals);
  }

  public List<ValueDescriptor> getValueDescriptors() {
    return Collections.unmodifiableList(descriptors);
  }

  public List<AnnotationElement> getAnnotationElements() {
    return annotations;
  }

  public String getTypeName() {
    return typeName;
  }

  public Object getValue(String name) {
    java.util.Objects.requireNonNull(name);
    if (!values.containsKey(name)) {
      throw new IllegalArgumentException("No value with name '" + name + "'");
    }
    return values.get(name);
  }

  public List<Object> getValues() {
    return new ArrayList<Object>(values.values());
  }

  public boolean hasValue(String name) {
    return values.containsKey(name);
  }

  @SuppressWarnings("unchecked")
  public final <A> A getAnnotation(Class<? extends Annotation> type) {
    java.util.Objects.requireNonNull(type);
    if (typeName.equals(type.getName())) {
      return (A) AnnotationProxy.create((Class<Annotation>) type, this);
    }
    for (AnnotationElement a : annotations) {
      if (a.typeName.equals(type.getName())) {
        return (A) AnnotationProxy.create((Class<Annotation>) type, a);
      }
    }
    return null;
  }

  public long getTypeId() {
    return Types.idOf(typeName);
  }

  public String toString() {
    return "@" + typeName + values;
  }
}
