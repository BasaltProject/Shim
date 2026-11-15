/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.reflect;

public class ReflectionFactory {

  private ReflectionFactory() {
  }

  public final java.lang.invoke.MethodHandle defaultReadObjectForSerialization(java.lang.Class<?> p0) {
    throw new UnsupportedOperationException();
  }
  public final java.lang.invoke.MethodHandle defaultWriteObjectForSerialization(java.lang.Class<?> p0) {
    throw new UnsupportedOperationException();
  }
  private static final ReflectionFactory instance = new ReflectionFactory();

  public static sun.reflect.ReflectionFactory getReflectionFactory() {
    return instance;
  }
  public final boolean hasStaticInitializerForSerialization(java.lang.Class<?> p0) {
    throw new UnsupportedOperationException();
  }
  public final java.lang.reflect.Constructor<?> newConstructorForExternalization(java.lang.Class<?> p0) {
    if (!java.io.Externalizable.class.isAssignableFrom(p0)) {
      return null;
    }
    try {
      return p0.getConstructor();
    } catch (NoSuchMethodException e) {
      return null;
    }
  }
  public java.lang.reflect.Constructor<?> newConstructorForSerialization(java.lang.Class<?> p0, java.lang.reflect.Constructor<?> constructor) {
    return new java.lang.reflect.Constructor<Object>(constructor.method, p0);
  }
  public final java.lang.reflect.Constructor<?> newConstructorForSerialization(java.lang.Class<?> p0) {
    Class<?> c = p0;
    while (java.io.Serializable.class.isAssignableFrom(c)) {
      c = c.getSuperclass();
      if (c == null) {
        return null;
      }
    }
    try {
      java.lang.reflect.Constructor<?> k = c.getDeclaredConstructor();
      int m = k.getModifiers();
      if (java.lang.reflect.Modifier.isPrivate(m)) {
        return null;
      }
      return newConstructorForSerialization(p0, k);
    } catch (NoSuchMethodException e) {
      return null;
    }
  }
  public final java.io.OptionalDataException newOptionalDataExceptionForSerialization(boolean flag) {
    throw new UnsupportedOperationException();
  }
  public final java.lang.invoke.MethodHandle readObjectForSerialization(java.lang.Class<?> p0) {
    throw new UnsupportedOperationException();
  }
  public final java.lang.invoke.MethodHandle readObjectNoDataForSerialization(java.lang.Class<?> p0) {
    throw new UnsupportedOperationException();
  }
  public final java.lang.invoke.MethodHandle readResolveForSerialization(java.lang.Class<?> p0) {
    throw new UnsupportedOperationException();
  }
  public final java.io.ObjectStreamField[] serialPersistentFields(java.lang.Class<?> p0) {
    throw new UnsupportedOperationException();
  }
  public final java.lang.invoke.MethodHandle writeObjectForSerialization(java.lang.Class<?> p0) {
    throw new UnsupportedOperationException();
  }
  public final java.lang.invoke.MethodHandle writeReplaceForSerialization(java.lang.Class<?> p0) {
    throw new UnsupportedOperationException();
  }
}
