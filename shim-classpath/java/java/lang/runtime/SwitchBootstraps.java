/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.runtime;

import shim.Assembler;
import shim.Classes;
import shim.ConstantPool;
import shim.ConstantPool.PoolEntry;
import shim.Stream;
import shim.SystemClassLoader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class SwitchBootstraps {
  private SwitchBootstraps() {}

  private static final List<Object[]> labelRegistry = new ArrayList<Object[]>();
  private static int nextClassId = 0;

  private static int registerLabels(Object[] labels) {
    int id;
    synchronized (SwitchBootstraps.class) {
      id = labelRegistry.size();
      Object[] copy = new Object[labels.length];
      System.arraycopy(labels, 0, copy, 0, labels.length);
      labelRegistry.add(copy);
    }
    return id;
  }

  public static CallSite typeSwitch(
      MethodHandles.Lookup lookup,
      String invocationName,
      MethodType invocationType,
      Object... labels) {
    Objects.requireNonNull(lookup, "lookup");
    Objects.requireNonNull(invocationName, "invocationName");
    Objects.requireNonNull(invocationType, "invocationType");
    Objects.requireNonNull(labels, "labels");
    int labelId = registerLabels(labels);
    try {
      MethodHandle target = generateSwitchTarget(lookup, invocationType, labelId, false);
      return new ConstantCallSite(target);
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }

  public static CallSite enumSwitch(
      MethodHandles.Lookup lookup,
      String invocationName,
      MethodType invocationType,
      Object... labels) {
    Objects.requireNonNull(lookup, "lookup");
    Objects.requireNonNull(invocationName, "invocationName");
    Objects.requireNonNull(invocationType, "invocationType");
    Objects.requireNonNull(labels, "labels");
    int labelId = registerLabels(labels);
    try {
      MethodHandle target = generateSwitchTarget(lookup, invocationType, labelId, true);
      return new ConstantCallSite(target);
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }

  private static MethodHandle generateSwitchTarget(
      MethodHandles.Lookup lookup,
      MethodType invocationType,
      int labelId,
      boolean isEnum) throws Throwable {

    String generatedClassName;
    synchronized (SwitchBootstraps.class) {
      generatedClassName = "shim/runtime/SwitchHelper$" + (nextClassId++);
    }

    List<PoolEntry> pool = new ArrayList<PoolEntry>();
    byte[] code = assembleSwitch(pool, labelId, isEnum);

    Assembler.MethodData method = new Assembler.MethodData(
        Assembler.ACC_PUBLIC | Assembler.ACC_STATIC,
        ConstantPool.addUtf8(pool, "make"),
        ConstantPool.addUtf8(pool, invocationType.toMethodDescriptorString()),
        code);

    int nameIndex = ConstantPool.addClass(pool, generatedClassName);
    int parentIndex = ConstantPool.addClass(pool, "java/lang/Object");

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    Assembler.writeClass(out, pool, nameIndex, parentIndex, new int[0],
        new Assembler.FieldData[0], new Assembler.MethodData[] { method });
    byte[] data = out.toByteArray();

    ClassLoader cl = lookup.lookupClass().getClassLoader();
    Class<?> generated = SystemClassLoader.getClass(
        Classes.defineVMClass(cl, data, 0, data.length));
    Method m = generated.getMethod("make", invocationType.parameterArray());
    return new MethodHandle(MethodHandle.REF_invokeStatic, cl, Classes.toVMMethod(m));
  }

  private static byte[] assembleSwitch(List<PoolEntry> pool, int labelId, boolean isEnum) throws IOException {
    ByteArrayOutputStream code = new ByteArrayOutputStream();
    Stream.write2(code, 3); // max_stack
    Stream.write2(code, 2); // max_locals
    Stream.write4(code, 0); // code_length placeholder

    Stream.write1(code, Assembler.aload_0);
    Stream.write1(code, Assembler.iload);
    Stream.write1(code, 1);
    Stream.write1(code, Assembler.ldc_w);
    Stream.write2(code, ConstantPool.addInteger(pool, labelId) + 1);
    Stream.write1(code, Assembler.invokestatic);
    String targetMethod = isEnum ? "doEnumSwitch" : "doSwitch";
    Stream.write2(code, ConstantPool.addMethodRef(pool, "java/lang/runtime/SwitchBootstraps",
        targetMethod, "(Ljava/lang/Object;II)I") + 1);
    Stream.write1(code, Assembler.ireturn);

    Stream.write2(code, 0); // exception_table_length
    Stream.write2(code, 0); // attributes_count
    byte[] result = code.toByteArray();
    Stream.set4(result, 4, result.length - 12);
    return result;
  }

  public static int doSwitch(Object selector, int restart, int labelId) {
    if (selector == null) {
      return -1;
    }
    Object[] labels = labelRegistry.get(labelId);
    if (restart < 0 || restart > labels.length) {
      throw new IllegalArgumentException("Invalid restart index: " + restart);
    }
    for (int i = restart; i < labels.length; ++i) {
      Object label = labels[i];
      if (label instanceof Class<?>) {
        if (((Class<?>) label).isInstance(selector)) {
          return i;
        }
      } else if (label instanceof Enum.EnumDesc<?>) {
        if (selector instanceof Enum<?>
            && ((Enum<?>) selector).name().equals(((Enum.EnumDesc<?>) label).constantName())) {
          return i;
        }
      } else if (label instanceof String) {
        if (selector instanceof Enum<?> && ((Enum<?>) selector).name().equals(label)) {
          return i;
        } else if (label.equals(selector)) {
          return i;
        }
      } else if (label != null) {
        if (label.equals(selector)) {
          return i;
        }
      }
    }
    return labels.length;
  }

  public static int doEnumSwitch(Object selector, int restart, int labelId) {
    if (selector == null) {
      return -1;
    }
    if (!(selector instanceof Enum<?>)) {
      throw new IllegalArgumentException("Not an enum: " + selector);
    }
    Enum<?> e = (Enum<?>) selector;
    String name = e.name();
    Object[] labels = labelRegistry.get(labelId);
    if (restart < 0 || restart > labels.length) {
      throw new IllegalArgumentException("Invalid restart index: " + restart);
    }
    for (int i = restart; i < labels.length; ++i) {
      Object label = labels[i];
      if (label instanceof String) {
        if (name.equals(label)) {
          return i;
        }
      } else if (label instanceof Enum.EnumDesc<?>) {
        if (name.equals(((Enum.EnumDesc<?>) label).constantName())) {
          return i;
        }
      } else if (label instanceof Class<?>) {
        if (((Class<?>) label).isInstance(selector)) {
          return i;
        }
      }
    }
    return labels.length;
  }
}
