/*
 * Based on Avian (https://github.com/ReadyTalk/avian).
 * Original work Copyright (c) 2008-2016, Avian Contributors.
 * Original work licensed under ISC; see LICENSE.txt:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE.txt
 *
 * Modifications Copyright (c) 2026 BasaltMC.
 * Modifications licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: ISC AND MIT
 */

/* Copyright (c) 2008-2016, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.lang.invoke;

import static shim.Stream.write1;
import static shim.Stream.write2;
import static shim.Stream.write4;
import static shim.Stream.set4;
import static shim.Assembler.*;

import java.lang.reflect.Proxy;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.IOException;

import shim.Classes;
import shim.ConstantPool;
import shim.Assembler;
import shim.ConstantPool.PoolEntry;
import shim.SystemClassLoader;

// To understand what this is all about, please read:
//
//   http://cr.openjdk.java.net/~briangoetz/lambda/lambda-translation.html

public class LambdaMetafactory {
  private static int nextNumber = 0;

  public static final int FLAG_SERIALIZABLE = 1;
  public static final int FLAG_MARKERS = 2;
  public static final int FLAG_BRIDGES = 4;

  private static Class resolveReturnInterface(MethodType type) {
    int index = 1;
    byte[] s = type.spec;

    while (s[index] != ')') ++ index;

    if (s[++ index] != 'L') throw new AssertionError();

    ++ index;

    int end = index + 1;
    while (s[end] != ';') ++ end;

    Class c = SystemClassLoader.getClass
      (Classes.loadVMClass(type.loader, s, index, end - index));

    if (! c.isInterface()) throw new AssertionError();

    return c;
  }

  private static int indexOf(int c, byte[] array) {
    int i = 0;
    while (array[i] != c) ++i;
    return i;
  }

  private static String constructorSpec(MethodType type) {
    return Classes.makeString(type.spec, 0, indexOf(')', type.spec) + 1) + "V";
  }

  private static byte[] makeFactoryCode(List<PoolEntry> pool,
                                        String className,
                                        String constructorSpec,
                                        MethodType type)
    throws IOException
  {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    write2(out, type.footprint() + 2); // max stack
    write2(out, type.footprint()); // max locals
    write4(out, 0); // length (we'll set the real value later)

    write1(out, new_);
    write2(out, ConstantPool.addClass(pool, className) + 1);
    write1(out, dup);

    for (MethodType.Parameter p: type.parameters()) {
      write1(out, p.load());
      write1(out, p.position());
    }

    write1(out, invokespecial);
    write2(out, ConstantPool.addMethodRef
           (pool, className, "<init>", constructorSpec) + 1);

    write1(out, areturn);

    write2(out, 0); // exception handler table length
    write2(out, 0); // attribute count

    byte[] result = out.toByteArray();
    set4(result, 4, result.length - 12);

    return result;
  }

  private static byte[] makeConstructorCode(List<PoolEntry> pool,
                                            String className,
                                            MethodType type)
    throws IOException
  {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    write2(out, 3); // max stack
    write2(out, type.footprint() + 1); // max locals
    write4(out, 0); // length (we'll set the real value later)

    write1(out, aload_0);
    write1(out, invokespecial);
    write2(out, ConstantPool.addMethodRef
           (pool, "java/lang/Object", "<init>", "()V") + 1);

    for (MethodType.Parameter p: type.parameters()) {
      write1(out, aload_0);
      write1(out, p.load());
      write1(out, p.position() + 1);
      write1(out, putfield);
      write2(out, ConstantPool.addFieldRef
             (pool, className, "field" + p.index(), p.spec()) + 1);
    }

    write1(out, return_);

    write2(out, 0); // exception handler table length
    write2(out, 0); // attribute count

    byte[] result = out.toByteArray();
    set4(result, 4, result.length - 12);

    return result;
  }

  private static String wrapperName(String spec) {
    switch (spec) {
    case "Z": return "java/lang/Boolean";
    case "B": return "java/lang/Byte";
    case "S": return "java/lang/Short";
    case "C": return "java/lang/Character";
    case "I": return "java/lang/Integer";
    case "F": return "java/lang/Float";
    case "J": return "java/lang/Long";
    case "D": return "java/lang/Double";
    default: throw new AssertionError("no wrapper for " + spec);
    }
  }

  private static void widenPrimitive(ByteArrayOutputStream out, String from, String to) throws IOException {
    if (from.equals(to) || from.equals("V") || to.equals("V") || from.equals("Z") || to.equals("Z")) return;
    boolean intLike = from.equals("B") || from.equals("S") || from.equals("C") || from.equals("I");
    switch (to) {
    case "J":
      if (intLike) write1(out, 0x85);
      break;
    case "F":
      write1(out, intLike ? 0x86 : 0x89);
      break;
    case "D":
      if (intLike) write1(out, 0x87);
      else if (from.equals("J")) write1(out, 0x8a);
      else if (from.equals("F")) write1(out, 0x8d);
      break;
    default:
      break;
    }
  }

  private static String primitiveOfWrapper(Class<?> c) {
    if (c == Boolean.class) return "Z";
    if (c == Byte.class) return "B";
    if (c == Short.class) return "S";
    if (c == Character.class) return "C";
    if (c == Integer.class) return "I";
    if (c == Float.class) return "F";
    if (c == Long.class) return "J";
    if (c == Double.class) return "D";
    return null;
  }

  private static void maybeBoxOrUnbox(ByteArrayOutputStream out,
                                      List<PoolEntry> pool,
                                      MethodType.TypeSpec from,
                                      MethodType.TypeSpec to,
                                      Class<?> hint)
    throws IOException
  {
    String hinted = hint == null ? null : primitiveOfWrapper(hint);
    if (hinted != null && to.type().isPrimitive() && !from.type().isPrimitive() && !hinted.equals(to.spec())
        && !hinted.equals("Z") && !to.spec().equals("Z")) {
      MethodType.TypeSpec prim = hintSpec(hinted);
      maybeBoxOrUnbox(out, pool, from, prim, null);
      widenPrimitive(out, hinted, to.spec());
      return;
    }
    maybeBoxOrUnbox(out, pool, from, to);
  }

  private static MethodType.TypeSpec hintSpec(final String spec) {
    final Class<?> type = spec.equals("B") ? byte.class : spec.equals("S") ? short.class
        : spec.equals("C") ? char.class : spec.equals("I") ? int.class : spec.equals("F") ? float.class
        : spec.equals("J") ? long.class : double.class;
    return new MethodType.TypeSpec() {
      public Class type() {
        return type;
      }

      public String spec() {
        return spec;
      }
    };
  }

  private static void maybeBoxOrUnbox(ByteArrayOutputStream out,
                                      List<PoolEntry> pool,
                                      MethodType.TypeSpec from,
                                      MethodType.TypeSpec to)
    throws IOException
  {
    if (to.type().isPrimitive() && from.type().isPrimitive()) {
      widenPrimitive(out, from.spec(), to.spec());
      return;
    }
    if (!to.type().isPrimitive() && !from.type().isPrimitive() && to.type() != Object.class
        && !to.type().isAssignableFrom(from.type())) {
      String spec = to.spec();
      String name = spec.startsWith("L") ? spec.substring(1, spec.length() - 1) : spec;
      write1(out, 0xc0);
      write2(out, ConstantPool.addClass(pool, name) + 1);
      return;
    }
    if (to.type().isPrimitive()) {
      if (! (from.type().isPrimitive() || "V".equals(to.spec()))) {
        write1(out, 0xc0);
        write2(out, ConstantPool.addClass(pool, wrapperName(to.spec())) + 1);
        write1(out, invokevirtual);

        try {
          switch (to.spec()) {
          case "Z":
            writeMethodReference(out, pool, Classes.toVMMethod
                                 (Boolean.class.getMethod("booleanValue")));
            break;

          case "B":
            writeMethodReference(out, pool, Classes.toVMMethod
                                 (Byte.class.getMethod("byteValue")));
            break;

          case "S":
            writeMethodReference(out, pool, Classes.toVMMethod
                                 (Short.class.getMethod("shortValue")));
            break;

          case "C":
            writeMethodReference(out, pool, Classes.toVMMethod
                                 (Character.class.getMethod("charValue")));
            break;

          case "I":
            writeMethodReference(out, pool, Classes.toVMMethod
                                 (Integer.class.getMethod("intValue")));
            break;

          case "F":
            writeMethodReference(out, pool, Classes.toVMMethod
                                 (Float.class.getMethod("floatValue")));
            break;

          case "J":
            writeMethodReference(out, pool, Classes.toVMMethod
                                 (Long.class.getMethod("longValue")));
            break;

          case "D":
            writeMethodReference(out, pool, Classes.toVMMethod
                                 (Double.class.getMethod("doubleValue")));
            break;

          default:
            throw new AssertionError("don't know how to auto-unbox to " + to.spec());
          }
        } catch (NoSuchMethodException e) {
          throw new Error(e);
        }
      }
    } else if (from.type().isPrimitive()) {
      write1(out, invokestatic);

      try {
        switch (from.spec()) {
        case "Z":
          writeMethodReference(out, pool, Classes.toVMMethod
                               (Boolean.class.getMethod
                                ("valueOf", Boolean.TYPE)));
          break;

        case "B":
          writeMethodReference(out, pool, Classes.toVMMethod
                               (Byte.class.getMethod
                                ("valueOf", Byte.TYPE)));
          break;

        case "S":
          writeMethodReference(out, pool, Classes.toVMMethod
                               (Short.class.getMethod
                                ("valueOf", Short.TYPE)));
          break;

        case "C":
          writeMethodReference(out, pool, Classes.toVMMethod
                               (Character.class.getMethod
                                ("valueOf", Character.TYPE)));
          break;

        case "I":
          writeMethodReference(out, pool, Classes.toVMMethod
                               (Integer.class.getMethod
                                ("valueOf", Integer.TYPE)));
          break;

        case "F":
          writeMethodReference(out, pool, Classes.toVMMethod
                               (Float.class.getMethod
                                ("valueOf", Float.TYPE)));
          break;

        case "J":
          writeMethodReference(out, pool, Classes.toVMMethod
                               (Long.class.getMethod
                                ("valueOf", Long.TYPE)));
          break;

        case "D":
          writeMethodReference(out, pool, Classes.toVMMethod
                               (Double.class.getMethod
                                ("valueOf", Double.TYPE)));
          break;

        default:
          throw new AssertionError("don't know how to autobox from " + from.spec());
        }
      } catch (NoSuchMethodException e) {
        throw new Error(e);
      }
    }
  }

  private static byte[] makeInvocationCode(List<PoolEntry> pool,
                                           String className,
                                           String constructorSpec,
                                           MethodType fieldType,
                                           MethodType localType,
                                           MethodType instantiatedType,
                                           MethodHandle implementation)
    throws IOException
  {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    write2(out, fieldType.footprint()
           + localType.footprint() + implementation.type().footprint() + 6); // max stack
    write2(out, localType.footprint() + 1); // max locals
    write4(out, 0); // length (we'll set the real value later)

    if (implementation.kind == MethodHandle.REF_newInvokeSpecial) {
      write1(out, new_);
      write2(out, ConstantPool.addClass
             (pool,
              Classes.makeString
              (implementation.method.class_.name, 0,
               implementation.method.class_.name.length - 1)) + 1);
      write1(out, dup);
    }

    Iterator<MethodType.Parameter> dst = implementation.type().parameters().iterator();

    boolean skip = implementation.kind != MethodHandle.REF_invokeStatic
        && implementation.kind != MethodHandle.REF_newInvokeSpecial;
    if (skip) dst.next(); // MethodHandle.type includes the receiver parameter.

    for (MethodType.Parameter p: fieldType.parameters()) {
      write1(out, aload_0);
      write1(out, getfield);
      write2(out, ConstantPool.addFieldRef
             (pool, className, "field" + p.index(), p.spec()) + 1);
      if (skip) {
        skip = false;
      } else {
        maybeBoxOrUnbox(out, pool, p, dst.next());
      }
    }

    Iterator<MethodType.Parameter> instantiated = instantiatedType.parameters().iterator();
    for (MethodType.Parameter p: localType.parameters()) {
      Class<?> hint = instantiated.hasNext() ? instantiated.next().type() : null;
      write1(out, p.load());
      write1(out, p.position() + 1);
      if (skip) {
        skip = false;
      } else {
        maybeBoxOrUnbox(out, pool, p, dst.next(), hint);
      }
    }

    switch (implementation.kind) {
    case MethodHandle.REF_invokeVirtual:
      write1(out, invokevirtual);
      writeMethodReference(out, pool, implementation.method);
      break;

    case MethodHandle.REF_invokeStatic:
      write1(out, invokestatic);
      writeMethodReference(out, pool, implementation.method);
      break;

    case MethodHandle.REF_invokeSpecial:
    case MethodHandle.REF_newInvokeSpecial:
      write1(out, invokespecial);
      writeMethodReference(out, pool, implementation.method);
      break;

    case MethodHandle.REF_invokeInterface:
      write1(out, invokeinterface);
      writeInterfaceMethodReference(out, pool, implementation.method);
      write1(out, implementation.method.parameterFootprint);
      write1(out, 0);
      break;

    default: throw new AssertionError
        ("todo: implement '" + implementation.kind + "' per http://docs.oracle.com/javase/specs/jvms/se8/html/jvms-5.html#jvms-5.4.3.5");
    }

    if (implementation.kind != MethodHandle.REF_newInvokeSpecial) {
      maybeBoxOrUnbox(out, pool, implementation.type().result(), localType.result());
    }
    write1(out, localType.result().return_());

    write2(out, 0); // exception handler table length
    write2(out, 0); // attribute count

    byte[] result = out.toByteArray();
    set4(result, 4, result.length - 12);

    return result;
  }

  private static final MethodType.TypeSpec kObjectSpec = new MethodType.TypeSpec() {
    public Class type() {
      return Object.class;
    }

    public String spec() {
      return "Ljava/lang/Object;";
    }
  };

  private static void pushString(ByteArrayOutputStream out, List<PoolEntry> pool, String value) throws IOException {
    write1(out, ldc_w);
    write2(out, ConstantPool.addString(pool, value) + 1);
  }

  private static void pushInt(ByteArrayOutputStream out, List<PoolEntry> pool, int value) throws IOException {
    write1(out, ldc_w);
    write2(out, ConstantPool.addInteger(pool, value) + 1);
  }

  private static byte[] makeWriteReplaceCode(List<PoolEntry> pool,
                                             String className,
                                             MethodType fieldType,
                                             MethodType interfaceMethodType,
                                             String invokedName,
                                             MethodType instantiatedType,
                                             MethodHandle implementation,
                                             Class<?> capturingClass)
    throws IOException
  {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    write2(out, 16); // max stack
    write2(out, 1); // max locals
    write4(out, 0); // length (we'll set the real value later)

    write1(out, new_);
    write2(out, ConstantPool.addClass(pool, "java/lang/invoke/SerializedLambda") + 1);
    write1(out, dup);
    write1(out, ldc_w);
    write2(out, ConstantPool.addClass(pool, capturingClass.getName().replace('.', '/')) + 1);
    pushString(out, pool, fieldType.returnType().getName().replace('.', '/'));
    pushString(out, pool, invokedName);
    pushString(out, pool, interfaceMethodType.toMethodDescriptorString());
    pushInt(out, pool, implementation.kind);
    pushString(out, pool, Classes.makeString(implementation.method.class_.name, 0,
                                             implementation.method.class_.name.length - 1));
    pushString(out, pool, Classes.makeString(implementation.method.name, 0, implementation.method.name.length - 1));
    pushString(out, pool, Classes.makeString(implementation.method.spec, 0, implementation.method.spec.length - 1));
    pushString(out, pool, instantiatedType.toMethodDescriptorString());

    int count = 0;
    for (MethodType.Parameter p: fieldType.parameters()) {
      count++;
    }
    pushInt(out, pool, count);
    write1(out, anewarray);
    write2(out, ConstantPool.addClass(pool, "java/lang/Object") + 1);
    int slot = 0;
    for (MethodType.Parameter p: fieldType.parameters()) {
      write1(out, dup);
      pushInt(out, pool, slot++);
      write1(out, aload_0);
      write1(out, getfield);
      write2(out, ConstantPool.addFieldRef(pool, className, "field" + p.index(), p.spec()) + 1);
      maybeBoxOrUnbox(out, pool, p, kObjectSpec);
      write1(out, aastore);
    }

    write1(out, invokespecial);
    write2(out, ConstantPool.addMethodRef
           (pool, "java/lang/invoke/SerializedLambda", "<init>",
            "(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ILjava/lang/String;"
            + "Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;[Ljava/lang/Object;)V") + 1);
    write1(out, areturn);

    write2(out, 0); // exception handler table length
    write2(out, 0); // attribute count

    byte[] result = out.toByteArray();
    set4(result, 4, result.length - 12);

    return result;
  }

  private static void writeMethodReference(OutputStream out,
                                           List<PoolEntry> pool,
                                           shim.VMMethod method)
    throws IOException
  {
    write2(out, ConstantPool.addMethodRef
           (pool,
            Classes.makeString(method.class_.name, 0,
                               method.class_.name.length - 1),
            Classes.makeString(method.name, 0,
                               method.name.length - 1),
            Classes.makeString(method.spec, 0,
                               method.spec.length - 1)) + 1);
  }

  private static void writeInterfaceMethodReference(OutputStream out,
                                                    List<PoolEntry> pool,
                                                    shim.VMMethod method)
    throws IOException
  {
    write2(out, ConstantPool.addInterfaceMethodRef
           (pool,
            Classes.makeString(method.class_.name, 0,
                               method.class_.name.length - 1),
            Classes.makeString(method.name, 0,
                               method.name.length - 1),
            Classes.makeString(method.spec, 0,
                               method.spec.length - 1)) + 1);
  }

  public static byte[] makeLambda(String invokedName,
                                  String invokedType,
                                  String methodType,
                                  String implementationClass,
                                  String implementationName,
                                  String implementationSpec,
                                  int implementationKind)
  {
    return makeLambda(invokedName,
                      new MethodType(invokedType),
                      new MethodType(methodType),
                      new MethodHandle(implementationClass,
                                       implementationName,
                                       implementationSpec,
                                       implementationKind),
                      emptyInterfaceList,
                      null,
                      new MethodType[0],
                      null,
                      false);
  }

  private static byte[] makeLambda(String invokedName,
                                   MethodType invokedType,
                                   MethodType methodType,
                                   MethodHandle methodImplementation,
                                   Class[] interfaces,
                                   MethodType instantiatedType,
                                   MethodType[] bridges,
                                   Class<?> caller,
                                   boolean serializable)
  {
    String className;
    { int number;
      synchronized (LambdaMetafactory.class) {
        number = nextNumber++;
      }
      className = caller == null ? "Lambda-" + number
          : caller.getName().replace('.', '/') + "$$Lambda$" + number;
    }

    List<PoolEntry> pool = new ArrayList();

    int[] interfaceIndexes = new int[interfaces.length + 1];
    interfaceIndexes[0] = ConstantPool.addClass(pool, invokedType.returnType().getName().replace('.', '/'));
    for (int i = 0; i < interfaces.length; i++) {
      String name = interfaces[i].getName().replace('.', '/');
      interfaceIndexes[i + 1] = ConstantPool.addClass(pool, name);
    }

    List<FieldData> fieldTable = new ArrayList();

    for (MethodType.Parameter p: invokedType.parameters()) {
      fieldTable.add
        (new FieldData(0,
                       ConstantPool.addUtf8(pool, "field" + p.index()),
                       ConstantPool.addUtf8(pool, p.spec())));
    }

    String constructorSpec = constructorSpec(invokedType);

    List<MethodData> methodTable = new ArrayList();

    try {
      methodTable.add
        (new MethodData
         (Modifier.PUBLIC | Modifier.STATIC,
          ConstantPool.addUtf8(pool, "make"),
          ConstantPool.addUtf8(pool, invokedType.toMethodDescriptorString()),
          makeFactoryCode(pool, className, constructorSpec, invokedType)));

      methodTable.add
        (new MethodData
         (Modifier.PUBLIC,
          ConstantPool.addUtf8(pool, "<init>"),
          ConstantPool.addUtf8(pool, constructorSpec),
          makeConstructorCode(pool, className, invokedType)));

      methodTable.add
        (new MethodData
         (Modifier.PUBLIC,
          ConstantPool.addUtf8(pool, invokedName),
          ConstantPool.addUtf8(pool, methodType.toMethodDescriptorString()),
          makeInvocationCode(pool, className, constructorSpec, invokedType,
                             methodType, instantiatedType, methodImplementation)));

      for (MethodType bridge: bridges) {
        if (bridge.toMethodDescriptorString().equals(methodType.toMethodDescriptorString())) continue;
        methodTable.add
          (new MethodData
           (Modifier.PUBLIC | 0x40 | 0x1000,
            ConstantPool.addUtf8(pool, invokedName),
            ConstantPool.addUtf8(pool, bridge.toMethodDescriptorString()),
            makeInvocationCode(pool, className, constructorSpec, invokedType,
                               bridge, instantiatedType, methodImplementation)));
      }

      if (serializable) {
        methodTable.add
          (new MethodData
           (Modifier.PRIVATE | Modifier.FINAL,
            ConstantPool.addUtf8(pool, "writeReplace"),
            ConstantPool.addUtf8(pool, "()Ljava/lang/Object;"),
            makeWriteReplaceCode(pool, className, invokedType, methodType, invokedName,
                                 instantiatedType, methodImplementation, caller)));
      }
    } catch (IOException e) {
      AssertionError error = new AssertionError();
      error.initCause(e);
      throw error;
    }

    int nameIndex = ConstantPool.addClass(pool, className);
    int superIndex = ConstantPool.addClass(pool, "java/lang/Object");

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    try {
      Assembler.writeClass
        (out, pool, nameIndex, superIndex, interfaceIndexes,
         fieldTable.toArray(new FieldData[fieldTable.size()]),
         methodTable.toArray(new MethodData[methodTable.size()]),
         Assembler.ACC_FINAL | Assembler.ACC_SYNTHETIC);
    } catch (IOException e) {
      AssertionError error = new AssertionError();
      error.initCause(e);
      throw error;
    }

    return out.toByteArray();
  }

  private static CallSite makeCallSite(MethodType invokedType, byte[] classData, MethodHandle implementation)
      throws AssertionError {
    try {
      return new ConstantCallSite
              (new MethodHandle
                      (MethodHandle.REF_invokeStatic, invokedType.loader, Classes.toVMMethod
                              (shim.SystemClassLoader.getClass
                                      (shim.Classes.defineLambdaVMClass
                                              (invokedType.loader, classData, implementation.method.class_))
                                      .getMethod("make", invokedType.parameterArray()))));
    } catch (NoSuchMethodException e) {
      AssertionError error = new AssertionError();
      error.initCause(e);
      throw error;
    }
  }

  private static final Class[] emptyInterfaceList = new Class[] {};

  public static CallSite metafactory(MethodHandles.Lookup caller,
                                     String invokedName,
                                     MethodType invokedType,
                                     MethodType methodType,
                                     MethodHandle methodImplementation,
                                     MethodType instantiatedMethodType)
    throws LambdaConversionException
  {
    methodImplementation = methodImplementation.asFixedArity();
    byte[] classData = makeLambda(invokedName, invokedType, methodType, methodImplementation, emptyInterfaceList,
                                  instantiatedMethodType, new MethodType[0], caller.lookupClass(), false);
    return makeCallSite(invokedType, classData, methodImplementation);
  }

  public static CallSite altMetafactory(MethodHandles.Lookup caller,
                                        String invokedName,
                                        MethodType invokedType,
                                        Object... args) throws LambdaConversionException {
    // See openjdk8/jdk/src/share/classes/java/lang/invoke/LambdaMetafactory.java
    // Behaves as if the prototype is like this:
    //
    // CallSite altMetafactory(
    //    MethodHandles.Lookup caller,
    //    String invokedName,
    //    MethodType invokedType,
    //    MethodType methodType,
    //    MethodHandle methodImplementation,
    //    MethodType instantiatedMethodType,
    //    int flags,
    //    int markerInterfaceCount,  // IF flags has MARKERS set
    //    Class... markerInterfaces, // IF flags has MARKERS set
    //    int bridgeCount,           // IF flags has BRIDGES set
    //    MethodType... bridges      // IF flags has BRIDGES set
    //  )
    MethodType methodType = (MethodType) args[0];
    MethodHandle methodImplementation = ((MethodHandle) args[1]).asFixedArity();
    MethodType instantiatedMethodType = (MethodType) args[2];

    int flags = (Integer) args[3];
    boolean serializable = (flags & FLAG_SERIALIZABLE) != 0;
    int next = 4;

    List<Class> interfaceList = new ArrayList<Class>();
    if (serializable) {
      interfaceList.add(java.io.Serializable.class);
    }
    if ((flags & FLAG_MARKERS) != 0) {
      int markerCount = (Integer) args[next++];
      for (int i = 0; i < markerCount; i++) {
        Class marker = (Class) args[next++];
        if (!interfaceList.contains(marker)) {
          interfaceList.add(marker);
        }
      }
    }

    MethodType[] bridges = new MethodType[0];
    if ((flags & FLAG_BRIDGES) != 0) {
      int bridgeCount = (Integer) args[next++];
      bridges = new MethodType[bridgeCount];
      for (int i = 0; i < bridgeCount; i++) {
        bridges[i] = (MethodType) args[next++];
      }
    }

    Class[] interfaces = interfaceList.toArray(new Class[interfaceList.size()]);
    byte[] classData = makeLambda(invokedName, invokedType, methodType, methodImplementation, interfaces,
                                  instantiatedMethodType, bridges, caller.lookupClass(), serializable);
    return makeCallSite(invokedType, classData, methodImplementation);
  }
}
