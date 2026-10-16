/*
 * Based on Avian (https://github.com/ReadyTalk/avian).
 * Original work Copyright (c) 2008-2015, Avian Contributors.
 * Original work licensed under ISC; see LICENSE.txt:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE.txt
 *
 * Modifications Copyright (c) 2026 BasaltMC.
 * Modifications licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: ISC AND MIT
 */

/* Copyright (c) 2008-2015, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.lang.reflect;

import static shim.Stream.write1;
import static shim.Stream.write2;
import static shim.Stream.write4;
import static shim.Stream.set4;
import static shim.Assembler.*;

import shim.SystemClassLoader;
import shim.Classes;

import shim.ConstantPool;
import shim.ConstantPool.PoolEntry;

import shim.Assembler;
import shim.Assembler.FieldData;
import shim.Assembler.MethodData;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Proxy {
  private static int nextNumber;

  protected InvocationHandler h;
  protected final static Map<Class, Method[]> methodRefsMap =
    new HashMap<Class, Method[]>();
  protected final Method[] methodRefs;

  public Proxy() {
    methodRefs = methodRefsMap.get(getClass());
  }

  public static Class getProxyClass(ClassLoader loader,
                                    Class ... interfaces)
  {
    for (Class c: interfaces) {
      if (! c.isInterface()) {
        throw new IllegalArgumentException();
      }
    }

    int number;
    synchronized (Proxy.class) {
      number = nextNumber++;
    }

    try {
      return makeClass(loader, interfaces, "Proxy-" + number);
    } catch (IOException e) {
      AssertionError error = new AssertionError();
      error.initCause(e);
      throw error;      
    }
  }

  public static boolean isProxyClass(Class c) {
    if (c == null) {
      throw new NullPointerException();
    }
    return Proxy.class.isAssignableFrom(c) && c != Proxy.class;
  }

  public static InvocationHandler getInvocationHandler(Object proxy) {
    if (proxy == null) {
      throw new NullPointerException();
    }
    if (!isProxyClass(proxy.getClass())) {
      throw new IllegalArgumentException("not a proxy instance");
    }
    return ((Proxy) proxy).h;
  }

  private static byte[] makeInvokeCode(List<PoolEntry> pool,
                                       String className,
                                       byte[] spec,
                                       int parameterCount,
                                       int parameterFootprint,
                                       int index)
    throws IOException
  {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    write2(out, 8); // max stack
    write2(out, parameterFootprint); // max locals
    write4(out, 0); // length (we'll set the real value later)

    write1(out, aload_0);
    write1(out, getfield);
    write2(out, ConstantPool.addFieldRef
           (pool, "java/lang/reflect/Proxy",
            "h", "Ljava/lang/reflect/InvocationHandler;") + 1);

    write1(out, aload_0);
    
    write1(out, aload_0);
    write1(out, getfield);
    write2(out, ConstantPool.addFieldRef
           (pool, className,
            "methodRefs", "[Ljava/lang/reflect/Method;") + 1);
    write1(out, ldc_w);
    write2(out, ConstantPool.addInteger(pool, index) + 1);
    write1(out, aaload);

    write1(out, ldc_w);
    write2(out, ConstantPool.addInteger(pool, parameterCount) + 1);
    write1(out, anewarray);
    write2(out, ConstantPool.addClass(pool, "java/lang/Object") + 1);

    int ai = 0;
    int si;
    for (si = 1; spec[si] != ')'; ++si) {
      write1(out, dup);

      write1(out, ldc_w);
      write2(out, ConstantPool.addInteger(pool, ai) + 1);
    
      switch (spec[si]) {
      case 'L':
        ++ si;
        while (spec[si] != ';') ++si;
      
        write1(out, aload);
        write1(out, ai + 1);
        break;

      case '[':
        ++ si;
        while (spec[si] == '[') ++si;
        switch (spec[si]) {
        case 'L':
          ++ si;
          while (spec[si] != ';') ++si;
          break;

        default:
          break;
        }

        write1(out, aload);
        write1(out, ai + 1);
        break;

      case 'Z':
        write1(out, iload);
        write1(out, ai + 1);

        write1(out, invokestatic);
        write2(out, ConstantPool.addMethodRef
               (pool, "java/lang/Boolean",
                "valueOf", "(Z)Ljava/lang/Boolean;") + 1);
        break;

      case 'B':
        write1(out, iload);
        write1(out, ai + 1);

        write1(out, invokestatic);
        write2(out, ConstantPool.addMethodRef
               (pool, "java/lang/Byte",
                "valueOf", "(B)Ljava/lang/Byte;") + 1);
        break;

      case 'S':
        write1(out, iload);
        write1(out, ai + 1);

        write1(out, invokestatic);
        write2(out, ConstantPool.addMethodRef
               (pool, "java/lang/Short",
                "valueOf", "(S)Ljava/lang/Short;") + 1);
        break;

      case 'C':
        write1(out, iload);
        write1(out, ai + 1);

        write1(out, invokestatic);
        write2(out, ConstantPool.addMethodRef
               (pool, "java/lang/Character",
                "valueOf", "(C)Ljava/lang/Character;") + 1);
        break;

      case 'I':
        write1(out, iload);
        write1(out, ai + 1);

        write1(out, invokestatic);
        write2(out, ConstantPool.addMethodRef
               (pool, "java/lang/Integer",
                "valueOf", "(I)Ljava/lang/Integer;") + 1);
        break;

      case 'F':
        write1(out, fload);
        write1(out, ai + 1);

        write1(out, invokestatic);
        write2(out, ConstantPool.addMethodRef
               (pool, "java/lang/Float",
                "valueOf", "(F)Ljava/lang/Float;") + 1);
        break;

      case 'J':
        write1(out, lload);
        write1(out, ai + 1);

        write1(out, invokestatic);
        write2(out, ConstantPool.addMethodRef
               (pool, "java/lang/Long",
                "valueOf", "(J)Ljava/lang/Long;") + 1);
        ++ ai;
        break;

      case 'D':
        write1(out, dload);
        write1(out, ai + 1);

        write1(out, invokestatic);
        write2(out, ConstantPool.addMethodRef
               (pool, "java/lang/Double",
                "valueOf", "(D)Ljava/lang/Double;") + 1);
        ++ ai;
        break;

      default: throw new IllegalArgumentException();
      }

      write1(out, aastore);

      ++ ai;
    }

    write1(out, invokeinterface);
    write2(out, ConstantPool.addInterfaceMethodRef
           (pool, "java/lang/reflect/InvocationHandler",
            "invoke",
            "(Ljava/lang/Object;"
            + "Ljava/lang/reflect/Method;"
            + "[Ljava/lang/Object;)"
            + "Ljava/lang/Object;") + 1);
    write1(out, 4);
    write1(out, 0);

    switch (spec[si + 1]) {
    case 'L':
    case '[':
      write1(out, areturn);
      break;

    case 'Z':
      write1(out, invokevirtual);
      write2(out, ConstantPool.addMethodRef
             (pool, "java/lang/Boolean", "booleanValue", "()Z") + 1);
      write1(out, ireturn);
      break;

    case 'B':
      write1(out, invokevirtual);
      write2(out, ConstantPool.addMethodRef
             (pool, "java/lang/Byte", "byteValue", "()B") + 1);
      write1(out, ireturn);
      break;

    case 'C':
      write1(out, invokevirtual);
      write2(out, ConstantPool.addMethodRef
             (pool, "java/lang/Character", "charValue", "()C") + 1);
      write1(out, ireturn);
      break;

    case 'S':
      write1(out, invokevirtual);
      write2(out, ConstantPool.addMethodRef
             (pool, "java/lang/Short", "shortValue", "()S") + 1);
      write1(out, ireturn);
      break;

    case 'I':
      write1(out, invokevirtual);
      write2(out, ConstantPool.addMethodRef
             (pool, "java/lang/Integer", "intValue", "()I") + 1);
      write1(out, ireturn);
      break;

    case 'F':
      write1(out, invokevirtual);
      write2(out, ConstantPool.addMethodRef
             (pool, "java/lang/Float", "floatValue", "()F") + 1);
      write1(out, freturn);
      break;

    case 'J':
      write1(out, invokevirtual);
      write2(out, ConstantPool.addMethodRef
             (pool, "java/lang/Long", "longValue", "()J") + 1);
      write1(out, lreturn);
      break;

    case 'D':
      write1(out, invokevirtual);
      write2(out, ConstantPool.addMethodRef
             (pool, "java/lang/Double", "doubleValue", "()D") + 1);
      write1(out, dreturn);
      break;

    case 'V':
      write1(out, pop);
      write1(out, return_);
      break;

    default: throw new IllegalArgumentException();
    }

    write2(out, 0); // exception handler table length
    write2(out, 0); // attribute count

    byte[] result = out.toByteArray();
    set4(result, 4, result.length - 12);

    return result;
  }

  private static byte[] makeConstructorCode(List<PoolEntry> pool)
    throws IOException
  {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    write2(out, 2); // max stack
    write2(out, 2); // max locals
    write4(out, 10); // length

    write1(out, aload_0);
    write1(out, invokespecial);
    write2(out, ConstantPool.addMethodRef
           (pool, "java/lang/reflect/Proxy",
            "<init>", "()V") + 1);

    write1(out, aload_0);
    write1(out, aload_1);
    write1(out, putfield);
    write2(out, ConstantPool.addFieldRef
           (pool, "java/lang/reflect/Proxy",
            "h", "Ljava/lang/reflect/InvocationHandler;") + 1);
    write1(out, return_);

    write2(out, 0); // exception handler table length
    write2(out, 0); // attribute count

    return out.toByteArray();
  }

  private static Class makeClass(ClassLoader loader,
                                 Class[] interfaces,
                                 String name)
    throws IOException
  {
    List<PoolEntry> pool = new ArrayList();

    int[] interfaceIndexes = new int[interfaces.length];
    for (int i = 0; i < interfaces.length; ++i) {
      interfaceIndexes[i] = ConstantPool.addClass
        (pool, interfaces[i].getName().replace('.', '/'));
    }

    Set<String> specs = new HashSet<String>();
    List<MethodData> methodTable = new ArrayList<MethodData>();
    List<Method> refs = new ArrayList<Method>();
    for (Class c: interfaces) {
      shim.VMMethod[] ivtable = SystemClassLoader.vmClass(c).virtualTable;
      if (ivtable != null) {
        for (shim.VMMethod m: ivtable) {
          String spec = Classes.toString(m.name) + Classes.toString(m.spec);
          if (specs.contains(spec)) {
            continue;
          }
          specs.add(spec);
          methodTable.add(new MethodData
            (Modifier.PUBLIC,
             ConstantPool.addUtf8(pool, Classes.toString(m.name)),
             ConstantPool.addUtf8(pool, Classes.toString(m.spec)),
             makeInvokeCode(pool, name, m.spec, m.parameterCount,
                            m.parameterFootprint, methodTable.size())));
          refs.add(Classes.makeMethod(m));
        }
      }
    }

    shim.VMMethod[] objectTable = SystemClassLoader.vmClass(Object.class).virtualTable;
    if (objectTable != null) {
      for (shim.VMMethod m: objectTable) {
        String spec = Classes.toString(m.name) + Classes.toString(m.spec);
        if (!(spec.equals("toString()Ljava/lang/String;") || spec.equals("hashCode()I")
              || spec.equals("equals(Ljava/lang/Object;)Z")) || specs.contains(spec)) {
          continue;
        }
        specs.add(spec);
        methodTable.add(new MethodData
          (Modifier.PUBLIC,
           ConstantPool.addUtf8(pool, Classes.toString(m.name)),
           ConstantPool.addUtf8(pool, Classes.toString(m.spec)),
           makeInvokeCode(pool, name, m.spec, m.parameterCount,
                          m.parameterFootprint, methodTable.size())));
        refs.add(Classes.makeMethod(m));
      }
    }

    methodTable.add(new MethodData
      (Modifier.PUBLIC,
       ConstantPool.addUtf8(pool, "<init>"),
       ConstantPool.addUtf8
       (pool, "(Ljava/lang/reflect/InvocationHandler;)V"),
       makeConstructorCode(pool)));

    int nameIndex = ConstantPool.addClass(pool, name);
    int superIndex = ConstantPool.addClass(pool, "java/lang/reflect/Proxy");

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    Assembler.writeClass
      (out, pool, nameIndex, superIndex, interfaceIndexes,
       new FieldData[0],
       methodTable.toArray(new MethodData[methodTable.size()]));

    byte[] classData = out.toByteArray();
    Class result = shim.SystemClassLoader.getClass
      (shim.Classes.defineVMClass(loader, classData, 0, classData.length));
    methodRefsMap.put(result, refs.toArray(new Method[refs.size()]));
    return result;
  }

  public static Object newProxyInstance(ClassLoader loader,
                                        Class[] interfaces,
                                        InvocationHandler handler)
  {
    try {
      return Proxy.getProxyClass(loader, interfaces)
        .getConstructor(new Class[] { InvocationHandler.class })
        .newInstance(new Object[] { handler });
    } catch (Exception e) {
      AssertionError error = new AssertionError();
      error.initCause(e);
      throw error;
    }
  }
}
