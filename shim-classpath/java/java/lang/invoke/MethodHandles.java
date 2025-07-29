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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class MethodHandles {
  public static Lookup lookup() {
    return new Lookup(java.lang.reflect.Method.getCaller().class_, 0x5f);
  }

  public static Lookup publicLookup() {
    return new Lookup(shim.Classes.toVMClass(Object.class), 0x20);
  }

  public static VarHandle arrayElementVarHandle(Class<?> arrayClass) {
    Objects.requireNonNull(arrayClass);
    if (!arrayClass.isArray()) {
      throw new IllegalArgumentException("Not an array class: " + arrayClass);
    }
    return VarHandle.makeArrayHandle(arrayClass);
  }

  public static MethodHandle identity(Class<?> type) {
    Objects.requireNonNull(type);
    return new MethodHandle.IdentityMethodHandle(type);
  }

  public static MethodHandle constant(Class<?> type, Object value) {
    Objects.requireNonNull(type);
    return new MethodHandle.ConstantMethodHandle(type, value);
  }

  public static MethodHandle dropArguments(MethodHandle target, int pos, List<Class<?>> valueTypes) {
    Objects.requireNonNull(target);
    Objects.requireNonNull(valueTypes);
    return new MethodHandle.DropArgumentsMethodHandle(target, pos, valueTypes);
  }

  public static MethodHandle dropArguments(MethodHandle target, int pos, Class<?>... valueTypes) {
    return dropArguments(target, pos, Arrays.asList(valueTypes));
  }

  public static MethodHandle filterReturnValue(MethodHandle target, MethodHandle filter) {
    Objects.requireNonNull(target);
    Objects.requireNonNull(filter);
    return new MethodHandle.FilterReturnValueMethodHandle(target, filter);
  }

  public static Lookup privateLookupIn(Class<?> targetClass, Lookup lookup) throws IllegalAccessException {
    Objects.requireNonNull(targetClass);
    Objects.requireNonNull(lookup);
    if (targetClass.isPrimitive() || targetClass.isArray()) {
      throw new IllegalArgumentException("Target class is primitive or array: " + targetClass);
    }
    return new Lookup(shim.Classes.toVMClass(targetClass), 0x5f);
  }

  public static class Lookup {
    public static final int PUBLIC = 0x0001;
    public static final int PRIVATE = 0x0002;
    public static final int PROTECTED = 0x0004;
    public static final int PACKAGE = 0x0008;
    public static final int MODULE = 0x0010;
    public static final int UNCONDITIONAL = 0x0020;
    public static final int ORIGINAL_FULL_RESOURCE_ACCESS = 0x0040;

    final shim.VMClass class_;
    private final int modes;

    Lookup(shim.VMClass class_, int modes) {
      this.class_ = class_;
      this.modes = modes;
    }

    public Class<?> lookupClass() {
      return shim.SystemClassLoader.getClass(class_);
    }

    public int lookupModes() {
      return modes;
    }

    public boolean hasFullPrivilegeAccess() {
      return (modes & 0x12) == 0x12;
    }

    public Lookup in(Class<?> requestedLookupClass) {
      Objects.requireNonNull(requestedLookupClass);
      if (requestedLookupClass == lookupClass()) {
        return this;
      }
      int newModes = modes & ~(PROTECTED | PRIVATE);
      if (!requestedLookupClass.getPackageName().equals(lookupClass().getPackageName())) {
        newModes &= ~PACKAGE;
      }
      return new Lookup(shim.Classes.toVMClass(requestedLookupClass), newModes);
    }

    public Lookup dropLookupMode(int modeToDrop) {
      return new Lookup(class_, modes & ~modeToDrop);
    }

    public MethodHandle findVirtual(Class<?> refc, String name, MethodType type)
        throws NoSuchMethodException, IllegalAccessException {
      Objects.requireNonNull(refc);
      Objects.requireNonNull(name);
      Objects.requireNonNull(type);
      if (name.startsWith("<")) {
        throw new NoSuchMethodException(name);
      }
      Method m = refc.getMethod(name, type.parameterArray());
      if (!m.getReturnType().equals(type.returnType())) {
        throw new NoSuchMethodException(name);
      }
      if ((m.getModifiers() & Modifier.STATIC) != 0) {
        throw new IllegalAccessException("Method is static: " + name);
      }
      int kind = refc.isInterface() ? MethodHandle.REF_invokeInterface : MethodHandle.REF_invokeVirtual;
      return new MethodHandle(kind, refc.getClassLoader(), m.vmMethod);
    }

    public MethodHandle findStatic(Class<?> refc, String name, MethodType type)
        throws NoSuchMethodException, IllegalAccessException {
      Objects.requireNonNull(refc);
      Objects.requireNonNull(name);
      Objects.requireNonNull(type);
      if (name.startsWith("<")) {
        throw new NoSuchMethodException(name);
      }
      Method m = null;
      try {
        m = refc.getDeclaredMethod(name, type.parameterArray());
      } catch (NoSuchMethodException e) {
        m = refc.getMethod(name, type.parameterArray());
      }
      if (!m.getReturnType().equals(type.returnType())) {
        throw new NoSuchMethodException(name);
      }
      if ((m.getModifiers() & Modifier.STATIC) == 0) {
        throw new IllegalAccessException("Method is not static: " + name);
      }
      return new MethodHandle(MethodHandle.REF_invokeStatic, refc.getClassLoader(), m.vmMethod);
    }

    public MethodHandle findSpecial(Class<?> refc, String name, MethodType type, Class<?> specialCaller)
        throws NoSuchMethodException, IllegalAccessException {
      Objects.requireNonNull(refc);
      Objects.requireNonNull(name);
      Objects.requireNonNull(type);
      Objects.requireNonNull(specialCaller);
      if (name.startsWith("<")) {
        throw new NoSuchMethodException(name);
      }
      Method m = refc.getDeclaredMethod(name, type.parameterArray());
      if (!m.getReturnType().equals(type.returnType())) {
        throw new NoSuchMethodException(name);
      }
      return new MethodHandle(MethodHandle.REF_invokeSpecial, refc.getClassLoader(), m.vmMethod);
    }

    public MethodHandle findConstructor(Class<?> refc, MethodType type)
        throws NoSuchMethodException, IllegalAccessException {
      Objects.requireNonNull(refc);
      Objects.requireNonNull(type);
      if (type.returnType() != void.class) {
        throw new NoSuchMethodException("Constructor must return void");
      }
      Constructor<?> c = refc.getDeclaredConstructor(type.parameterArray());
      return new MethodHandle(MethodHandle.REF_newInvokeSpecial, refc.getClassLoader(), c.method.vmMethod);
    }

    public MethodHandle unreflect(Method m) throws IllegalAccessException {
      Objects.requireNonNull(m);
      if ((m.getModifiers() & Modifier.STATIC) != 0) {
        return new MethodHandle(MethodHandle.REF_invokeStatic, m.getDeclaringClass().getClassLoader(), m.vmMethod);
      }
      int kind = m.getDeclaringClass().isInterface() ? MethodHandle.REF_invokeInterface : MethodHandle.REF_invokeVirtual;
      return new MethodHandle(kind, m.getDeclaringClass().getClassLoader(), m.vmMethod);
    }

    public MethodHandle unreflectSpecial(Method m, Class<?> specialCaller)
        throws IllegalAccessException {
      Objects.requireNonNull(m);
      return new MethodHandle(MethodHandle.REF_invokeSpecial, m.getDeclaringClass().getClassLoader(), m.vmMethod);
    }

    public MethodHandle unreflectConstructor(Constructor<?> c) throws IllegalAccessException {
      Objects.requireNonNull(c);
      return new MethodHandle(MethodHandle.REF_newInvokeSpecial, c.getDeclaringClass().getClassLoader(), c.method.vmMethod);
    }

    public MethodHandle findGetter(Class<?> refc, String name, Class<?> type)
        throws NoSuchFieldException, IllegalAccessException {
      Field f = refc.getField(name);
      if (!f.getType().equals(type) || (f.getModifiers() & Modifier.STATIC) != 0) {
        throw new NoSuchFieldException(name);
      }
      return MethodHandle.makeFieldHandle(MethodHandle.REF_getField, f);
    }

    public MethodHandle findSetter(Class<?> refc, String name, Class<?> type)
        throws NoSuchFieldException, IllegalAccessException {
      Field f = refc.getField(name);
      if (!f.getType().equals(type) || (f.getModifiers() & Modifier.STATIC) != 0) {
        throw new NoSuchFieldException(name);
      }
      return MethodHandle.makeFieldHandle(MethodHandle.REF_putField, f);
    }

    public MethodHandle findStaticGetter(Class<?> refc, String name, Class<?> type)
        throws NoSuchFieldException, IllegalAccessException {
      Field f = refc.getField(name);
      if (!f.getType().equals(type) || (f.getModifiers() & Modifier.STATIC) == 0) {
        throw new NoSuchFieldException(name);
      }
      return MethodHandle.makeFieldHandle(MethodHandle.REF_getStatic, f);
    }

    public MethodHandle findStaticSetter(Class<?> refc, String name, Class<?> type)
        throws NoSuchFieldException, IllegalAccessException {
      Field f = refc.getField(name);
      if (!f.getType().equals(type) || (f.getModifiers() & Modifier.STATIC) == 0) {
        throw new NoSuchFieldException(name);
      }
      return MethodHandle.makeFieldHandle(MethodHandle.REF_putStatic, f);
    }

    public MethodHandle unreflectGetter(Field f) throws IllegalAccessException {
      Objects.requireNonNull(f);
      int kind = (f.getModifiers() & Modifier.STATIC) != 0
          ? MethodHandle.REF_getStatic
          : MethodHandle.REF_getField;
      return MethodHandle.makeFieldHandle(kind, f);
    }

    public MethodHandle unreflectSetter(Field f) throws IllegalAccessException {
      Objects.requireNonNull(f);
      int kind = (f.getModifiers() & Modifier.STATIC) != 0
          ? MethodHandle.REF_putStatic
          : MethodHandle.REF_putField;
      return MethodHandle.makeFieldHandle(kind, f);
    }

    public VarHandle findVarHandle(Class<?> recv, String name, Class<?> type)
        throws NoSuchFieldException, IllegalAccessException {
      Field f = recv.getField(name);
      if (!f.getType().equals(type) || (f.getModifiers() & Modifier.STATIC) != 0) {
        throw new NoSuchFieldException(name);
      }
      return VarHandle.makeFieldHandle(f, false);
    }

    public VarHandle findStaticVarHandle(Class<?> decl, String name, Class<?> type)
        throws NoSuchFieldException, IllegalAccessException {
      Field f = decl.getField(name);
      if (!f.getType().equals(type) || (f.getModifiers() & Modifier.STATIC) == 0) {
        throw new NoSuchFieldException(name);
      }
      return VarHandle.makeFieldHandle(f, true);
    }

    public VarHandle unreflectVarHandle(Field f) throws IllegalAccessException {
      Objects.requireNonNull(f);
      return VarHandle.makeFieldHandle(f, (f.getModifiers() & Modifier.STATIC) != 0);
    }

    public String toString() {
      return "lookup[" + shim.SystemClassLoader.getClass(class_) + ", "
        + modes + "]";
    }
  }
}
