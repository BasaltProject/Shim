/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.jfr.consumer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import jdk.jfr.AnnotationElement;
import jdk.jfr.EventType;
import jdk.jfr.SettingDescriptor;
import jdk.jfr.ValueDescriptor;
import jdk.jfr.internal.PrivateAccess;

final class Chunk {
  static final class El {
    String name;
    Map<String, String> attrs = new LinkedHashMap<String, String>();
    List<El> kids = new ArrayList<El>();
  }

  static final class Ref {
    final long type;
    final long key;

    Ref(long type, long key) {
      this.type = type;
      this.key = key;
    }
  }

  static final class FieldDef {
    String name;
    long typeId;
    boolean cp;
    int dim;
    List<El> annotations = new ArrayList<El>();
  }

  static final class TypeDef {
    long id;
    String name;
    String superType;
    boolean simple;
    List<FieldDef> fields = new ArrayList<FieldDef>();
    List<El> annotations = new ArrayList<El>();
    List<El> settings = new ArrayList<El>();
    List<ValueDescriptor> descriptors;
  }

  long startNanos;
  long durationNanos;
  long startTicks;
  long ticksPerSecond;
  final Map<Long, TypeDef> types = new LinkedHashMap<Long, TypeDef>();
  final Map<Long, Map<Long, Object[]>> rawPools = new HashMap<Long, Map<Long, Object[]>>();
  final Map<Long, Map<Long, Object>> pools = new HashMap<Long, Map<Long, Object>>();
  final List<RecordedEvent> events = new ArrayList<RecordedEvent>();
  final Map<Long, EventType> eventTypes = new LinkedHashMap<Long, EventType>();
  private final Map<Long, List<ValueDescriptor>> building = new HashMap<Long, List<ValueDescriptor>>();
  private byte[] b;
  private int p;

  static List<Chunk> parse(byte[] data) throws IOException {
    List<Chunk> out = new ArrayList<Chunk>();
    int pos = 0;
    if (data.length < 68) {
      throw new IOException("Not a valid Flight Recorder file. File magic is missing");
    }
    while (pos + 68 <= data.length) {
      Chunk c = new Chunk();
      long size = c.header(data, pos);
      c.read(pos, size);
      out.add(c);
      if (size <= 0) {
        break;
      }
      pos += (int) size;
    }
    return out;
  }

  private long i64(int at) {
    long v = 0;
    for (int i = 0; i < 8; i++) {
      v = (v << 8) | (b[at + i] & 0xff);
    }
    return v;
  }

  private long header(byte[] data, int pos) throws IOException {
    this.b = data;
    if (data[pos] != 'F' || data[pos + 1] != 'L' || data[pos + 2] != 'R' || data[pos + 3] != 0) {
      throw new IOException("Not a valid Flight Recorder file. File magic is " + String.format("%02x%02x%02x%02x",
          data[pos], data[pos + 1], data[pos + 2], data[pos + 3]) + ", expected 464c5200");
    }
    startNanos = i64(pos + 32);
    durationNanos = i64(pos + 40);
    startTicks = i64(pos + 48);
    ticksPerSecond = i64(pos + 56);
    return i64(pos + 8);
  }

  private void read(int base, long size) throws IOException {
    try {
      long cpOffset = i64(base + 16);
      long metaOffset = i64(base + 24);
      readMetadata(base + (int) metaOffset);
      readCheckpoints(base + (int) cpOffset);
      p = base + 68;
      int end = base + (int) size;
      while (p < end) {
        int start = p;
        long sz = varlong();
        long type = varlong();
        if (type > 1) {
          TypeDef td = types.get(type);
          if (td != null) {
            Object[] vals = new Object[td.fields.size()];
            for (int i = 0; i < vals.length; i++) {
              vals[i] = readValue(td.fields.get(i));
            }
            events.add(new RecordedEvent(td, vals, this));
          }
        }
        p = start + (int) sz;
      }
    } catch (ArrayIndexOutOfBoundsException e) {
      throw new IOException("Unexpected end of file");
    }
  }

  long varlong() {
    long r = 0;
    for (int i = 0; i < 8; i++) {
      int x = b[p++] & 0xff;
      r |= (long) (x & 0x7f) << (7 * i);
      if ((x & 0x80) == 0) {
        return r;
      }
    }
    return r | (long) (b[p++] & 0xff) << 56;
  }

  private String string(String[] pool) {
    int enc = b[p++];
    switch (enc) {
      case 0:
        return null;
      case 1:
        return "";
      case 2: {
        long key = varlong();
        Map<Long, Object[]> m = rawPools.get(typeIdByName("java.lang.String"));
        if (m != null && m.get(key) != null) {
          return (String) m.get(key)[0];
        }
        return null;
      }
      case 3: {
        int n = (int) varlong();
        String s = new String(b, p, n, StandardCharsets.UTF_8);
        p += n;
        return s;
      }
      case 4: {
        int n = (int) varlong();
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) {
          sb.append((char) varlong());
        }
        return sb.toString();
      }
      case 5: {
        int n = (int) varlong();
        String s = new String(b, p, n, StandardCharsets.ISO_8859_1);
        p += n;
        return s;
      }
      default:
        throw new IllegalStateException("Unknown string encoding " + enc);
    }
  }

  private long typeIdByName(String n) {
    for (TypeDef t : types.values()) {
      if (t.name.equals(n)) {
        return t.id;
      }
    }
    return -1;
  }

  private void readMetadata(int at) {
    p = at;
    varlong();
    varlong();
    varlong();
    varlong();
    varlong();
    int n = (int) varlong();
    String[] strs = new String[n];
    for (int i = 0; i < n; i++) {
      strs[i] = string(null);
    }
    El root = element(strs);
    for (El m : root.kids) {
      if (!m.name.equals("metadata")) {
        continue;
      }
      for (El c : m.kids) {
        TypeDef td = new TypeDef();
        td.id = Long.parseLong(c.attrs.get("id"));
        td.name = c.attrs.get("name");
        td.superType = c.attrs.get("superType");
        td.simple = "true".equals(c.attrs.get("simpleType"));
        for (El k : c.kids) {
          if (k.name.equals("field")) {
            FieldDef f = new FieldDef();
            f.name = k.attrs.get("name");
            f.typeId = Long.parseLong(k.attrs.get("class"));
            f.cp = "true".equals(k.attrs.get("constantPool"));
            f.dim = k.attrs.containsKey("dimension") ? Integer.parseInt(k.attrs.get("dimension")) : 0;
            for (El a : k.kids) {
              if (a.name.equals("annotation")) {
                f.annotations.add(a);
              }
            }
            td.fields.add(f);
          } else if (k.name.equals("annotation")) {
            td.annotations.add(k);
          } else if (k.name.equals("setting")) {
            td.settings.add(k);
          }
        }
        types.put(td.id, td);
      }
    }
  }

  private El element(String[] strs) {
    El e = new El();
    e.name = strs[(int) varlong()];
    int na = (int) varlong();
    for (int i = 0; i < na; i++) {
      String k = strs[(int) varlong()];
      e.attrs.put(k, strs[(int) varlong()]);
    }
    int nc = (int) varlong();
    for (int i = 0; i < nc; i++) {
      e.kids.add(element(strs));
    }
    return e;
  }

  private void readCheckpoints(int at) {
    int cur = at;
    List<Integer> chain = new ArrayList<Integer>();
    while (true) {
      p = cur;
      varlong();
      varlong();
      varlong();
      varlong();
      long delta = varlong();
      chain.add(cur);
      if (delta == 0) {
        break;
      }
      cur += (int) delta;
    }
    Collections.reverse(chain);
    for (int off : chain) {
      p = off;
      varlong();
      varlong();
      varlong();
      varlong();
      varlong();
      p++;
      int np = (int) varlong();
      for (int i = 0; i < np; i++) {
        long tid = varlong();
        TypeDef td = types.get(tid);
        int count = (int) varlong();
        Map<Long, Object[]> m = rawPools.get(tid);
        if (m == null) {
          m = new HashMap<Long, Object[]>();
          rawPools.put(tid, m);
        }
        for (int j = 0; j < count; j++) {
          long key = varlong();
          Object[] vals = new Object[td.fields.size()];
          for (int k = 0; k < vals.length; k++) {
            vals[k] = readValue(td.fields.get(k));
          }
          m.put(key, vals);
        }
      }
    }
  }

  private Object readValue(FieldDef f) {
    if (f.dim > 0) {
      int n = (int) varlong();
      Object[] a = new Object[n];
      for (int i = 0; i < n; i++) {
        a[i] = readOne(f);
      }
      return a;
    }
    return readOne(f);
  }

  private Object readOne(FieldDef f) {
    TypeDef td = types.get(f.typeId);
    String n = td.name;
    if (f.cp) {
      return new Ref(f.typeId, varlong());
    }
    switch (n) {
      case "boolean":
        return b[p++] != 0;
      case "byte":
        return b[p++];
      case "char":
        return (char) varlong();
      case "short":
        return (short) varlong();
      case "int":
        return (int) varlong();
      case "long":
        return varlong();
      case "float": {
        int bits = ((b[p] & 0xff) << 24) | ((b[p + 1] & 0xff) << 16) | ((b[p + 2] & 0xff) << 8) | (b[p + 3] & 0xff);
        p += 4;
        return Float.intBitsToFloat(bits);
      }
      case "double": {
        long bits = i64(p);
        p += 8;
        return Double.longBitsToDouble(bits);
      }
      case "java.lang.String":
        return string(null);
      default: {
        Object[] vals = new Object[td.fields.size()];
        for (int i = 0; i < vals.length; i++) {
          vals[i] = readValue(td.fields.get(i));
        }
        return new Struct(td, vals);
      }
    }
  }

  static final class Struct {
    final TypeDef td;
    final Object[] vals;

    Struct(TypeDef td, Object[] vals) {
      this.td = td;
      this.vals = vals;
    }
  }

  Object resolve(Object v) {
    if (v instanceof Ref) {
      Ref r = (Ref) v;
      Map<Long, Object> cache = pools.get(r.type);
      if (cache == null) {
        cache = new HashMap<Long, Object>();
        pools.put(r.type, cache);
      }
      if (cache.containsKey(r.key)) {
        return cache.get(r.key);
      }
      Map<Long, Object[]> raw = rawPools.get(r.type);
      Object[] vals = raw == null ? null : raw.get(r.key);
      Object res = null;
      if (vals != null) {
        TypeDef td = types.get(r.type);
        res = td.simple && vals.length == 1 ? vals[0] : wrap(td, vals);
      }
      cache.put(r.key, res);
      return res;
    }
    if (v instanceof Struct) {
      Struct s = (Struct) v;
      return s.td.simple && s.vals.length == 1 ? s.vals[0] : wrap(s.td, s.vals);
    }
    if (v instanceof Object[]) {
      Object[] a = (Object[]) v;
      List<Object> l = new ArrayList<Object>(a.length);
      for (Object o : a) {
        l.add(resolve(o));
      }
      return l;
    }
    return v;
  }

  RecordedObject wrap(TypeDef td, Object[] vals) {
    switch (td.name) {
      case "java.lang.Thread":
        return new RecordedThread(td, vals, this);
      case "jdk.types.ThreadGroup":
        return new RecordedThreadGroup(td, vals, this);
      case "java.lang.Class":
        return new RecordedClass(td, vals, this);
      case "jdk.types.ClassLoader":
        return new RecordedClassLoader(td, vals, this);
      case "jdk.types.Method":
        return new RecordedMethod(td, vals, this);
      case "jdk.types.StackFrame":
        return new RecordedFrame(td, vals, this);
      case "jdk.types.StackTrace":
        return new RecordedStackTrace(td, vals, this);
      default:
        return new RecordedObject(td, vals, this);
    }
  }

  AnnotationElement annotation(El a) {
    PrivateAccess pa = PrivateAccess.getInstance();
    TypeDef at = types.get(Long.parseLong(a.attrs.get("class")));
    List<ValueDescriptor> ds = new ArrayList<ValueDescriptor>();
    Map<String, Object> vals = new LinkedHashMap<String, Object>();
    for (FieldDef f : at.fields) {
      String tn = types.get(f.typeId).name;
      ds.add(pa.newValue(tn, f.name, new ArrayList<AnnotationElement>(), false, new ArrayList<ValueDescriptor>(),
          f.dim > 0));
      if (f.dim > 0) {
        List<Object> arr = new ArrayList<Object>();
        for (int i = 0; a.attrs.containsKey(f.name + "-" + i); i++) {
          arr.add(convert(tn, a.attrs.get(f.name + "-" + i)));
        }
        vals.put(f.name, arr.toArray(new String[0]));
      } else if (a.attrs.containsKey(f.name)) {
        vals.put(f.name, convert(tn, a.attrs.get(f.name)));
      }
    }
    List<AnnotationElement> meta = new ArrayList<AnnotationElement>();
    for (El m : at.annotations) {
      meta.add(annotation(m));
    }
    return pa.newAnnotation(at.name, ds, vals, meta);
  }

  private static Object convert(String type, String v) {
    switch (type) {
      case "boolean":
        return Boolean.valueOf(v);
      case "int":
        return Integer.valueOf(v);
      case "long":
        return Long.valueOf(v);
      case "double":
        return Double.valueOf(v);
      case "float":
        return Float.valueOf(v);
      default:
        return v;
    }
  }

  List<AnnotationElement> annotations(List<El> raw) {
    List<AnnotationElement> l = new ArrayList<AnnotationElement>();
    for (El a : raw) {
      l.add(annotation(a));
    }
    return l;
  }

  List<ValueDescriptor> descriptors(TypeDef td) {
    if (td.descriptors != null) {
      return td.descriptors;
    }
    List<ValueDescriptor> inProgress = building.get(td.id);
    if (inProgress != null) {
      return Collections.emptyList();
    }
    List<ValueDescriptor> l = new ArrayList<ValueDescriptor>();
    building.put(td.id, l);
    PrivateAccess pa = PrivateAccess.getInstance();
    for (FieldDef f : td.fields) {
      TypeDef ft = types.get(f.typeId);
      boolean prim = isPrimitiveOrString(ft.name);
      l.add(pa.newValue(ft.name, f.name, annotations(f.annotations), f.cp,
          prim ? new ArrayList<ValueDescriptor>() : descriptors(ft), f.dim > 0));
    }
    building.remove(td.id);
    td.descriptors = l;
    return l;
  }

  private static boolean isPrimitiveOrString(String n) {
    switch (n) {
      case "boolean":
      case "byte":
      case "char":
      case "short":
      case "int":
      case "long":
      case "float":
      case "double":
      case "java.lang.String":
        return true;
      default:
        return false;
    }
  }

  EventType eventType(TypeDef td) {
    EventType t = eventTypes.get(td.id);
    if (t == null) {
      PrivateAccess pa = PrivateAccess.getInstance();
      List<SettingDescriptor> settings = new ArrayList<SettingDescriptor>();
      for (El s : td.settings) {
        TypeDef st = types.get(Long.parseLong(s.attrs.get("class")));
        settings.add(pa.newSetting(s.attrs.get("name"), st.name, s.attrs.get("defaultValue"),
            annotations(st.annotations)));
      }
      t = pa.newEventType(td.name, null, descriptors(td), annotations(td.annotations), settings);
      eventTypes.put(td.id, t);
    }
    return t;
  }

  java.time.Instant instantOf(long ticks) {
    long ns = startNanos + (long) ((ticks - startTicks) * (1e9 / ticksPerSecond));
    return java.time.Instant.ofEpochSecond(Math.floorDiv(ns, 1000000000L), Math.floorMod(ns, 1000000000L));
  }

  String unit(FieldDef f, String annotationType) {
    for (El a : f.annotations) {
      TypeDef at = types.get(Long.parseLong(a.attrs.get("class")));
      if (at.name.equals(annotationType)) {
        return a.attrs.get("value");
      }
    }
    return null;
  }
}
