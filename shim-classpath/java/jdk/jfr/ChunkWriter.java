/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.jfr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import jdk.jfr.internal.Types;

final class ChunkWriter {
  private static final byte[] MAGIC = {'F', 'L', 'R', 0};

  private static final class El {
    final String name;
    final List<String> attrs = new ArrayList<String>();
    final List<El> kids = new ArrayList<El>();

    El(String name) {
      this.name = name;
    }

    El attr(String k, String v) {
      attrs.add(k);
      attrs.add(v);
      return this;
    }
  }

  private final Map<Long, ThreadData> threads = new LinkedHashMap<Long, ThreadData>();
  private final Map<String, Integer> groups = new LinkedHashMap<String, Integer>();
  private final List<String[]> groupData = new ArrayList<String[]>();
  private final Map<List<StackTraceElement>, Integer> stacks = new LinkedHashMap<List<StackTraceElement>, Integer>();
  private final Map<String, Integer> methods = new LinkedHashMap<String, Integer>();
  private final List<int[]> methodData = new ArrayList<int[]>();
  private final Map<String, Integer> classes = new LinkedHashMap<String, Integer>();
  private final Map<String, Integer> symbols = new LinkedHashMap<String, Integer>();
  private final Map<String, El> typeEls = new LinkedHashMap<String, El>();
  private final List<El> typeOrder = new ArrayList<El>();

  byte[] write(List<EventRecord> records, long startTicks, long endTicks) {
    Buf out = new Buf();
    for (int i = 0; i < 68; i++) {
      out.u8(0);
    }
    List<EventInfo> infos = new ArrayList<EventInfo>(JfrRuntime.REGISTERED.values());
    for (EventRecord r : records) {
      if (!infos.contains(r.info)) {
        infos.add(r.info);
      }
    }
    Collections.sort(infos, new Comparator<EventInfo>() {
      public int compare(EventInfo a, EventInfo b) {
        return a.name.compareTo(b.name);
      }
    });
    int metaOffset = out.len;
    writeMetadata(out, infos, startTicks);
    for (EventRecord r : records) {
      writeEvent(out, r);
    }
    int cpOffset = out.len;
    writeCheckpoint(out, endTicks);
    out.data[0] = MAGIC[0];
    out.data[1] = MAGIC[1];
    out.data[2] = MAGIC[2];
    out.data[3] = MAGIC[3];
    out.data[4] = 0;
    out.data[5] = 2;
    out.data[6] = 0;
    out.data[7] = 1;
    out.putI64(8, out.len);
    out.putI64(16, cpOffset);
    out.putI64(24, metaOffset);
    out.putI64(32, JfrRuntime.epochOf(startTicks));
    out.putI64(40, Math.max(0, endTicks - startTicks));
    out.putI64(48, startTicks);
    out.putI64(56, 1000000000L);
    out.putI32(64, 1);
    return Arrays.copyOf(out.data, out.len);
  }

  private int symbol(String s) {
    if (s == null) {
      return 0;
    }
    Integer k = symbols.get(s);
    if (k == null) {
      k = symbols.size() + 1;
      symbols.put(s, k);
    }
    return k;
  }

  private int clazz(String name) {
    if (name == null) {
      return 0;
    }
    Integer k = classes.get(name);
    if (k == null) {
      symbol(name);
      k = classes.size() + 1;
      classes.put(name, k);
    }
    return k;
  }

  private int group(String[] chain, int from) {
    if (from >= chain.length) {
      return 0;
    }
    StringBuilder path = new StringBuilder();
    for (int i = chain.length - 1; i >= from; i--) {
      path.append('/').append(chain[i]);
    }
    String key = path.toString();
    Integer k = groups.get(key);
    if (k == null) {
      int parent = group(chain, from + 1);
      k = groups.size() + 1;
      groups.put(key, k);
      groupData.add(new String[] {String.valueOf(parent), chain[from]});
    }
    return k;
  }

  private int method(StackTraceElement e) {
    String key = e.getClassName() + "#" + e.getMethodName();
    Integer k = methods.get(key);
    if (k == null) {
      int[] d = {clazz(e.getClassName()), symbol(e.getMethodName()), symbol("()V"), 1};
      k = methods.size() + 1;
      methods.put(key, k);
      methodData.add(d);
    }
    return k;
  }

  private int stack(StackTraceElement[] st) {
    if (st == null) {
      return 0;
    }
    List<StackTraceElement> key = Arrays.asList(st);
    Integer k = stacks.get(key);
    if (k == null) {
      for (StackTraceElement e : st) {
        method(e);
      }
      k = stacks.size() + 1;
      stacks.put(key, k);
    }
    return k;
  }

  private void writeEvent(Buf out, EventRecord r) {
    Buf b = new Buf();
    b.varlong(r.info.type.getId());
    b.varlong(r.start);
    b.varlong(r.duration);
    int tk = 0;
    if (r.thread != null) {
      threads.put(r.thread.id, r.thread);
      tk = (int) r.thread.id;
      group(r.thread.groups, 0);
    }
    b.varlong(tk);
    b.varlong(stack(r.stack));
    for (int i = 0; i < r.info.kinds.length; i++) {
      Object v = i < r.values.length ? r.values[i] : null;
      switch (r.info.kinds[i]) {
        case EventInfo.K_BOOLEAN:
          b.u8(v instanceof Boolean && (Boolean) v ? 1 : 0);
          break;
        case EventInfo.K_BYTE:
          b.u8(v instanceof Number ? ((Number) v).intValue() : 0);
          break;
        case EventInfo.K_SHORT:
          b.varlong((v instanceof Number ? ((Number) v).shortValue() : 0) & 0xffffL);
          break;
        case EventInfo.K_CHAR:
          b.varlong(v instanceof Character ? (Character) v : 0);
          break;
        case EventInfo.K_INT:
          b.varlong((v instanceof Number ? ((Number) v).intValue() : 0) & 0xffffffffL);
          break;
        case EventInfo.K_LONG:
          b.varlong(v instanceof Number ? ((Number) v).longValue() : 0);
          break;
        case EventInfo.K_FLOAT:
          b.i32(Float.floatToIntBits(v instanceof Number ? ((Number) v).floatValue() : 0));
          break;
        case EventInfo.K_DOUBLE:
          b.i64(Double.doubleToLongBits(v instanceof Number ? ((Number) v).doubleValue() : 0));
          break;
        case EventInfo.K_STRING:
          b.str(v == null ? null : v.toString());
          break;
        case EventInfo.K_CLASS:
          b.varlong(clazz(v == null ? null : v.toString()));
          break;
        default:
          if (v instanceof ThreadData) {
            ThreadData t = (ThreadData) v;
            threads.put(t.id, t);
            group(t.groups, 0);
            b.varlong(t.id);
          } else {
            b.varlong(0);
          }
          break;
      }
    }
    out.writeEvent(b);
  }

  private void writeCheckpoint(Buf out, long endTicks) {
    Buf b = new Buf();
    b.varlong(1);
    b.varlong(endTicks);
    b.varlong(0);
    b.varlong(0);
    b.u8(0);
    List<Runnable> pools = new ArrayList<Runnable>();
    final Buf p = new Buf();
    int n = 0;
    p.varlong(Types.idOf("java.lang.Thread"));
    p.varlong(threads.size());
    for (ThreadData t : threads.values()) {
      p.varlong(t.id);
      p.str(t.name);
      p.varlong(t.id);
      p.str(t.name);
      p.varlong(t.id);
      p.varlong(group(t.groups, 0));
      p.u8(t.virtual ? 1 : 0);
    }
    n++;
    if (!groupData.isEmpty()) {
      p.varlong(Types.idOf("jdk.types.ThreadGroup"));
      p.varlong(groupData.size());
      for (int i = 0; i < groupData.size(); i++) {
        p.varlong(i + 1);
        p.varlong(Long.parseLong(groupData.get(i)[0]));
        p.str(groupData.get(i)[1]);
      }
      n++;
    }
    if (!stacks.isEmpty()) {
      p.varlong(Types.idOf("jdk.types.StackTrace"));
      p.varlong(stacks.size());
      int key = 1;
      for (List<StackTraceElement> st : stacks.keySet()) {
        p.varlong(key++);
        p.u8(0);
        p.varlong(st.size());
        for (StackTraceElement e : st) {
          p.varlong(method(e));
          p.varlong(Math.max(e.getLineNumber(), 0));
          p.varlong(0);
          p.varlong(0);
        }
      }
      n++;
    }
    if (!methods.isEmpty()) {
      p.varlong(Types.idOf("jdk.types.Method"));
      p.varlong(methodData.size());
      for (int i = 0; i < methodData.size(); i++) {
        int[] d = methodData.get(i);
        p.varlong(i + 1);
        p.varlong(d[0]);
        p.varlong(d[1]);
        p.varlong(d[2]);
        p.varlong(d[3]);
        p.u8(0);
      }
      n++;
    }
    if (!classes.isEmpty()) {
      p.varlong(Types.idOf("java.lang.Class"));
      p.varlong(classes.size());
      for (Map.Entry<String, Integer> e : classes.entrySet()) {
        p.varlong(e.getValue());
        p.varlong(0);
        p.varlong(symbol(e.getKey()));
        p.varlong(0);
        p.varlong(1);
        p.u8(0);
      }
      n++;
    }
    p.varlong(Types.idOf("jdk.types.FrameType"));
    p.varlong(1);
    p.varlong(0);
    p.str("Interpreted");
    n++;
    if (!symbols.isEmpty()) {
      p.varlong(Types.idOf("jdk.types.Symbol"));
      p.varlong(symbols.size());
      for (Map.Entry<String, Integer> e : symbols.entrySet()) {
        p.varlong(e.getValue());
        p.str(e.getKey());
      }
      n++;
    }
    b.varlong(n);
    b.bytes(p.data, 0, p.len);
    out.writeEvent(b);
  }

  private void writeMetadata(Buf out, List<EventInfo> infos, long startTicks) {
    El root = new El("root");
    El meta = new El("metadata");
    root.kids.add(meta);
    ensureValue(Std.stackTrace(), meta);
    ensureValue(new ValueDescriptor(Thread.class, "t"), meta);
    for (EventInfo i : infos) {
      eventClass(i, meta);
    }
    El region = new El("region");
    region.attr("locale", java.util.Locale.getDefault().toString());
    java.util.TimeZone tz = java.util.TimeZone.getDefault();
    region.attr("gmtOffset", String.valueOf(tz.getRawOffset())).attr("dst", "0");
    root.kids.add(region);
    Map<String, Integer> strings = new LinkedHashMap<String, Integer>();
    Buf tree = new Buf();
    serialize(root, strings, tree);
    Buf b = new Buf();
    b.varlong(0);
    b.varlong(startTicks);
    b.varlong(0);
    b.varlong(0);
    b.varlong(strings.size());
    for (String s : strings.keySet()) {
      b.str(s);
    }
    b.bytes(tree.data, 0, tree.len);
    out.writeEvent(b);
  }

  private static int intern(Map<String, Integer> m, String s) {
    Integer k = m.get(s);
    if (k == null) {
      k = m.size();
      m.put(s, k);
    }
    return k;
  }

  private void serialize(El e, Map<String, Integer> strings, Buf out) {
    out.varlong(intern(strings, e.name));
    out.varlong(e.attrs.size() / 2);
    for (int i = 0; i < e.attrs.size(); i += 2) {
      out.varlong(intern(strings, e.attrs.get(i)));
      out.varlong(intern(strings, e.attrs.get(i + 1)));
    }
    out.varlong(e.kids.size());
    for (El k : e.kids) {
      serialize(k, strings, out);
    }
  }

  private static String id(String type) {
    return String.valueOf(Types.idOf(type));
  }

  private El typeEl(String name, El meta) {
    El c = typeEls.get(name);
    if (c == null) {
      c = new El("class").attr("name", name);
      typeEls.put(name, c);
      meta.kids.add(c);
      return c;
    }
    return null;
  }

  private void ensureValue(ValueDescriptor v, El meta) {
    String tn = v.getTypeName();
    El c = typeEl(tn, meta);
    if (c == null) {
      return;
    }
    if (tn.equals("jdk.types.Symbol") || tn.equals("jdk.types.FrameType")) {
      c.attr("simpleType", "true");
    }
    c.attr("id", id(tn));
    for (ValueDescriptor f : v.getFields()) {
      c.kids.add(fieldEl(f, meta));
    }
  }

  private El fieldEl(ValueDescriptor f, El meta) {
    ensureValue(f, meta);
    El fe = new El("field").attr("name", f.getName()).attr("class", id(f.getTypeName()));
    if (f.isConstantPool()) {
      fe.attr("constantPool", "true");
    }
    if (f.isArray()) {
      fe.attr("dimension", "1");
    }
    for (AnnotationElement a : f.getAnnotationElements()) {
      fe.kids.add(annEl(a, meta));
    }
    return fe;
  }

  private El annEl(AnnotationElement a, El meta) {
    ensureAnnotationType(a, meta);
    El e = new El("annotation").attr("class", id(a.getTypeName()));
    for (ValueDescriptor d : a.getValueDescriptors()) {
      if (!a.hasValue(d.getName())) {
        continue;
      }
      Object v = a.getValue(d.getName());
      if (v != null && v.getClass().isArray()) {
        int n = java.lang.reflect.Array.getLength(v);
        for (int i = 0; i < n; i++) {
          e.attr(d.getName() + "-" + i, String.valueOf(java.lang.reflect.Array.get(v, i)));
        }
      } else {
        e.attr(d.getName(), String.valueOf(v));
      }
    }
    return e;
  }

  private void ensureAnnotationType(AnnotationElement a, El meta) {
    String tn = a.getTypeName();
    El c = typeEl(tn, meta);
    if (c == null) {
      return;
    }
    c.attr("superType", "java.lang.annotation.Annotation").attr("id", id(tn));
    for (ValueDescriptor d : a.getValueDescriptors()) {
      ensureValue(d, meta);
      El fe = new El("field").attr("name", d.getName()).attr("class", id(d.getTypeName()));
      if (d.isArray()) {
        fe.attr("dimension", "1");
      }
      c.kids.add(fe);
    }
    for (AnnotationElement m : a.getAnnotationElements()) {
      c.kids.add(annEl(m, meta));
    }
  }

  private void settingType(String cls, String name, String label, String desc, El meta, boolean flag,
      String timespan) {
    El c = typeEl(cls, meta);
    if (c == null) {
      return;
    }
    c.attr("superType", "jdk.jfr.SettingControl").attr("id", id(cls));
    c.kids.add(annEl(new AnnotationElement(Label.class, label), meta));
    c.kids.add(annEl(new AnnotationElement(Description.class, desc), meta));
    c.kids.add(annEl(new AnnotationElement(Name.class, cls), meta));
    if (flag) {
      c.kids.add(annEl(new AnnotationElement(BooleanFlag.class), meta));
    }
    if (timespan != null) {
      c.kids.add(annEl(new AnnotationElement(Timespan.class, timespan), meta));
    }
  }

  private void eventClass(EventInfo info, El meta) {
    settingType("jdk.settings.Enabled", "enabled", "Enabled", "Record event", meta, true, null);
    settingType("jdk.settings.StackTrace", "stackTrace", "Stack Trace", "Record stack traces", meta, true, null);
    settingType("jdk.settings.Threshold", "threshold", "Threshold",
        "Record event with duration above or equal to threshold", meta, false, Timespan.NANOSECONDS);
    settingType("jdk.settings.Period", "period", "Period", "Record event at interval", meta, false, null);
    El c = new El("class").attr("name", info.name).attr("superType", "jdk.jfr.Event").attr("id",
        id(info.name));
    typeEls.put(info.name, c);
    meta.kids.add(c);
    for (SettingDescriptor s : info.type.getSettingDescriptors()) {
      c.kids.add(new El("setting").attr("name", s.getName()).attr("class", id(s.getTypeName()))
          .attr("defaultValue", s.getDefaultValue()));
    }
    for (ValueDescriptor f : info.allFields) {
      c.kids.add(fieldEl(f, meta));
    }
    for (AnnotationElement a : info.type.getAnnotationElements()) {
      c.kids.add(annEl(a, meta));
    }
  }
}
