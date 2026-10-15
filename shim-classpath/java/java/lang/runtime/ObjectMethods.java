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
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.TypeDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class ObjectMethods {
  private ObjectMethods() {}

  private static final class RecordMeta {
    final Class<?> recordClass;
    final String[] names;
    final MethodHandle[] getters;

    RecordMeta(Class<?> recordClass, String[] names, MethodHandle[] getters) {
      this.recordClass = recordClass;
      this.names = names;
      this.getters = getters;
    }
  }

  private static final List<RecordMeta> registry = new ArrayList<RecordMeta>();
  private static int nextClassId = 0;

  private static synchronized int register(Class<?> recordClass, String[] names, MethodHandle[] getters) {
    int id = registry.size();
    registry.add(new RecordMeta(recordClass, names, getters));
    return id;
  }

  public static Object bootstrap(
      MethodHandles.Lookup lookup,
      String methodName,
      TypeDescriptor type,
      Class<?> recordClass,
      String names,
      MethodHandle... getters) throws Throwable {
    Objects.requireNonNull(lookup, "lookup");
    Objects.requireNonNull(methodName, "methodName");
    Objects.requireNonNull(type, "type");
    Objects.requireNonNull(recordClass, "recordClass");
    Objects.requireNonNull(names, "names");
    Objects.requireNonNull(getters, "getters");
    for (MethodHandle g : getters) {
      Objects.requireNonNull(g, "getter");
    }
    String[] nameList = names.isEmpty() ? new String[0] : names.split(";");
    if (nameList.length != getters.length) {
      throw new IllegalArgumentException("names length " + nameList.length + " != getters length " + getters.length);
    }

    MethodType methodType;
    if (type instanceof MethodType) {
      methodType = (MethodType) type;
    } else if (type instanceof Class) {
      switch (methodName) {
        case "toString":
          methodType = MethodType.methodType(String.class, recordClass);
          break;
        case "hashCode":
          methodType = MethodType.methodType(int.class, recordClass);
          break;
        case "equals":
          methodType = MethodType.methodType(boolean.class, new Class<?>[] { recordClass, Object.class });
          break;
        default:
          throw new IllegalArgumentException("Unknown method name: " + methodName);
      }
    } else {
      throw new IllegalArgumentException("Unsupported type descriptor: " + type);
    }

    int metaId = register(recordClass, nameList, getters);
    MethodHandle targetHandle = generateTarget(lookup, methodName, methodType, recordClass, metaId);

    if (type instanceof MethodType) {
      return new ConstantCallSite(targetHandle);
    } else {
      return targetHandle;
    }
  }

  private static MethodHandle generateTarget(
      MethodHandles.Lookup lookup,
      String methodName,
      MethodType methodType,
      Class<?> recordClass,
      int metaId) throws Throwable {

    String generatedClassName;
    synchronized (ObjectMethods.class) {
      generatedClassName = "shim/record/RecordHelper$" + (nextClassId++);
    }

    List<PoolEntry> pool = new ArrayList<PoolEntry>();
    byte[] code;

    switch (methodName) {
      case "toString":
        code = assembleToString(pool, metaId);
        break;
      case "hashCode":
        code = assembleHashCode(pool, metaId);
        break;
      case "equals":
        code = assembleEquals(pool, metaId);
        break;
      default:
        throw new IllegalArgumentException("Unknown method name: " + methodName);
    }

    Assembler.MethodData method = new Assembler.MethodData(
        Assembler.ACC_PUBLIC | Assembler.ACC_STATIC,
        ConstantPool.addUtf8(pool, "make"),
        ConstantPool.addUtf8(pool, methodType.toMethodDescriptorString()),
        code);

    int nameIndex = ConstantPool.addClass(pool, generatedClassName);
    int parentIndex = ConstantPool.addClass(pool, "java/lang/Object");

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    Assembler.writeClass(out, pool, nameIndex, parentIndex, new int[0],
        new Assembler.FieldData[0], new Assembler.MethodData[] { method });
    byte[] data = out.toByteArray();

    ClassLoader cl = recordClass.getClassLoader();
    Class<?> generated = SystemClassLoader.getClass(
        Classes.defineVMClass(cl, data, 0, data.length));
    Method m = generated.getMethod("make", methodType.parameterArray());
    return new MethodHandle(MethodHandle.REF_invokeStatic, cl, Classes.toVMMethod(m));
  }

  private static byte[] assembleToString(List<PoolEntry> pool, int metaId) throws IOException {
    ByteArrayOutputStream code = new ByteArrayOutputStream();
    Stream.write2(code, 2); // max_stack
    Stream.write2(code, 1); // max_locals
    Stream.write4(code, 0); // code_length placeholder

    Stream.write1(code, Assembler.aload_0);
    Stream.write1(code, Assembler.ldc_w);
    Stream.write2(code, ConstantPool.addInteger(pool, metaId) + 1);
    Stream.write1(code, Assembler.invokestatic);
    Stream.write2(code, ConstantPool.addMethodRef(pool, "java/lang/runtime/ObjectMethods",
        "recordToString", "(Ljava/lang/Object;I)Ljava/lang/String;") + 1);
    Stream.write1(code, Assembler.areturn);

    Stream.write2(code, 0); // exception_table_length
    Stream.write2(code, 0); // attributes_count
    byte[] result = code.toByteArray();
    Stream.set4(result, 4, result.length - 12);
    return result;
  }

  private static byte[] assembleHashCode(List<PoolEntry> pool, int metaId) throws IOException {
    ByteArrayOutputStream code = new ByteArrayOutputStream();
    Stream.write2(code, 2); // max_stack
    Stream.write2(code, 1); // max_locals
    Stream.write4(code, 0); // code_length placeholder

    Stream.write1(code, Assembler.aload_0);
    Stream.write1(code, Assembler.ldc_w);
    Stream.write2(code, ConstantPool.addInteger(pool, metaId) + 1);
    Stream.write1(code, Assembler.invokestatic);
    Stream.write2(code, ConstantPool.addMethodRef(pool, "java/lang/runtime/ObjectMethods",
        "recordHashCode", "(Ljava/lang/Object;I)I") + 1);
    Stream.write1(code, Assembler.ireturn);

    Stream.write2(code, 0); // exception_table_length
    Stream.write2(code, 0); // attributes_count
    byte[] result = code.toByteArray();
    Stream.set4(result, 4, result.length - 12);
    return result;
  }

  private static byte[] assembleEquals(List<PoolEntry> pool, int metaId) throws IOException {
    ByteArrayOutputStream code = new ByteArrayOutputStream();
    Stream.write2(code, 3); // max_stack
    Stream.write2(code, 2); // max_locals
    Stream.write4(code, 0); // code_length placeholder

    Stream.write1(code, Assembler.aload_0);
    Stream.write1(code, Assembler.aload_1);
    Stream.write1(code, Assembler.ldc_w);
    Stream.write2(code, ConstantPool.addInteger(pool, metaId) + 1);
    Stream.write1(code, Assembler.invokestatic);
    Stream.write2(code, ConstantPool.addMethodRef(pool, "java/lang/runtime/ObjectMethods",
        "recordEquals", "(Ljava/lang/Object;Ljava/lang/Object;I)Z") + 1);
    Stream.write1(code, Assembler.ireturn);

    Stream.write2(code, 0); // exception_table_length
    Stream.write2(code, 0); // attributes_count
    byte[] result = code.toByteArray();
    Stream.set4(result, 4, result.length - 12);
    return result;
  }

  public static String recordToString(Object recordInstance, int metaId) {
    RecordMeta meta = registry.get(metaId);
    StringBuilder sb = new StringBuilder();
    sb.append(meta.recordClass.getSimpleName()).append('[');
    for (int i = 0; i < meta.names.length; ++i) {
      if (i > 0) sb.append(", ");
      sb.append(meta.names[i]).append('=');
      try {
        Object val = meta.getters[i].invokeWithArguments(recordInstance);
        sb.append(val);
      } catch (Throwable t) {
        throw new RuntimeException(t);
      }
    }
    sb.append(']');
    return sb.toString();
  }

  public static int recordHashCode(Object recordInstance, int metaId) {
    RecordMeta meta = registry.get(metaId);
    int hash = 0;
    for (int i = 0; i < meta.getters.length; ++i) {
      try {
        Object val = meta.getters[i].invokeWithArguments(recordInstance);
        hash = 31 * hash + (val == null ? 0 : val.hashCode());
      } catch (Throwable t) {
        throw new RuntimeException(t);
      }
    }
    return hash;
  }

  public static boolean recordEquals(Object recordInstance, Object other, int metaId) {
    if (recordInstance == other) return true;
    if (other == null || other.getClass() != recordInstance.getClass()) return false;
    RecordMeta meta = registry.get(metaId);
    for (int i = 0; i < meta.getters.length; ++i) {
      try {
        Object v1 = meta.getters[i].invokeWithArguments(recordInstance);
        Object v2 = meta.getters[i].invokeWithArguments(other);
        if (!Objects.equals(v1, v2)) {
          return false;
        }
      } catch (Throwable t) {
        throw new RuntimeException(t);
      }
    }
    return true;
  }
}
