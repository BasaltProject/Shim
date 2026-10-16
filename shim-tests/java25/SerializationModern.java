/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.io.*;
import java.util.*;
import java.util.function.Supplier;

public class SerializationModern {
  static void probe(String name, Supplier<Object> body) {
    String out;
    try {
      out = String.valueOf(body.get());
    } catch (Throwable t) {
      out = "!" + t.getClass().getName() + ":" + t.getMessage();
    }
    System.out.println(name + "=" + out);
  }

  interface IoBody { Object run() throws Exception; }

  static void io(String name, IoBody body) {
    probe(name, () -> {
      try { return body.run(); } catch (Exception e) { return "!" + e.getClass().getName() + ":" + e.getMessage(); }
    });
  }

  static String hex(byte[] b) {
    StringBuilder sb = new StringBuilder();
    for (byte x : b) sb.append(String.format("%02x", x));
    return sb.toString();
  }

  static byte[] ser(Object... objs) throws IOException {
    ByteArrayOutputStream bo = new ByteArrayOutputStream();
    try (ObjectOutputStream o = new ObjectOutputStream(bo)) { for (Object x : objs) o.writeObject(x); }
    return bo.toByteArray();
  }

  @SuppressWarnings("unchecked")
  static <T> T de(byte[] b) throws Exception {
    try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(b))) { return (T) in.readObject(); }
  }

  static class Plain implements Serializable {
    private static final long serialVersionUID = 1L;
    int a = 5; String s = "hi"; transient int t = 9; double[] d = {1.5, 2.5}; boolean z = true; byte b = -3; char c = 'q';
    short sh = 300; long l = 1L << 40; float f = 2.5f; Plain self;
  }

  static class NoSuid implements Serializable {
    int x = 1; String y = "y";
    public int get() { return x; }
    static int util() { return 2; }
  }

  static class Base { int baseVal = 7; Base() { baseVal = 11; } }
  static class Derived extends Base implements Serializable { private static final long serialVersionUID = 2L; int d = 3; }

  static class Custom implements Serializable {
    private static final long serialVersionUID = 3L;
    int a = 1; transient int b = 2; String text = "custom";
    private void writeObject(ObjectOutputStream o) throws IOException { o.defaultWriteObject(); o.writeInt(b + 40); o.writeUTF("extra"); }
    private void readObject(ObjectInputStream i) throws IOException, ClassNotFoundException { i.defaultReadObject(); b = i.readInt(); text = text + i.readUTF(); }
  }

  static class Ext implements Externalizable {
    int v; String w;
    public Ext() { }
    Ext(int v, String w) { this.v = v; this.w = w; }
    public void writeExternal(ObjectOutput o) throws IOException { o.writeInt(v); o.writeUTF(w); }
    public void readExternal(ObjectInput i) throws IOException { v = i.readInt(); w = i.readUTF(); }
  }

  enum Color { RED, GREEN { public String toString() { return "g"; } }, BLUE }

  static class Single implements Serializable {
    private static final long serialVersionUID = 4L;
    static final Single INSTANCE = new Single();
    private Object readResolve() { return INSTANCE; }
  }

  static class Replaced implements Serializable {
    private static final long serialVersionUID = 5L;
    private Object writeReplace() { return "replaced"; }
  }

  static class Fields implements Serializable {
    private static final long serialVersionUID = 6L;
    private static final ObjectStreamField[] serialPersistentFields = {
        new ObjectStreamField("name", String.class), new ObjectStreamField("count", int.class) };
    String n = "nm"; int c = 9;
    private void writeObject(ObjectOutputStream o) throws IOException {
      ObjectOutputStream.PutField pf = o.putFields(); pf.put("name", n + "!"); pf.put("count", c * 2); o.writeFields();
    }
    private void readObject(ObjectInputStream i) throws IOException, ClassNotFoundException {
      ObjectInputStream.GetField gf = i.readFields(); n = (String) gf.get("name", "dflt"); c = gf.get("count", -1);
    }
  }

  static class Validated implements Serializable, ObjectInputValidation {
    private static final long serialVersionUID = 7L;
    static final List<String> LOG = new ArrayList<>();
    int v = 5;
    private void readObject(ObjectInputStream i) throws IOException, ClassNotFoundException {
      i.defaultReadObject(); LOG.add("read"); i.registerValidation(this, 0);
    }
    public void validateObject() { LOG.add("validate"); }
  }

  record Point(int x, String label) implements Serializable { }

  static class NotSer { }
  static class HoldsNot implements Serializable { private static final long serialVersionUID = 8L; NotSer n = new NotSer(); }

  public static void main(String[] args) throws Exception {
    Plain p = new Plain(); p.self = p;
    io("plain-bytes", () -> hex(ser(new Plain())));
    io("plain-roundtrip", () -> { Plain r = de(ser(p)); return r.a + r.s + r.t + Arrays.toString(r.d) + r.z + r.b + r.c + r.sh + r.l + r.f + (r.self == r); });
    io("nosuid-bytes", () -> hex(ser(new NoSuid())));
    io("nosuid-suid", () -> ObjectStreamClass.lookup(NoSuid.class).getSerialVersionUID());
    io("derived-ctor", () -> { Derived r = de(ser(new Derived())); return r.baseVal + " " + r.d; });
    io("custom-bytes", () -> hex(ser(new Custom())));
    io("custom-roundtrip", () -> { Custom r = de(ser(new Custom())); return r.a + " " + r.b + " " + r.text; });
    io("ext-bytes", () -> hex(ser(new Ext(4, "w"))));
    io("ext-roundtrip", () -> { Ext r = de(ser(new Ext(4, "w"))); return r.v + r.w; });
    io("enum", () -> { Color c = de(ser(Color.GREEN)); return c + " " + (c == Color.GREEN) + " " + hex(ser(Color.BLUE)); });
    io("resolve", () -> { Object o = de(ser(Single.INSTANCE)); return o == Single.INSTANCE; });
    io("replace", () -> de(ser(new Replaced())));
    io("putfields-bytes", () -> hex(ser(new Fields())));
    io("putfields-roundtrip", () -> { Fields f = de(ser(new Fields())); return f.n + f.c; });
    io("validation", () -> { Validated.LOG.clear(); Validated v = de(ser(new Validated())); return Validated.LOG + " " + v.v; });
    io("record-bytes", () -> hex(ser(new Point(3, "p"))));
    io("record", () -> de(ser(new Point(3, "p"))));
    io("not-serializable", () -> hex(ser(new HoldsNot())));
    io("not-serializable-plain", () -> ser(new NotSer()));
    io("arrays", () -> { Object o = de(ser(new int[] {1, 2, 3}, new String[] {"a", null}, new long[0], new double[][] {{1}, {2, 3}}, new boolean[] {true, false}, new char[] {'a'}, new short[] {7}, new float[] {1.5f}, new byte[] {1, 2})); return o.getClass().getSimpleName(); });
    io("arrays-bytes", () -> hex(ser(new int[] {1, 2, 3}, new String[] {"a", null}, new double[][] {{1}, {2, 3}}, new byte[] {1, 2})));
    io("multi", () -> { try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(ser("a", 1, "a", new int[] {4})))) { return in.readObject() + " " + in.readObject() + " " + in.readObject() + " " + Arrays.toString((int[]) in.readObject()); } });
    io("shared-ref", () -> { String s = "shared"; Object[] arr = {s, s}; Object[] r = de(ser((Object) arr)); return r[0] == r[1]; });
    io("unshared", () -> { ByteArrayOutputStream bo = new ByteArrayOutputStream(); try (ObjectOutputStream o = new ObjectOutputStream(bo)) { String s = "u"; o.writeUnshared(s); o.writeObject(s); o.writeObject(s); } return hex(bo.toByteArray()); });
    io("data-stream", () -> { ByteArrayOutputStream bo = new ByteArrayOutputStream(); try (ObjectOutputStream o = new ObjectOutputStream(bo)) { o.writeBoolean(true); o.writeByte(1); o.writeShort(2); o.writeChar('c'); o.writeInt(3); o.writeLong(4); o.writeFloat(5f); o.writeDouble(6d); o.writeUTF("ué中"); o.writeBytes("ab"); o.writeChars("cd"); o.writeObject("o"); o.write(new byte[300]); o.writeInt(9); }
      try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bo.toByteArray()))) { String r = "" + in.readBoolean() + in.readByte() + in.readShort() + in.readChar() + in.readInt() + in.readLong() + in.readFloat() + in.readDouble() + in.readUTF() + (char) in.readByte() + (char) in.readByte() + in.readChar() + in.readChar() + in.readObject(); byte[] b = new byte[300]; in.readFully(b); return r + in.readInt() + hex(bo.toByteArray()).length(); } });
    io("optional-data", () -> { ByteArrayOutputStream bo = new ByteArrayOutputStream(); try (ObjectOutputStream o = new ObjectOutputStream(bo)) { o.writeInt(1); o.writeObject("x"); }
      try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bo.toByteArray()))) { try { in.readObject(); return "no"; } catch (OptionalDataException e) { return e.length + " " + e.eof; } } });
    io("bad-header", () -> new ObjectInputStream(new ByteArrayInputStream(new byte[] {1, 2, 3, 4})));
    io("eof", () -> new ObjectInputStream(new ByteArrayInputStream(new byte[0])));
    io("truncated", () -> { byte[] b = ser(new Plain()); return de(Arrays.copyOf(b, b.length - 5)); });
    io("class-obj", () -> { Object o = de(ser(String.class, int.class, Plain.class)); return o; });
    io("desc-lookup", () -> { ObjectStreamClass d = ObjectStreamClass.lookup(Plain.class); StringBuilder sb = new StringBuilder(d.getName() + " " + d.getSerialVersionUID() + " " + d.forClass().getSimpleName()); for (ObjectStreamField f : d.getFields()) sb.append(" ").append(f.getName()).append(":").append(f.getTypeCode()).append(f.isPrimitive()).append(f.getTypeString()); return sb + " " + d + " " + ObjectStreamClass.lookup(NotSer.class) + " " + ObjectStreamClass.lookupAny(NotSer.class).getSerialVersionUID(); });
    io("field-api", () -> { ObjectStreamField a = new ObjectStreamField("a", int.class), b = new ObjectStreamField("b", String.class); return a + " " + b + " " + a.compareTo(b) + " " + b.compareTo(a) + " " + a.getType() + " " + b.getTypeCode() + " " + b.getOffset(); });
    io("filter-reject", () -> { try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(ser(new Plain())))) { in.setObjectInputFilter(ObjectInputFilter.Config.createFilter("!SerializationModern$Plain")); return in.readObject(); } });
    io("filter-allow", () -> { try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(ser(new Plain())))) { in.setObjectInputFilter(ObjectInputFilter.Config.createFilter("SerializationModern$Plain;java.lang.*;*")); return in.readObject().getClass().getSimpleName() + " " + (in.getObjectInputFilter() != null); } });
    io("filter-limits", () -> { try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(ser(new int[100])))) { in.setObjectInputFilter(ObjectInputFilter.Config.createFilter("maxarray=10")); return in.readObject(); } });
    io("filter-api", () -> ObjectInputFilter.allowFilter(c -> c == String.class, ObjectInputFilter.Status.REJECTED).checkInput(new ObjectInputFilter.FilterInfo() { public Class<?> serialClass() { return String.class; } public long arrayLength() { return -1; } public long depth() { return 1; } public long references() { return 1; } public long streamBytes() { return 1; } }));
    io("filter-pattern-bad", () -> ObjectInputFilter.Config.createFilter("maxdepth=x"));
    io("string-long", () -> { String s = "x".repeat(70000); String r = de(ser(s)); return r.length() + " " + hex(ser(s)).length(); });
    io("string-utf", () -> { String s = "a\u0000bé中😀"; return de(ser(s)).equals(s) + " " + hex(ser(s)); });
    io("wrapper-boxes", () -> { Object[] o = de(ser((Object) new Object[] {1, 2L, 3.5, 'c', (short) 4, (byte) 5, true, 1.5f, "s", Color.RED})); return Arrays.toString(o); });
    io("wrapper-bytes", () -> hex(ser(Integer.valueOf(1), Long.valueOf(2), Boolean.TRUE, Double.valueOf(1.5))));
    io("serialpermission", () -> new SerializablePermission("enableSubstitution").getName() + " " + ObjectStreamConstants.STREAM_MAGIC + " " + ObjectStreamConstants.baseWireHandle);
    io("reset", () -> { ByteArrayOutputStream bo = new ByteArrayOutputStream(); try (ObjectOutputStream o = new ObjectOutputStream(bo)) { String s = "r"; o.writeObject(s); o.reset(); o.writeObject(s); } return hex(bo.toByteArray()); });
    io("reset-read", () -> { ByteArrayOutputStream bo = new ByteArrayOutputStream(); try (ObjectOutputStream o = new ObjectOutputStream(bo)) { String s = "r"; o.writeObject(s); o.reset(); o.writeObject(s); } try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bo.toByteArray()))) { return in.readObject() + "" + in.readObject(); } });
    io("proxy", () -> { Runnable r = (Runnable) java.lang.reflect.Proxy.newProxyInstance(SerializationModern.class.getClassLoader(), new Class<?>[] {Runnable.class}, new Handler()); Runnable back = de(ser(r)); return java.lang.reflect.Proxy.isProxyClass(back.getClass()); });
    io("exception", () -> { Exception e = de(ser(new IllegalStateException("boom", new RuntimeException("cause")))); return e.getMessage() + " " + e.getCause().getMessage(); });
    io("available", () -> { try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(ser("x")))) { return in.available() >= 0; } });
  }

  static class Handler implements java.lang.reflect.InvocationHandler, Serializable {
    private static final long serialVersionUID = 9L;
    public Object invoke(Object p, java.lang.reflect.Method m, Object[] a) { return null; }
  }
}
