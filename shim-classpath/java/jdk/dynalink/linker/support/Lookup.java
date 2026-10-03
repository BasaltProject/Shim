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
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class Lookup {
  private final MethodHandles.Lookup lookup;

  public static final Lookup PUBLIC = new Lookup(MethodHandles.publicLookup());

  public Lookup(MethodHandles.Lookup lookup) {
    this.lookup = lookup;
  }

  public MethodHandle unreflect(Method m) {
    return unreflect(lookup, m);
  }

  public static MethodHandle unreflect(MethodHandles.Lookup lookup, Method m) {
    try {
      return lookup.unreflect(m);
    } catch (IllegalAccessException e) {
      final IllegalAccessError ee = new IllegalAccessError("Failed to unreflect method " + m);
      ee.initCause(e);
      throw ee;
    }
  }

  public MethodHandle unreflectGetter(Field f) {
    try {
      return lookup.unreflectGetter(f);
    } catch (IllegalAccessException e) {
      final IllegalAccessError ee = new IllegalAccessError("Failed to unreflect getter for field " + f);
      ee.initCause(e);
      throw ee;
    }
  }

  public MethodHandle unreflectSetter(Field f) {
    try {
      return lookup.unreflectSetter(f);
    } catch (IllegalAccessException e) {
      final IllegalAccessError ee = new IllegalAccessError("Failed to unreflect setter for field " + f);
      ee.initCause(e);
      throw ee;
    }
  }

  public MethodHandle unreflectConstructor(Constructor<?> c) {
    return unreflectConstructor(lookup, c);
  }

  public static MethodHandle unreflectConstructor(MethodHandles.Lookup lookup, Constructor<?> c) {
    try {
      return lookup.unreflectConstructor(c);
    } catch (IllegalAccessException e) {
      final IllegalAccessError ee = new IllegalAccessError("Failed to unreflect constructor " + c);
      ee.initCause(e);
      throw ee;
    }
  }

  public MethodHandle findGetter(Class<?> refc, String name, Class<?> type) {
    try {
      return lookup.findGetter(refc, name, type);
    } catch (NoSuchFieldException e) {
      final NoSuchFieldError ee = new NoSuchFieldError("Failed to find getter for field " + refc.getName() + "."
          + name + " of type " + type.getName());
      ee.initCause(e);
      throw ee;
    } catch (IllegalAccessException e) {
      final IllegalAccessError ee = new IllegalAccessError("Failed to access getter for field " + refc.getName()
          + "." + name + " of type " + type.getName());
      ee.initCause(e);
      throw ee;
    }
  }

  public MethodHandle findStatic(Class<?> declaringClass, String name, MethodType type) {
    try {
      return lookup.findStatic(declaringClass, name, type);
    } catch (NoSuchMethodException e) {
      final NoSuchMethodError ee = new NoSuchMethodError("Failed to find static method " + methodDescription(
          declaringClass, name, type));
      ee.initCause(e);
      throw ee;
    } catch (IllegalAccessException e) {
      final IllegalAccessError ee = new IllegalAccessError("Failed to access static method " + methodDescription(
          declaringClass, name, type));
      ee.initCause(e);
      throw ee;
    }
  }

  public MethodHandle findVirtual(Class<?> declaringClass, String name, MethodType type) {
    try {
      return lookup.findVirtual(declaringClass, name, type);
    } catch (NoSuchMethodException e) {
      final NoSuchMethodError ee = new NoSuchMethodError("Failed to find virtual method " + methodDescription(
          declaringClass, name, type));
      ee.initCause(e);
      throw ee;
    } catch (IllegalAccessException e) {
      final IllegalAccessError ee = new IllegalAccessError("Failed to access virtual method " + methodDescription(
          declaringClass, name, type));
      ee.initCause(e);
      throw ee;
    }
  }

  public MethodHandle findSpecial(Class<?> declaringClass, String name, MethodType type) {
    try {
      return lookup.findSpecial(declaringClass, name, type, declaringClass);
    } catch (NoSuchMethodException e) {
      final NoSuchMethodError ee = new NoSuchMethodError("Failed to find special method " + methodDescription(
          declaringClass, name, type));
      ee.initCause(e);
      throw ee;
    } catch (IllegalAccessException e) {
      final IllegalAccessError ee = new IllegalAccessError("Failed to access special method " + methodDescription(
          declaringClass, name, type));
      ee.initCause(e);
      throw ee;
    }
  }

  private static String methodDescription(Class<?> clazz, String name, MethodType type) {
    return clazz.getName() + "#" + name + type;
  }

  public MethodHandle findOwnSpecial(String name, Class<?> rtype, Class<?>... ptypes) {
    return findOwnSpecial(lookup, name, rtype, ptypes);
  }

  public static MethodHandle findOwnSpecial(MethodHandles.Lookup lookup, String name, Class<?> rtype,
      Class<?>... ptypes) {
    return new Lookup(lookup).findSpecial(lookup.lookupClass(), name, MethodType.methodType(rtype, ptypes));
  }

  public MethodHandle findOwnStatic(String name, Class<?> rtype, Class<?>... ptypes) {
    return findOwnStatic(lookup, name, rtype, ptypes);
  }

  public static MethodHandle findOwnStatic(MethodHandles.Lookup lookup, String name, Class<?> rtype,
      Class<?>... ptypes) {
    return new Lookup(lookup).findStatic(lookup.lookupClass(), name, MethodType.methodType(rtype, ptypes));
  }
}
