/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

public class BooleanStorage {
  public static class Remote { public static int field; }
  public static class Raw {
    public int field;
    public volatile int changing;
    public static int global;
    public static volatile int shared;
    public int instance(int value) { field = value; return field; }
    public int volatileInstance(int value) { changing = value; return changing; }
    public static int statics(int value) { global = value; return global; }
    public static int volatileStatics(int value) { shared = value; return shared; }
    public static int array(int[] array, int index, int value) { array[index] = value; return array[index]; }
    public static int bytes(byte[] array, int index, int value) { array[index] = (byte) value; return array[index]; }
    public static int remote(int value) { Remote.field = value; return Remote.field; }
    public static int narrow(int value) { return value; }
    public static int call(int value) { return narrow(value); }
    public static int narrowByte(int value) { return value; }
    public static int callByte(int value) { return narrowByte(value); }
    public static int narrowChar(int value) { return value; }
    public static int callChar(int value) { return narrowChar(value); }
    public static int narrowShort(int value) { return value; }
    public static int callShort(int value) { return narrowShort(value); }
    public static synchronized int narrowSync(int value) { return value; }
    public static int callSync(int value) { return narrowSync(value); }
    public static int accept(int value) { return value; }
    public static int acceptCaller(int value) { return accept(value); }
  }

  private static int u2(byte[] bytes, int offset) {
    return ((bytes[offset] & 255) << 8) | (bytes[offset + 1] & 255);
  }
  private static int u4(byte[] bytes, int offset) {
    return (u2(bytes, offset) << 16) | u2(bytes, offset + 2);
  }
  private static void put2(byte[] bytes, int offset, int value) {
    bytes[offset] = (byte) (value >>> 8); bytes[offset + 1] = (byte) value;
  }

  private static byte[] data(String nested) throws Exception {
    InputStream in = BooleanStorage.class.getResourceAsStream("/BooleanStorage$" + nested + ".class");
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    byte[] buffer = new byte[1024];
    for (int n; (n = in.read(buffer)) != -1;) out.write(buffer, 0, n);
    in.close();
    byte[] bytes = out.toByteArray();
    int count = u2(bytes, 8), offset = 10;
    String[] text = new String[count];
    int[] entries = new int[count];
    for (int i = 1; i < count; ++i) {
      entries[i] = offset;
      int tag = bytes[offset++] & 255;
      if (tag == 1) {
        int length = u2(bytes, offset); offset += 2;
        text[i] = new String(bytes, offset, length, "UTF-8");
        if (text[i].equals("I")) bytes[offset] = 'Z';
        if (text[i].equals("([III)I")) bytes[offset + 2] = 'Z';
        offset += length;
      } else if (tag == 7 || tag == 8 || tag == 16) offset += 2;
      else if (tag == 15) offset += 3;
      else if (tag == 5 || tag == 6) { offset += 8; ++i; }
      else offset += 4;
    }
    int end = offset;
    String[] descriptors = {"(I)Z", "(I)B", "(I)C", "(I)S", "(Z)I"};
    int added = 7 * descriptors.length;
    byte[] result = new byte[bytes.length + added];
    System.arraycopy(bytes, 0, result, 0, end);
    for (int i = 0; i < descriptors.length; ++i) {
      int at = end + i * 7;
      result[at] = 1; result[at + 2] = 4;
      for (int j = 0; j < 4; ++j) result[at + 3 + j] = (byte) descriptors[i].charAt(j);
    }
    System.arraycopy(bytes, end, result, end + added, bytes.length - end);
    bytes = result;
    put2(bytes, 8, count + descriptors.length);
    for (int i = 1; i < count; ++i) {
      int at = entries[i];
      if (at != 0 && bytes[at] == 12) {
        int kind = descriptor(text[u2(bytes, at + 1)]);
        if (kind >= 0) put2(bytes, at + 3, count + kind);
      }
    }
    offset = end + added + 6;
    int interfaces = u2(bytes, offset); offset += 2 + 2 * interfaces;
    int fields = u2(bytes, offset); offset += 2;
    for (int i = 0; i < fields; ++i) offset = skip(bytes, offset, null, text, count);
    int methods = u2(bytes, offset); offset += 2;
    for (int i = 0; i < methods; ++i) offset = skip(bytes, offset, text[u2(bytes, offset + 2)], text, count);
    return bytes;
  }

  private static int descriptor(String method) {
    if ("narrow".equals(method) || "narrowSync".equals(method)) return 0;
    if ("narrowByte".equals(method)) return 1;
    if ("narrowChar".equals(method)) return 2;
    if ("narrowShort".equals(method)) return 3;
    if ("accept".equals(method)) return 4;
    return -1;
  }

  private static int skip(byte[] bytes, int offset, String method, String[] text, int descriptor) {
    int kind = descriptor(method);
    if (kind >= 0) put2(bytes, offset + 4, descriptor + kind);
    int attributes = u2(bytes, offset + 6); offset += 8;
    for (int j = 0; j < attributes; ++j) {
      int name = u2(bytes, offset), length = u4(bytes, offset + 2); offset += 6;
      if ("array".equals(method) && "Code".equals(text[name])) {
        int codeLength = u4(bytes, offset + 4);
        for (int k = offset + 8; k < offset + 8 + codeLength; ++k) {
          if (bytes[k] == 79) bytes[k] = 84;
          else if (bytes[k] == 46) bytes[k] = 51;
        }
      }
      offset += length;
    }
    return offset;
  }

  private static class Loader extends ClassLoader {
    Loader() { super(BooleanStorage.class.getClassLoader()); }
    Class<?> load(byte[] data) { return defineClass(null, data, 0, data.length); }
    protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
      if (!name.equals("BooleanStorage$Remote")) return super.loadClass(name, resolve);
      Class<?> type = findLoadedClass(name);
      if (type == null) {
        try { type = load(data("Remote")); }
        catch (Exception e) { throw new ClassNotFoundException(name, e); }
      }
      if (resolve) resolveClass(type);
      return type;
    }
  }

  public static void main(String[] args) throws Exception {
    Class<?> type = new Loader().load(data("Raw"));
    Object owner = type.getDeclaredConstructor().newInstance();
    int[] values = {Integer.MIN_VALUE, -257, -256, -255, -129, -128, -3, -2, -1,
      0, 1, 2, 3, 127, 128, 129, 254, 255, 256, 257, Integer.MAX_VALUE};
    for (String name : new String[] {"instance", "volatileInstance", "statics", "volatileStatics", "remote", "call", "narrow",
        "callByte", "callChar", "callShort", "narrowByte", "narrowChar", "narrowShort",
        "narrowSync", "callSync", "acceptCaller"}) {
      Method method = type.getMethod(name, int.class);
      for (int value : values) {
        Object result = method.invoke(owner, value);
        if (result instanceof Character) result = Integer.valueOf((Character) result);
        System.out.println(name + ":" + value + ":" + result);
      }
    }
    Method array = type.getMethod("array", boolean[].class, int.class, int.class);
    Method bytes = type.getMethod("bytes", byte[].class, int.class, int.class);
    boolean[] booleans = new boolean[1];
    byte[] signed = new byte[1];
    for (int value : values) {
      System.out.println("boolean-array:" + value + ":" + array.invoke(null, booleans, 0, value) + ":" + booleans[0]);
      System.out.println("byte-array:" + value + ":" + bytes.invoke(null, signed, 0, value) + ":" + signed[0]);
    }
    for (Method method : new Method[] {array, bytes}) {
      for (int index : new int[] {-1, 0, 1}) {
        for (boolean missing : new boolean[] {false, true}) {
          Object target = missing ? null : (method == array ? booleans : signed);
          try {
            System.out.println("bounds:" + method.invoke(null, target, index, 3));
          } catch (InvocationTargetException e) {
            System.out.println("bounds:" + e.getCause().getClass().getSimpleName());
          }
        }
      }
    }
  }
}
