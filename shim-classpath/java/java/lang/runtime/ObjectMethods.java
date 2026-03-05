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
    Component[] components = components(recordClass, nameList, getters);
    MethodHandle targetHandle = generateTarget(lookup, methodName, methodType, recordClass, metaId, components);

    if (type instanceof MethodType) {
      return new ConstantCallSite(targetHandle);
    } else {
      return targetHandle;
    }
  }

  private static final class Component {
    final String name;
    final String descriptor;

    Component(String name, String descriptor) {
      this.name = name;
      this.descriptor = descriptor;
    }

    char kind() {
      char c = descriptor.charAt(0);
      return c == '[' ? 'L' : c;
    }
  }

  private static Component[] components(Class<?> recordClass, String[] names, MethodHandle[] getters) {
    Component[] result = new Component[names.length];
    for (int i = 0; i < names.length; ++i) {
      try {
        Method accessor = recordClass.getDeclaredMethod(names[i]);
        if (!java.lang.reflect.Modifier.isPublic(accessor.getModifiers())
            || java.lang.reflect.Modifier.isStatic(accessor.getModifiers())) {
          return null;
        }
        String desc = MethodType.methodType(accessor.getReturnType()).toMethodDescriptorString();
        result[i] = new Component(names[i], desc.substring(desc.indexOf(')') + 1));
      } catch (NoSuchMethodException e) {
        return null;
      }
    }
    return result;
  }

  private static MethodHandle generateTarget(
      MethodHandles.Lookup lookup,
      String methodName,
      MethodType methodType,
      Class<?> recordClass,
      int metaId,
      Component[] components) throws Throwable {

    String generatedClassName;
    synchronized (ObjectMethods.class) {
      generatedClassName = recordClass.getName().replace('.', '/') + "$$RecordHelper" + (nextClassId++);
    }
    String recordName = recordClass.getName().replace('.', '/');

    List<PoolEntry> pool = new ArrayList<PoolEntry>();
    byte[] code;

    switch (methodName) {
      case "toString":
        code = assembleToString(pool, metaId);
        break;
      case "hashCode":
        code = components != null ? assembleDirectHashCode(pool, recordName, components) : assembleHashCode(pool, metaId);
        break;
      case "equals":
        code = components != null ? assembleDirectEquals(pool, recordName, components) : assembleEquals(pool, metaId);
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

  private static void op(ByteArrayOutputStream code, int opcode) throws IOException {
    Stream.write1(code, opcode);
  }

  private static void opIndex(ByteArrayOutputStream code, int opcode, int poolIndex) throws IOException {
    Stream.write1(code, opcode);
    Stream.write2(code, poolIndex + 1);
  }

  private static byte[] finishCode(ByteArrayOutputStream code) throws IOException {
    Stream.write2(code, 0);
    Stream.write2(code, 0);
    byte[] result = code.toByteArray();
    Stream.set4(result, 4, result.length - 12);
    return result;
  }

  private static byte[] assembleDirectEquals(List<PoolEntry> pool, String recordName, Component[] components)
      throws IOException {
    ByteArrayOutputStream code = new ByteArrayOutputStream();
    Stream.write2(code, 6);
    Stream.write2(code, 3);
    Stream.write4(code, 0);
    int base = code.size();

    int recordClass = ConstantPool.addClass(pool, recordName);
    op(code, Assembler.aload_0);
    op(code, Assembler.aload_1);
    int branch = code.size();
    op(code, 0xa6);
    Stream.write2(code, 5);
    op(code, 0x04);
    op(code, Assembler.ireturn);
    op(code, Assembler.aload_1);
    opIndex(code, 0xc1, recordClass);
    int instanceBranch = code.size();
    op(code, 0x9a);
    Stream.write2(code, 5);
    op(code, 0x03);
    op(code, Assembler.ireturn);
    op(code, Assembler.aload_1);
    opIndex(code, 0xc0, recordClass);
    op(code, 0x4d);

    List<Integer> falseBranches = new ArrayList<Integer>();
    for (Component c : components) {
      int accessor = ConstantPool.addMethodRef(pool, recordName, c.name, "()" + c.descriptor);
      op(code, Assembler.aload_0);
      opIndex(code, Assembler.invokevirtual, accessor);
      op(code, 0x2c);
      opIndex(code, Assembler.invokevirtual, accessor);
      switch (c.kind()) {
      case 'J':
        op(code, 0x94);
        falseBranches.add(code.size());
        op(code, 0x9a);
        Stream.write2(code, 0);
        break;
      case 'F':
        opIndex(code, Assembler.invokestatic, ConstantPool.addMethodRef(pool, "java/lang/Float", "compare", "(FF)I"));
        falseBranches.add(code.size());
        op(code, 0x9a);
        Stream.write2(code, 0);
        break;
      case 'D':
        opIndex(code, Assembler.invokestatic, ConstantPool.addMethodRef(pool, "java/lang/Double", "compare", "(DD)I"));
        falseBranches.add(code.size());
        op(code, 0x9a);
        Stream.write2(code, 0);
        break;
      case 'L':
        opIndex(code, Assembler.invokestatic,
            ConstantPool.addMethodRef(pool, "java/util/Objects", "equals", "(Ljava/lang/Object;Ljava/lang/Object;)Z"));
        falseBranches.add(code.size());
        op(code, 0x99);
        Stream.write2(code, 0);
        break;
      default:
        falseBranches.add(code.size());
        op(code, 0xa0);
        Stream.write2(code, 0);
        break;
      }
    }
    op(code, 0x04);
    op(code, Assembler.ireturn);
    int falseTarget = code.size();
    op(code, 0x03);
    op(code, Assembler.ireturn);

    byte[] result = finishCode(code);
    for (int at : falseBranches) {
      int offset = falseTarget - at;
      result[at + 1] = (byte) (offset >> 8);
      result[at + 2] = (byte) offset;
    }
    return result;
  }

  private static byte[] assembleDirectHashCode(List<PoolEntry> pool, String recordName, Component[] components)
      throws IOException {
    ByteArrayOutputStream code = new ByteArrayOutputStream();
    Stream.write2(code, 8);
    Stream.write2(code, 2);
    Stream.write4(code, 0);

    op(code, 0x03);
    op(code, 0x3c);
    for (Component c : components) {
      int accessor = ConstantPool.addMethodRef(pool, recordName, c.name, "()" + c.descriptor);
      op(code, 0x1b);
      op(code, 0x10);
      op(code, 31);
      op(code, 0x68);
      op(code, Assembler.aload_0);
      opIndex(code, Assembler.invokevirtual, accessor);
      switch (c.kind()) {
      case 'Z':
        opIndex(code, Assembler.invokestatic, ConstantPool.addMethodRef(pool, "java/lang/Boolean", "hashCode", "(Z)I"));
        break;
      case 'J':
        opIndex(code, Assembler.invokestatic, ConstantPool.addMethodRef(pool, "java/lang/Long", "hashCode", "(J)I"));
        break;
      case 'F':
        opIndex(code, Assembler.invokestatic, ConstantPool.addMethodRef(pool, "java/lang/Float", "hashCode", "(F)I"));
        break;
      case 'D':
        opIndex(code, Assembler.invokestatic, ConstantPool.addMethodRef(pool, "java/lang/Double", "hashCode", "(D)I"));
        break;
      case 'L':
        opIndex(code, Assembler.invokestatic,
            ConstantPool.addMethodRef(pool, "java/util/Objects", "hashCode", "(Ljava/lang/Object;)I"));
        break;
      default:
        break;
      }
      op(code, 0x60);
      op(code, 0x3c);
    }
    op(code, 0x1b);
    op(code, Assembler.ireturn);
    return finishCode(code);
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
