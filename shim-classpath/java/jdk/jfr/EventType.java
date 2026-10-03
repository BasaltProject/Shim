/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.jfr;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import jdk.jfr.internal.AnnotationProxy;
import jdk.jfr.internal.PrivateAccess;
import jdk.jfr.internal.Types;

public final class EventType {
  private final String name;
  private final long id;
  private final Class<? extends Event> eventClass;
  private final List<ValueDescriptor> fields;
  private final List<AnnotationElement> annotations;
  private final List<SettingDescriptor> settings;

  static {
    PrivateAccess.setPrivateAccess(new PrivateAccess() {
      public EventType newEventType(String name, Class<? extends Event> cls, List<ValueDescriptor> fields,
          List<AnnotationElement> annotations, List<SettingDescriptor> settings) {
        return new EventType(name, cls, fields, annotations, settings);
      }

      public Class<? extends Event> eventClass(EventType type) {
        return type.eventClass;
      }

      public ValueDescriptor newValue(String typeName, String name, List<AnnotationElement> annotations,
          boolean constantPool, List<ValueDescriptor> fields, boolean array) {
        return new ValueDescriptor(typeName, name, annotations, constantPool, fields, array);
      }

      public boolean isConstantPool(ValueDescriptor v) {
        return v.isConstantPool();
      }

      public SettingDescriptor newSetting(String name, String typeName, String defaultValue,
          List<AnnotationElement> annotations) {
        return new SettingDescriptor(name, typeName, defaultValue, annotations);
      }

      public AnnotationElement newAnnotation(String typeName, List<ValueDescriptor> descriptors,
          Map<String, Object> values, List<AnnotationElement> meta) {
        return new AnnotationElement(typeName, descriptors, values, meta);
      }

      public AnnotationElement ofAnnotation(Annotation a) {
        return AnnotationElement.of(a);
      }

      public void writeRecording(java.nio.file.Path dest, List<Row> rows) throws java.io.IOException {
        JfrRuntime.writeRows(dest, rows);
      }

      public byte[] chunkFrom(Recording r, int from, int[] next) {
        return r.chunkFrom(from, next);
      }
    });
  }

  EventType(String name, Class<? extends Event> cls, List<ValueDescriptor> fields,
      List<AnnotationElement> annotations, List<SettingDescriptor> settings) {
    this.name = name;
    this.id = Types.idOf(name);
    this.eventClass = cls;
    this.fields = Collections.unmodifiableList(new ArrayList<ValueDescriptor>(fields));
    this.annotations = Collections.unmodifiableList(new ArrayList<AnnotationElement>(annotations));
    this.settings = Collections.unmodifiableList(new ArrayList<SettingDescriptor>(settings));
  }

  public List<SettingDescriptor> getSettingDescriptors() {
    return settings;
  }

  public String getName() {
    return name;
  }

  public long getId() {
    return id;
  }

  public List<ValueDescriptor> getFields() {
    return fields;
  }

  public ValueDescriptor getField(String name) {
    java.util.Objects.requireNonNull(name);
    for (ValueDescriptor v : fields) {
      if (v.getName().equals(name)) {
        return v;
      }
    }
    return null;
  }

  public String getLabel() {
    AnnotationElement a = find(Label.class.getName());
    return a == null ? null : (String) a.getValue("value");
  }

  public String getDescription() {
    AnnotationElement a = find(Description.class.getName());
    return a == null ? null : (String) a.getValue("value");
  }

  public List<String> getCategoryNames() {
    AnnotationElement a = find(Category.class.getName());
    if (a == null) {
      return Collections.emptyList();
    }
    Object v = a.getValue("value");
    List<String> l = new ArrayList<String>();
    if (v instanceof Object[]) {
      for (Object o : (Object[]) v) {
        l.add(String.valueOf(o));
      }
    } else {
      l.add(String.valueOf(v));
    }
    return Collections.unmodifiableList(l);
  }

  public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
    java.util.Objects.requireNonNull(annotationType);
    AnnotationElement a = find(annotationType.getName());
    return a == null ? null : AnnotationProxy.create(annotationType, a);
  }

  public List<AnnotationElement> getAnnotationElements() {
    return annotations;
  }

  public boolean isEnabled() {
    return JfrRuntime.isEnabledAnywhere(this);
  }

  public static EventType getEventType(Class<? extends Event> eventClass) {
    java.util.Objects.requireNonNull(eventClass);
    return JfrRuntime.infoFor(eventClass, true).type;
  }

  Class<? extends Event> eventClass() {
    return eventClass;
  }

  AnnotationElement find(String type) {
    for (AnnotationElement a : annotations) {
      if (a.getTypeName().equals(type)) {
        return a;
      }
    }
    return null;
  }

  public String toString() {
    return name;
  }
}
