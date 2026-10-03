/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.jfr.internal;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import jdk.jfr.AnnotationElement;
import jdk.jfr.Event;
import jdk.jfr.EventType;
import jdk.jfr.SettingDescriptor;
import jdk.jfr.ValueDescriptor;

public abstract class PrivateAccess {
  private static volatile PrivateAccess instance;

  public static void setPrivateAccess(PrivateAccess pa) {
    instance = pa;
  }

  public static PrivateAccess getInstance() {
    if (instance == null) {
      try {
        Class.forName("jdk.jfr.EventType", true, PrivateAccess.class.getClassLoader());
      } catch (ClassNotFoundException e) {
        throw new InternalError(e);
      }
    }
    return instance;
  }

  public abstract EventType newEventType(String name, Class<? extends Event> cls, List<ValueDescriptor> fields,
      List<AnnotationElement> annotations, List<SettingDescriptor> settings);

  public abstract Class<? extends Event> eventClass(EventType type);

  public abstract ValueDescriptor newValue(String typeName, String name, List<AnnotationElement> annotations,
      boolean constantPool, List<ValueDescriptor> fields, boolean array);

  public abstract boolean isConstantPool(ValueDescriptor v);

  public abstract SettingDescriptor newSetting(String name, String typeName, String defaultValue,
      List<AnnotationElement> annotations);

  public abstract AnnotationElement newAnnotation(String typeName, List<ValueDescriptor> descriptors,
      Map<String, Object> values, List<AnnotationElement> meta);

  public abstract AnnotationElement ofAnnotation(Annotation a);

  public abstract void writeRecording(java.nio.file.Path dest, List<Row> rows) throws java.io.IOException;

  public abstract byte[] chunkFrom(jdk.jfr.Recording r, int from, int[] next);

  public static final class Row {
    public EventType type;
    public long startEpochNanos;
    public long durationNanos;
    public String thread;
    public long threadId;
    public StackTraceElement[] stack;
    public Map<String, Object> values = new java.util.HashMap<String, Object>();
  }
}
