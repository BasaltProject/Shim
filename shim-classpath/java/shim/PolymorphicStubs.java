/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package shim;

import static shim.Stream.set4;
import static shim.Stream.write1;
import static shim.Stream.write2;
import static shim.Stream.write4;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.lang.invoke.WrongMethodTypeException;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import shim.ConstantPool.PoolEntry;

public final class PolymorphicStubs {
  private static final int sipush = 0x11;
  private static final int iconst_0 = 0x03;
  private static final int checkcast = 0xc0;
  private static final int ldc_w = 0x13;

  private static final Map<ClassLoader, Map<String, VMMethod>> cache = new IdentityHashMap<>();
  private static int nextNumber;

  private PolymorphicStubs() { }

  public static Object call(MethodHandle handle, String descriptor, Class<?> origin, Object[] arguments,
                            boolean exact) throws Throwable {
    MethodType site = MethodType.fromMethodDescriptorString(descriptor, origin.getClassLoader());
    MethodType actual = handle.type();
    if (!actual.equals(site)) {
      if (exact) {
        throw new WrongMethodTypeException("handle's method type " + actual + " but found " + site);
      }
      handle = handle.asType(site);
    }
    return handle.invokeWithArguments(arguments);
  }

  public static Object get(VarHandle h, Object[] a) { return h.get(a); }
  public static Object getVolatile(VarHandle h, Object[] a) { return h.getVolatile(a); }
  public static Object getOpaque(VarHandle h, Object[] a) { return h.getOpaque(a); }
  public static Object getAcquire(VarHandle h, Object[] a) { return h.getAcquire(a); }
  public static Object compareAndExchange(VarHandle h, Object[] a) { return h.compareAndExchange(a); }
  public static Object compareAndExchangeAcquire(VarHandle h, Object[] a) { return h.compareAndExchangeAcquire(a); }
  public static Object compareAndExchangeRelease(VarHandle h, Object[] a) { return h.compareAndExchangeRelease(a); }
  public static Object getAndSet(VarHandle h, Object[] a) { return h.getAndSet(a); }
  public static Object getAndSetAcquire(VarHandle h, Object[] a) { return h.getAndSetAcquire(a); }
  public static Object getAndSetRelease(VarHandle h, Object[] a) { return h.getAndSetRelease(a); }
  public static Object getAndAdd(VarHandle h, Object[] a) { return h.getAndAdd(a); }
  public static Object getAndAddAcquire(VarHandle h, Object[] a) { return h.getAndAddAcquire(a); }
  public static Object getAndAddRelease(VarHandle h, Object[] a) { return h.getAndAddRelease(a); }
  public static Object getAndBitwiseOr(VarHandle h, Object[] a) { return h.getAndBitwiseOr(a); }
  public static Object getAndBitwiseOrAcquire(VarHandle h, Object[] a) { return h.getAndBitwiseOrAcquire(a); }
  public static Object getAndBitwiseOrRelease(VarHandle h, Object[] a) { return h.getAndBitwiseOrRelease(a); }
  public static Object getAndBitwiseAnd(VarHandle h, Object[] a) { return h.getAndBitwiseAnd(a); }
  public static Object getAndBitwiseAndAcquire(VarHandle h, Object[] a) { return h.getAndBitwiseAndAcquire(a); }
  public static Object getAndBitwiseAndRelease(VarHandle h, Object[] a) { return h.getAndBitwiseAndRelease(a); }
  public static Object getAndBitwiseXor(VarHandle h, Object[] a) { return h.getAndBitwiseXor(a); }
  public static Object getAndBitwiseXorAcquire(VarHandle h, Object[] a) { return h.getAndBitwiseXorAcquire(a); }
  public static Object getAndBitwiseXorRelease(VarHandle h, Object[] a) { return h.getAndBitwiseXorRelease(a); }
  public static void set(VarHandle h, Object[] a) { h.set(a); }
  public static void setVolatile(VarHandle h, Object[] a) { h.setVolatile(a); }
  public static void setOpaque(VarHandle h, Object[] a) { h.setOpaque(a); }
  public static void setRelease(VarHandle h, Object[] a) { h.setRelease(a); }
  public static boolean compareAndSet(VarHandle h, Object[] a) { return h.compareAndSet(a); }
  public static boolean weakCompareAndSetPlain(VarHandle h, Object[] a) { return h.weakCompareAndSetPlain(a); }
  public static boolean weakCompareAndSet(VarHandle h, Object[] a) { return h.weakCompareAndSet(a); }
  public static boolean weakCompareAndSetAcquire(VarHandle h, Object[] a) { return h.weakCompareAndSetAcquire(a); }
  public static boolean weakCompareAndSetRelease(VarHandle h, Object[] a) { return h.weakCompareAndSetRelease(a); }

  public static Object varHandle(VarHandle handle, String mode, Object[] arguments) {
    switch (mode) {
    case "get": return handle.get(arguments);
    case "set": handle.set(arguments); return null;
    case "getVolatile": return handle.getVolatile(arguments);
    case "setVolatile": handle.setVolatile(arguments); return null;
    case "getOpaque": return handle.getOpaque(arguments);
    case "setOpaque": handle.setOpaque(arguments); return null;
    case "getAcquire": return handle.getAcquire(arguments);
    case "setRelease": handle.setRelease(arguments); return null;
    case "compareAndSet": return handle.compareAndSet(arguments);
    case "compareAndExchange": return handle.compareAndExchange(arguments);
    case "compareAndExchangeAcquire": return handle.compareAndExchangeAcquire(arguments);
    case "compareAndExchangeRelease": return handle.compareAndExchangeRelease(arguments);
    case "weakCompareAndSetPlain": return handle.weakCompareAndSetPlain(arguments);
    case "weakCompareAndSet": return handle.weakCompareAndSet(arguments);
    case "weakCompareAndSetAcquire": return handle.weakCompareAndSetAcquire(arguments);
    case "weakCompareAndSetRelease": return handle.weakCompareAndSetRelease(arguments);
    case "getAndSet": return handle.getAndSet(arguments);
    case "getAndSetAcquire": return handle.getAndSetAcquire(arguments);
    case "getAndSetRelease": return handle.getAndSetRelease(arguments);
    case "getAndAdd": return handle.getAndAdd(arguments);
    case "getAndAddAcquire": return handle.getAndAddAcquire(arguments);
    case "getAndAddRelease": return handle.getAndAddRelease(arguments);
    case "getAndBitwiseOr": return handle.getAndBitwiseOr(arguments);
    case "getAndBitwiseOrAcquire": return handle.getAndBitwiseOrAcquire(arguments);
    case "getAndBitwiseOrRelease": return handle.getAndBitwiseOrRelease(arguments);
    case "getAndBitwiseAnd": return handle.getAndBitwiseAnd(arguments);
    case "getAndBitwiseAndAcquire": return handle.getAndBitwiseAndAcquire(arguments);
    case "getAndBitwiseAndRelease": return handle.getAndBitwiseAndRelease(arguments);
    case "getAndBitwiseXor": return handle.getAndBitwiseXor(arguments);
    case "getAndBitwiseXorAcquire": return handle.getAndBitwiseXorAcquire(arguments);
    case "getAndBitwiseXorRelease": return handle.getAndBitwiseXorRelease(arguments);
    default: throw new UnsupportedOperationException(mode);
    }
  }

  public static VMMethod stub(ClassLoader loader, String name, String descriptor) {
    boolean exact = name.equals("invokeExact");
    String key = name + descriptor;
    synchronized (cache) {
      Map<String, VMMethod> perLoader = cache.get(loader);
      if (perLoader == null) {
        perLoader = new HashMap<>();
        cache.put(loader, perLoader);
      }
      VMMethod method = perLoader.get(key);
      if (method == null) {
        method = define(loader, name, descriptor, exact);
        perLoader.put(key, method);
      }
      return method;
    }
  }

  private static VMMethod define(ClassLoader loader, String name, String descriptor, boolean exact) {
    String className = "shim/PolymorphicStub$" + nextNumber++;
    boolean variable = !name.equals("invoke") && !name.equals("invokeExact");
    String stubSpec = (variable ? "(Ljava/lang/invoke/VarHandle;" : "(Ljava/lang/invoke/MethodHandle;")
        + descriptor.substring(1);
    try {
      List<PoolEntry> pool = new ArrayList<>();
      byte[] code = makeCode(pool, className, name, descriptor, exact, variable);
      int methodName = ConstantPool.addUtf8(pool, "call");
      int methodSpec = ConstantPool.addUtf8(pool, stubSpec);
      int nameIndex = ConstantPool.addClass(pool, className);
      int superIndex = ConstantPool.addClass(pool, "java/lang/Object");
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      Assembler.writeClass(out, pool, nameIndex, superIndex, new int[0], new Assembler.FieldData[0],
                           new Assembler.MethodData[] {
                             new Assembler.MethodData(Assembler.ACC_PUBLIC | Assembler.ACC_STATIC, methodName,
                                                      methodSpec, code)});
      byte[] data = out.toByteArray();
      VMClass defined = Classes.defineVMClass(loader, data, 0, data.length);
      for (VMMethod candidate : defined.methodTable) {
        if (Classes.toString(candidate.name).equals("call")) return candidate;
      }
      throw new AssertionError();
    } catch (IOException e) {
      throw new AssertionError(e);
    }
  }

  private static String[] parameters(String descriptor) {
    List<String> result = new ArrayList<>();
    int i = 1;
    while (descriptor.charAt(i) != ')') {
      int start = i;
      while (descriptor.charAt(i) == '[') ++i;
      if (descriptor.charAt(i) == 'L') i = descriptor.indexOf(';', i);
      result.add(descriptor.substring(start, ++i));
    }
    result.add(descriptor.substring(i + 1));
    return result.toArray(new String[0]);
  }

  private static String wrapper(char type) {
    switch (type) {
    case 'Z': return "java/lang/Boolean";
    case 'B': return "java/lang/Byte";
    case 'C': return "java/lang/Character";
    case 'S': return "java/lang/Short";
    case 'I': return "java/lang/Integer";
    case 'J': return "java/lang/Long";
    case 'F': return "java/lang/Float";
    case 'D': return "java/lang/Double";
    default: throw new AssertionError();
    }
  }

  private static String unboxName(char type) {
    switch (type) {
    case 'Z': return "booleanValue";
    case 'B': return "byteValue";
    case 'C': return "charValue";
    case 'S': return "shortValue";
    case 'I': return "intValue";
    case 'J': return "longValue";
    case 'F': return "floatValue";
    case 'D': return "doubleValue";
    default: throw new AssertionError();
    }
  }

  private static int loadOpcode(char type) {
    switch (type) {
    case 'J': return 0x16;
    case 'F': return 0x17;
    case 'D': return 0x18;
    case 'L':
    case '[': return 0x19;
    default: return 0x15;
    }
  }

  private static int returnOpcode(char type) {
    switch (type) {
    case 'J': return 0xad;
    case 'F': return 0xae;
    case 'D': return 0xaf;
    case 'L':
    case '[': return 0xb0;
    case 'V': return 0xb1;
    default: return 0xac;
    }
  }

  private static byte[] makeCode(List<PoolEntry> pool, String className, String name, String descriptor,
                                 boolean exact, boolean variable)
    throws IOException
  {
    String[] types = parameters(descriptor);
    int count = types.length - 1;
    int footprint = 1;
    for (int i = 0; i < count; ++i) {
      char c = types[i].charAt(0);
      footprint += c == 'J' || c == 'D' ? 2 : 1;
    }
    if (footprint > 255) throw new IllegalArgumentException("Too many parameters");

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    write2(out, 10);
    write2(out, footprint);
    write4(out, 0);

    write1(out, 0x2a);
    if (!variable) {
      write1(out, ldc_w);
      write2(out, ConstantPool.addString(pool, descriptor) + 1);
      write1(out, ldc_w);
      write2(out, ConstantPool.addClass(pool, className) + 1);
    }
    write1(out, sipush);
    write2(out, count);
    write1(out, 0xbd);
    write2(out, ConstantPool.addClass(pool, "java/lang/Object") + 1);

    int local = 1;
    for (int i = 0; i < count; ++i) {
      char c = types[i].charAt(0);
      write1(out, 0x59);
      write1(out, sipush);
      write2(out, i);
      write1(out, loadOpcode(c));
      write1(out, local);
      if (c != 'L' && c != '[') {
        write1(out, 0xb8);
        write2(out, ConstantPool.addMethodRef(pool, wrapper(c), "valueOf",
                                              "(" + c + ")L" + wrapper(c) + ";") + 1);
      }
      write1(out, 0x53);
      local += c == 'J' || c == 'D' ? 2 : 1;
    }

    if (variable) {
      boolean returnsVoid = name.startsWith("set");
      boolean returnsBoolean = name.equals("compareAndSet") || name.startsWith("weakCompareAndSet");
      write1(out, 0xb8);
      write2(out, ConstantPool.addMethodRef(pool, "shim/PolymorphicStubs", name,
                                            "(Ljava/lang/invoke/VarHandle;[Ljava/lang/Object;)"
                                            + (returnsVoid ? "V" : returnsBoolean ? "Z" : "Ljava/lang/Object;")) + 1);
      if (returnsVoid) {
        write1(out, 0x01);
      } else if (returnsBoolean) {
        write1(out, 0xb8);
        write2(out, ConstantPool.addMethodRef(pool, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;") + 1);
      }
    } else {
      write1(out, iconst_0 + (exact ? 1 : 0));
      write1(out, 0xb8);
      write2(out, ConstantPool.addMethodRef(pool, "shim/PolymorphicStubs", "call",
                                            "(Ljava/lang/invoke/MethodHandle;Ljava/lang/String;Ljava/lang/Class;"
                                            + "[Ljava/lang/Object;Z)Ljava/lang/Object;") + 1);
    }

    String result = types[count];
    char r = result.charAt(0);
    if (r == 'V') {
      write1(out, 0x57);
    } else if (r != 'L' && r != '[') {
      write1(out, checkcast);
      write2(out, ConstantPool.addClass(pool, wrapper(r)) + 1);
      write1(out, 0xb6);
      write2(out, ConstantPool.addMethodRef(pool, wrapper(r), unboxName(r), "()" + r) + 1);
    }
    write1(out, returnOpcode(r));

    write2(out, 0);
    write2(out, 0);

    byte[] bytes = out.toByteArray();
    set4(bytes, 4, bytes.length - 12);
    return bytes;
  }
}
