/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.dynalink.linker.support;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import jdk.dynalink.DynamicLinker;
import jdk.dynalink.linker.LinkerServices;

public final class Guards {
  private static final MethodHandle IS_INSTANCE = lookup("isInstance", boolean.class, Class.class, Object.class);
  private static final MethodHandle IS_OF_CLASS = lookup("isOfClass", boolean.class, Class.class, Object.class);
  private static final MethodHandle IS_ARRAY = lookup("isArray", boolean.class, Object.class);
  private static final MethodHandle IS_IDENTICAL = lookup("isIdentical", boolean.class, Object.class, Object.class);
  private static final MethodHandle IS_NULL = lookup("isNull", boolean.class, Object.class);
  private static final MethodHandle IS_NOT_NULL = lookup("isNotNull", boolean.class, Object.class);

  private static final Logger LOG = Logger.getLogger(Guards.class.getName());

  private Guards() {
  }

  private static void warn(String kind, Class<?> clazz, int pos, MethodType type, boolean value) {
    if (LOG.isLoggable(Level.WARNING)) {
      LOG.log(Level.WARNING, kind + " guard " + (clazz == null ? "" : "for " + clazz.getName() + " ") + "in position "
          + pos + " in method type " + type + " at " + DynamicLinker.getLinkedCallSiteLocation()
          + " will always return " + value);
    }
  }

  private static MethodHandle lookup(String name, Class<?> rtype, Class<?>... ptypes) {
    try {
      return MethodHandles.lookup().findStatic(Guards.class, name, MethodType.methodType(rtype, ptypes));
    } catch (NoSuchMethodException | IllegalAccessException e) {
      throw new AssertionError(e);
    }
  }

  public static MethodHandle getClassGuard(Class<?> clazz) {
    return IS_OF_CLASS.bindTo(clazz);
  }

  public static MethodHandle getInstanceOfGuard(Class<?> clazz) {
    return IS_INSTANCE.bindTo(clazz);
  }

  public static MethodHandle getIdentityGuard(Object obj) {
    return IS_IDENTICAL.bindTo(obj);
  }

  public static MethodHandle isNull() {
    return IS_NULL;
  }

  public static MethodHandle isNotNull() {
    return IS_NOT_NULL;
  }

  public static MethodHandle isOfClass(Class<?> clazz, MethodType type) {
    final Class<?> declaredType = type.parameterType(0);
    if (clazz == declaredType) {
      warn("isOfClass", clazz, 0, type, true);
      return constantTrue(type);
    }
    if (!declaredType.isAssignableFrom(clazz)) {
      warn("isOfClass", clazz, 0, type, false);
      return constantFalse(type);
    }
    return getClassBoundArgumentTest(IS_OF_CLASS, clazz, 0, type);
  }

  public static MethodHandle isInstance(Class<?> clazz, MethodType type) {
    return isInstance(clazz, 0, type);
  }

  public static MethodHandle isInstance(Class<?> clazz, int pos, MethodType type) {
    final Class<?> declaredType = type.parameterType(pos);
    if (clazz.isAssignableFrom(declaredType)) {
      warn("isInstance", clazz, pos, type, true);
      return constantTrue(type);
    }
    if (!declaredType.isAssignableFrom(clazz)) {
      warn("isInstance", clazz, pos, type, false);
      return constantFalse(type);
    }
    return getClassBoundArgumentTest(IS_INSTANCE, clazz, pos, type);
  }

  public static MethodHandle isArray(int pos, MethodType type) {
    final Class<?> declaredType = type.parameterType(pos);
    if (declaredType.isArray()) {
      warn("isArray", null, pos, type, true);
      return constantTrue(type);
    }
    if (!declaredType.isAssignableFrom(Object[].class)) {
      warn("isArray", null, pos, type, false);
      return constantFalse(type);
    }
    return asType(IS_ARRAY, pos, type);
  }

  public static MethodHandle asType(MethodHandle test, MethodType type) {
    return test.asType(getTestType(test, type));
  }

  public static MethodHandle asType(LinkerServices linkerServices, MethodHandle test, MethodType type) {
    return linkerServices.asType(test, getTestType(test, type));
  }

  private static MethodType getTestType(MethodHandle test, MethodType type) {
    return type.dropParameterTypes(test.type().parameterCount(), type.parameterCount()).changeReturnType(
        boolean.class);
  }

  private static MethodHandle getClassBoundArgumentTest(MethodHandle test, Class<?> clazz, int pos, MethodType type) {
    return asType(test.bindTo(clazz), pos, type);
  }

  private static MethodHandle asType(MethodHandle test, int pos, MethodType type) {
    assert test != null;
    assert type != null;
    assert type.parameterCount() > 0;
    assert pos >= 0 && pos < type.parameterCount();
    assert test.type().parameterCount() == 1;
    assert test.type().returnType() == boolean.class;
    return MethodHandles.permuteArguments(test.asType(test.type().changeParameterType(0, type.parameterType(pos))),
        type.changeReturnType(boolean.class), new int[] {pos});
  }

  private static MethodHandle constantTrue(MethodType type) {
    return constantBoolean(Boolean.TRUE, type);
  }

  private static MethodHandle constantFalse(MethodType type) {
    return constantBoolean(Boolean.FALSE, type);
  }

  private static MethodHandle constantBoolean(Boolean value, MethodType type) {
    return MethodHandles.permuteArguments(MethodHandles.constant(boolean.class, value),
        type.changeReturnType(boolean.class));
  }

  @SuppressWarnings("unused")
  private static boolean isInstance(Class<?> clazz, Object obj) {
    return clazz.isInstance(obj);
  }

  @SuppressWarnings("unused")
  private static boolean isOfClass(Class<?> clazz, Object obj) {
    return obj != null && obj.getClass() == clazz;
  }

  @SuppressWarnings("unused")
  private static boolean isArray(Object obj) {
    return obj != null && obj.getClass().isArray();
  }

  @SuppressWarnings("unused")
  private static boolean isIdentical(Object obj1, Object obj2) {
    return obj1 == obj2;
  }

  @SuppressWarnings("unused")
  private static boolean isNull(Object obj) {
    return obj == null;
  }

  @SuppressWarnings("unused")
  private static boolean isNotNull(Object obj) {
    return obj != null;
  }
}
