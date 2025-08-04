/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.invoke;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Objects;

public final class ConstantBootstraps {
  private ConstantBootstraps() {}

  public static Object nullConstant(MethodHandles.Lookup lookup, String name, Class<?> type) {
    Objects.requireNonNull(lookup, "lookup");
    Objects.requireNonNull(name, "name");
    Objects.requireNonNull(type, "type");
    if (type.isPrimitive()) {
      throw new IllegalArgumentException("Primitive type cannot be null: " + type);
    }
    return null;
  }

  public static Class<?> primitiveClass(MethodHandles.Lookup lookup, String name, Class<?> type) {
    Objects.requireNonNull(lookup, "lookup");
    Objects.requireNonNull(name, "name");
    Objects.requireNonNull(type, "type");
    if (type != Class.class) {
      throw new IllegalArgumentException("Type must be Class.class: " + type);
    }
    if (name.length() == 1) {
      switch (name.charAt(0)) {
        case 'Z': return boolean.class;
        case 'B': return byte.class;
        case 'C': return char.class;
        case 'S': return short.class;
        case 'I': return int.class;
        case 'J': return long.class;
        case 'F': return float.class;
        case 'D': return double.class;
        case 'V': return void.class;
      }
    }
    throw new IllegalArgumentException("Invalid primitive name: " + name);
  }

  public static <E extends Enum<E>> E enumConstant(MethodHandles.Lookup lookup, String name, Class<E> type) {
    Objects.requireNonNull(lookup, "lookup");
    Objects.requireNonNull(name, "name");
    Objects.requireNonNull(type, "type");
    return Enum.valueOf(type, name);
  }

  public static Object getStaticFinal(MethodHandles.Lookup lookup, String name, Class<?> type, Class<?> declaringClass) {
    Objects.requireNonNull(lookup, "lookup");
    Objects.requireNonNull(name, "name");
    Objects.requireNonNull(type, "type");
    Objects.requireNonNull(declaringClass, "declaringClass");
    try {
      Field f = declaringClass.getDeclaredField(name);
      if (!Modifier.isStatic(f.getModifiers()) || !Modifier.isFinal(f.getModifiers())) {
        throw new IllegalArgumentException("Field is not static final: " + name);
      }
      f.setAccessible(true);
      return f.get(null);
    } catch (ReflectiveOperationException e) {
      throw new IllegalArgumentException("Field cannot be accessed: " + name, e);
    }
  }

  public static Object getStaticFinal(MethodHandles.Lookup lookup, String name, Class<?> type) {
    return getStaticFinal(lookup, name, type, type);
  }

  public static Object invoke(MethodHandles.Lookup lookup, String name, Class<?> type, MethodHandle handle, Object... args) throws Throwable {
    Objects.requireNonNull(lookup, "lookup");
    Objects.requireNonNull(handle, "handle");
    return handle.invokeWithArguments(args);
  }

  public static VarHandle fieldVarHandle(MethodHandles.Lookup lookup, String name, Class<VarHandle> type, Class<?> declaringClass, Class<?> fieldType) throws NoSuchFieldException, IllegalAccessException {
    Objects.requireNonNull(lookup, "lookup");
    Objects.requireNonNull(name, "name");
    Objects.requireNonNull(declaringClass, "declaringClass");
    Objects.requireNonNull(fieldType, "fieldType");
    return lookup.findVarHandle(declaringClass, name, fieldType);
  }

  public static VarHandle staticFieldVarHandle(MethodHandles.Lookup lookup, String name, Class<VarHandle> type, Class<?> declaringClass, Class<?> fieldType) throws NoSuchFieldException, IllegalAccessException {
    Objects.requireNonNull(lookup, "lookup");
    Objects.requireNonNull(name, "name");
    Objects.requireNonNull(declaringClass, "declaringClass");
    Objects.requireNonNull(fieldType, "fieldType");
    return lookup.findStaticVarHandle(declaringClass, name, fieldType);
  }

  public static VarHandle arrayVarHandle(MethodHandles.Lookup lookup, String name, Class<VarHandle> type, Class<?> arrayClass) {
    Objects.requireNonNull(lookup, "lookup");
    Objects.requireNonNull(arrayClass, "arrayClass");
    return MethodHandles.arrayElementVarHandle(arrayClass);
  }

  public static Object explicitCast(MethodHandles.Lookup lookup, String name, Class<?> type, Object value) {
    Objects.requireNonNull(lookup, "lookup");
    Objects.requireNonNull(type, "type");
    if (value instanceof Number) {
      Number n = (Number) value;
      if (type == byte.class || type == Byte.class) return n.byteValue();
      if (type == short.class || type == Short.class) return n.shortValue();
      if (type == int.class || type == Integer.class) return n.intValue();
      if (type == long.class || type == Long.class) return n.longValue();
      if (type == float.class || type == Float.class) return n.floatValue();
      if (type == double.class || type == Double.class) return n.doubleValue();
      if (type == char.class || type == Character.class) return (char) n.intValue();
    }
    if (value instanceof Character && (type == char.class || type == Character.class)) {
      return value;
    }
    if (value instanceof Boolean && (type == boolean.class || type == Boolean.class)) {
      return value;
    }
    return type.cast(value);
  }
}
