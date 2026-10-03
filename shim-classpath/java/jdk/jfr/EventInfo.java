/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.jfr;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class EventInfo {
  static final int K_BOOLEAN = 0;
  static final int K_BYTE = 1;
  static final int K_SHORT = 2;
  static final int K_CHAR = 3;
  static final int K_INT = 4;
  static final int K_LONG = 5;
  static final int K_FLOAT = 6;
  static final int K_DOUBLE = 7;
  static final int K_STRING = 8;
  static final int K_CLASS = 9;
  static final int K_THREAD = 10;

  private static final Set<String> SETTING_ANNOTATIONS = new HashSet<String>(Arrays.asList(
      Enabled.class.getName(), StackTrace.class.getName(), Threshold.class.getName(), Period.class.getName(),
      Throttle.class.getName(), Registered.class.getName()));

  final String name;
  final Class<? extends Event> cls;
  EventType type;
  final List<ValueDescriptor> declared;
  final List<ValueDescriptor> allFields = new ArrayList<ValueDescriptor>();
  final boolean defEnabled;
  final boolean defStack;
  final String defThreshold;
  final String defPeriod;
  final boolean registeredByDefault;
  final Field[] reflect;
  final int[] kinds;
  final boolean builtin;
  volatile boolean registered;

  private EventInfo(String name, Class<? extends Event> cls, List<ValueDescriptor> declared, Field[] reflect,
      List<AnnotationElement> annotations, boolean enabled, boolean stack, String threshold, String period,
      boolean registeredByDefault, boolean builtin) {
    this.name = name;
    this.cls = cls;
    this.declared = declared;
    this.reflect = reflect;
    this.defEnabled = enabled;
    this.defStack = stack;
    this.defThreshold = threshold;
    this.defPeriod = period;
    this.registeredByDefault = registeredByDefault;
    this.builtin = builtin;
    this.kinds = new int[declared.size()];
    for (int i = 0; i < kinds.length; i++) {
      kinds[i] = kindOf(declared.get(i).getTypeName());
    }
    allFields.add(ValueDescriptor.internal("long", "startTime", labelAnn("Start Time", Timestamp.class,
        Timestamp.TICKS), false, new ArrayList<ValueDescriptor>(), false));
    allFields.add(ValueDescriptor.internal("long", "duration", labelAnn("Duration", Timespan.class, Timespan.TICKS),
        false, new ArrayList<ValueDescriptor>(), false));
    allFields.add(new ValueDescriptor(Thread.class, "eventThread", Arrays.asList(new AnnotationElement(Label.class,
        "Event Thread"), new AnnotationElement(Description.class, "Thread in which event was committed in"))));
    allFields.add(Std.stackTrace());
    allFields.addAll(declared);
    List<SettingDescriptor> settings = new ArrayList<SettingDescriptor>();
    settings.add(new SettingDescriptor("threshold", "jdk.settings.Threshold", threshold,
        Arrays.asList(new AnnotationElement(Label.class, "Threshold"), new AnnotationElement(Description.class,
            "Record event with duration above or equal to threshold"))));
    settings.add(new SettingDescriptor("stackTrace", "jdk.settings.StackTrace", String.valueOf(stack),
        Arrays.asList(new AnnotationElement(Label.class, "Stack Trace"), new AnnotationElement(Description.class,
            "Record stack traces"))));
    settings.add(new SettingDescriptor("enabled", "jdk.settings.Enabled", String.valueOf(enabled),
        Arrays.asList(new AnnotationElement(Label.class, "Enabled"), new AnnotationElement(Description.class,
            "Record event"))));
    this.type = new EventType(name, cls, allFields, annotations, settings);
  }

  private static List<AnnotationElement> labelAnn(String label, Class<? extends Annotation> a, String value) {
    List<AnnotationElement> l = new ArrayList<AnnotationElement>();
    l.add(new AnnotationElement(Label.class, label));
    l.add(new AnnotationElement(a, value));
    return l;
  }

  static int kindOf(String t) {
    switch (t) {
      case "boolean":
        return K_BOOLEAN;
      case "byte":
        return K_BYTE;
      case "short":
        return K_SHORT;
      case "char":
        return K_CHAR;
      case "int":
        return K_INT;
      case "long":
        return K_LONG;
      case "float":
        return K_FLOAT;
      case "double":
        return K_DOUBLE;
      case "java.lang.String":
        return K_STRING;
      case "java.lang.Class":
        return K_CLASS;
      case "java.lang.Thread":
        return K_THREAD;
      default:
        return -1;
    }
  }

  static EventInfo dynamic(List<AnnotationElement> anns, List<ValueDescriptor> fields) {
    String name = null;
    boolean enabled = true;
    boolean stack = true;
    String threshold = "0 ns";
    String period = null;
    List<AnnotationElement> kept = new ArrayList<AnnotationElement>();
    for (AnnotationElement a : anns) {
      String t = a.getTypeName();
      if (t.equals(Name.class.getName())) {
        name = (String) a.getValue("value");
        kept.add(a);
      } else if (t.equals(Enabled.class.getName())) {
        enabled = (Boolean) a.getValue("value");
      } else if (t.equals(StackTrace.class.getName())) {
        stack = (Boolean) a.getValue("value");
      } else if (t.equals(Threshold.class.getName())) {
        threshold = (String) a.getValue("value");
      } else if (t.equals(Period.class.getName())) {
        period = (String) a.getValue("value");
      } else if (!SETTING_ANNOTATIONS.contains(t)) {
        kept.add(a);
      }
    }
    return new EventInfo(name, null, fields, null, kept, enabled, stack, threshold, period, false, false);
  }

  static EventInfo builtin(String name, String label, String category, String period, List<ValueDescriptor> fields) {
    List<AnnotationElement> kept = new ArrayList<AnnotationElement>();
    kept.add(new AnnotationElement(Name.class, name));
    kept.add(new AnnotationElement(Label.class, label));
    kept.add(new AnnotationElement(Category.class, new String[] {"Java Virtual Machine", category}));
    return new EventInfo(name, null, fields, null, kept, false, false, "0 ns", period, false, true);
  }

  @SuppressWarnings("unchecked")
  static EventInfo of(Class<? extends Event> cls) {
    if (java.lang.reflect.Modifier.isAbstract(cls.getModifiers())) {
      throw new IllegalArgumentException("An abstract class cannot be registered as an event");
    }
    Name n = cls.getAnnotation(Name.class);
    String name = n != null ? n.value() : cls.getName();
    Enabled en = cls.getAnnotation(Enabled.class);
    StackTrace st = cls.getAnnotation(StackTrace.class);
    Threshold th = cls.getAnnotation(Threshold.class);
    Period pe = cls.getAnnotation(Period.class);
    Registered rg = cls.getAnnotation(Registered.class);
    List<AnnotationElement> anns = new ArrayList<AnnotationElement>();
    for (Annotation a : cls.getAnnotations()) {
      if (a.annotationType().getAnnotation(MetadataDefinition.class) != null
          && !SETTING_ANNOTATIONS.contains(a.annotationType().getName())) {
        anns.add(AnnotationElement.of(a));
      }
    }
    List<Class<?>> chain = new ArrayList<Class<?>>();
    for (Class<?> c = cls; c != null && c != Event.class; c = c.getSuperclass()) {
      chain.add(c);
    }
    List<ValueDescriptor> fields = new ArrayList<ValueDescriptor>();
    List<Field> reflect = new ArrayList<Field>();
    Set<String> seen = new HashSet<String>();
    for (Class<?> c : chain) {
      for (Field f : c.getDeclaredFields()) {
        int mod = f.getModifiers();
        if (Modifier.isStatic(mod) || Modifier.isTransient(mod) || f.isSynthetic()) {
          continue;
        }
        Class<?> ft = f.getType();
        if (kindOf(ft.getName()) < 0) {
          continue;
        }
        Name fn = (Name) f.getAnnotation(Name.class);
        String fname = fn != null ? fn.value() : f.getName();
        if (!seen.add(fname)) {
          continue;
        }
        List<AnnotationElement> fa = new ArrayList<AnnotationElement>();
        for (Annotation a : f.getAnnotations()) {
          if (a.annotationType().getAnnotation(MetadataDefinition.class) != null && !(a instanceof Name)) {
            fa.add(AnnotationElement.of(a));
          }
        }
        fields.add(new ValueDescriptor(ft, fname, fa));
        f.setAccessible(true);
        reflect.add(f);
      }
    }
    return new EventInfo(name, cls, fields, reflect.toArray(new Field[0]), anns, en == null || en.value(),
        st == null || st.value(), th == null ? "0 ns" : th.value(), pe == null ? null : pe.value(),
        rg == null || rg.value(), false);
  }

  Object[] extract(Event e) {
    if (e instanceof DynamicEvent) {
      return ((DynamicEvent) e).values.clone();
    }
    Object[] v = new Object[reflect.length];
    for (int i = 0; i < v.length; i++) {
      try {
        Object o = reflect[i].get(e);
        if (o instanceof Class) {
          o = ((Class<?>) o).getName();
        } else if (o instanceof Thread) {
          o = ThreadData.of((Thread) o);
        }
        v[i] = o;
      } catch (IllegalAccessException ex) {
        v[i] = null;
      }
    }
    return v;
  }

  static List<ValueDescriptor> list(ValueDescriptor... v) {
    return new ArrayList<ValueDescriptor>(Arrays.asList(v));
  }

  static List<ValueDescriptor> none() {
    return Collections.emptyList();
  }
}
