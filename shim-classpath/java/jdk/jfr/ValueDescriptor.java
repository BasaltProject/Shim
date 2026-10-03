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
import jdk.jfr.internal.AnnotationProxy;
import jdk.jfr.internal.Types;

public final class ValueDescriptor {
  private final String name;
  private final String typeName;
  private final boolean array;
  private final List<AnnotationElement> annotations;
  private final List<ValueDescriptor> fields;
  private final boolean constantPool;

  public ValueDescriptor(Class<?> type, String name, List<AnnotationElement> annotations) {
    this(type, name, annotations, false);
  }

  public ValueDescriptor(Class<?> type, String name) {
    this(type, name, Collections.<AnnotationElement>emptyList(), false);
  }

  ValueDescriptor(Class<?> type, String name, List<AnnotationElement> annotations, boolean ignored) {
    java.util.Objects.requireNonNull(type, "type");
    java.util.Objects.requireNonNull(name, "name");
    java.util.Objects.requireNonNull(annotations, "annotations");
    if (type.isArray()) {
      throw new IllegalArgumentException("Array types are not supported");
    }
    if (type == void.class || !(type.isPrimitive() || type == String.class || type == Class.class
        || type == Thread.class)) {
      throw new IllegalArgumentException("Only primitive types, java.lang.Thread, java.lang.String and java.lang.Class "
          + "are allowed");
    }
    if (!isIdentifier(name)) {
      throw new IllegalArgumentException("'" + name + "' is not a valid Java identifier");
    }
    this.name = name;
    this.typeName = type.getName();
    this.array = false;
    this.constantPool = type == Thread.class || type == Class.class;
    this.annotations = Collections.unmodifiableList(new ArrayList<AnnotationElement>(annotations));
    this.fields = Collections.unmodifiableList(type == Thread.class ? threadFields() : new ArrayList<ValueDescriptor>());
  }

  ValueDescriptor(String typeName, String name, List<AnnotationElement> annotations, boolean cp,
      List<ValueDescriptor> fields, boolean array) {
    this.typeName = typeName;
    this.name = name;
    this.annotations = Collections.unmodifiableList(annotations);
    this.constantPool = cp;
    this.fields = Collections.unmodifiableList(fields);
    this.array = array;
  }

  static ValueDescriptor internal(String typeName, String name, List<AnnotationElement> annotations, boolean cp,
      List<ValueDescriptor> fields, boolean array) {
    return new ValueDescriptor(typeName, name, annotations, cp, fields, array);
  }

  private static List<ValueDescriptor> threadFields() {
    List<ValueDescriptor> l = new ArrayList<ValueDescriptor>();
    l.add(internal("java.lang.String", "osName", label("OS Thread Name"), false, new ArrayList<ValueDescriptor>(), false));
    l.add(internal("long", "osThreadId", label("OS Thread Id"), false, new ArrayList<ValueDescriptor>(), false));
    l.add(internal("java.lang.String", "javaName", label("Java Thread Name"), false, new ArrayList<ValueDescriptor>(), false));
    l.add(internal("long", "javaThreadId", label("Java Thread Id"), false, new ArrayList<ValueDescriptor>(), false));
    List<ValueDescriptor> gf = new ArrayList<ValueDescriptor>();
    gf.add(internal("jdk.types.ThreadGroup", "parent", label("Parent"), true, new ArrayList<ValueDescriptor>(), false));
    gf.add(internal("java.lang.String", "name", label("Name"), false, new ArrayList<ValueDescriptor>(), false));
    l.add(internal("jdk.types.ThreadGroup", "group", label("Java Thread Group"), true, gf, false));
    l.add(internal("boolean", "virtual", label("Virtual Thread"), false, new ArrayList<ValueDescriptor>(), false));
    return l;
  }

  private static List<AnnotationElement> label(String text) {
    List<AnnotationElement> l = new ArrayList<AnnotationElement>();
    l.add(new AnnotationElement(Label.class, text));
    return l;
  }

  private static boolean isIdentifier(String n) {
    if (n.isEmpty() || !Character.isJavaIdentifierStart(n.charAt(0))) {
      return false;
    }
    for (int i = 1; i < n.length(); i++) {
      if (!Character.isJavaIdentifierPart(n.charAt(i))) {
        return false;
      }
    }
    String[] kw = {"abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class", "const",
        "continue", "default", "do", "double", "else", "enum", "extends", "final", "finally", "float", "for", "goto",
        "if", "implements", "import", "instanceof", "int", "interface", "long", "native", "new", "package",
        "private", "protected", "public", "return", "short", "static", "strictfp", "super", "switch",
        "synchronized", "this", "throw", "throws", "transient", "try", "void", "volatile", "while", "true", "false",
        "null"};
    for (String k : kw) {
      if (k.equals(n)) {
        return false;
      }
    }
    return true;
  }

  public String getLabel() {
    AnnotationElement a = find(Label.class.getName());
    return a == null ? null : (String) a.getValue("value");
  }

  public String getName() {
    return name;
  }

  public String getTypeName() {
    return typeName;
  }

  public String getDescription() {
    AnnotationElement a = find(Description.class.getName());
    return a == null ? null : (String) a.getValue("value");
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

  public boolean isArray() {
    return array;
  }

  public List<ValueDescriptor> getFields() {
    return fields;
  }

  public List<AnnotationElement> getAnnotationElements() {
    return annotations;
  }

  @SuppressWarnings("unchecked")
  public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
    java.util.Objects.requireNonNull(annotationType);
    AnnotationElement a = find(annotationType.getName());
    return a == null ? null : AnnotationProxy.create(annotationType, a);
  }

  boolean isConstantPool() {
    return constantPool;
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
