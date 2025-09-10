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
import java.lang.reflect.Member;
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

  public static MethodHandle insertArguments(MethodHandle target, int pos, Object... values) {
    Objects.requireNonNull(target);
    Objects.requireNonNull(values);
    return new MethodHandle.InsertArgumentsMethodHandle(target, pos, values);
  }

  public static MethodHandle filterArguments(MethodHandle target, int pos, MethodHandle... filters) {
    Objects.requireNonNull(target);
    Objects.requireNonNull(filters);
    return new MethodHandle.FilterArgumentsMethodHandle(target, pos, filters);
  }

  public static MethodHandle permuteArguments(MethodHandle target, MethodType newType, int... reorder) {
    Objects.requireNonNull(target);
    Objects.requireNonNull(newType);
    Objects.requireNonNull(reorder);
    return new MethodHandle.PermuteArgumentsMethodHandle(target, newType, reorder);
  }

  public static MethodHandle foldArguments(MethodHandle target, MethodHandle combiner) {
    return foldArguments(target, 0, combiner);
  }

  public static MethodHandle foldArguments(MethodHandle target, int pos, MethodHandle combiner) {
    Objects.requireNonNull(target);
    Objects.requireNonNull(combiner);
    return new MethodHandle.FoldArgumentsMethodHandle(target, pos, combiner);
  }

  public static MethodHandle guardWithTest(MethodHandle test, MethodHandle target, MethodHandle fallback) {
    Objects.requireNonNull(test);
    Objects.requireNonNull(target);
    Objects.requireNonNull(fallback);
    return new MethodHandle.GuardWithTestMethodHandle(test, target, fallback);
  }

  public static MethodHandle catchException(MethodHandle target, Class<? extends Throwable> exType, MethodHandle handler) {
    Objects.requireNonNull(target);
    Objects.requireNonNull(exType);
    Objects.requireNonNull(handler);
    return new MethodHandle.CatchExceptionMethodHandle(target, exType, handler);
  }

  public static MethodHandle throwException(Class<?> returnType, Class<? extends Throwable> exType) {
    Objects.requireNonNull(returnType);
    Objects.requireNonNull(exType);
    return new MethodHandle.ThrowExceptionMethodHandle(returnType, exType);
  }

  public static MethodHandle dropReturn(MethodHandle target) {
    Objects.requireNonNull(target);
    return new MethodHandle.DropReturnMethodHandle(target);
  }

  public static MethodHandle zero(Class<?> type) {
    Objects.requireNonNull(type);
    if (type == void.class) {
      return constant(void.class, null);
    }
    if (type == boolean.class) {
      return constant(boolean.class, Boolean.FALSE);
    }
    if (type == byte.class) {
      return constant(byte.class, Byte.valueOf((byte) 0));
    }
    if (type == short.class) {
      return constant(short.class, Short.valueOf((short) 0));
    }
    if (type == char.class) {
      return constant(char.class, Character.valueOf('\0'));
    }
    if (type == int.class) {
      return constant(int.class, Integer.valueOf(0));
    }
    if (type == long.class) {
      return constant(long.class, Long.valueOf(0L));
    }
    if (type == float.class) {
      return constant(float.class, Float.valueOf(0.0f));
    }
    if (type == double.class) {
      return constant(double.class, Double.valueOf(0.0d));
    }
    return constant(type, null);
  }

  public static MethodHandle empty(MethodType type) {
    Objects.requireNonNull(type);
    MethodHandle z = zero(type.returnType());
    return dropArguments(z, 0, type.parameterList());
  }

  public static MethodHandle arrayConstructor(Class<?> arrayClass) {
    Objects.requireNonNull(arrayClass);
    if (!arrayClass.isArray()) {
      throw new IllegalArgumentException("Not an array class: " + arrayClass);
    }
    return new MethodHandle.ArrayConstructorMethodHandle(arrayClass);
  }

  public static MethodHandle arrayLength(Class<?> arrayClass) {
    Objects.requireNonNull(arrayClass);
    if (!arrayClass.isArray()) {
      throw new IllegalArgumentException("Not an array class: " + arrayClass);
    }
    return new MethodHandle.ArrayLengthMethodHandle(arrayClass);
  }

  public static MethodHandle arrayElementGetter(Class<?> arrayClass) {
    Objects.requireNonNull(arrayClass);
    if (!arrayClass.isArray()) {
      throw new IllegalArgumentException("Not an array class: " + arrayClass);
    }
    return new MethodHandle.ArrayElementGetterMethodHandle(arrayClass);
  }

  public static MethodHandle arrayElementSetter(Class<?> arrayClass) {
    Objects.requireNonNull(arrayClass);
    if (!arrayClass.isArray()) {
      throw new IllegalArgumentException("Not an array class: " + arrayClass);
    }
    return new MethodHandle.ArrayElementSetterMethodHandle(arrayClass);
  }

  public static MethodHandle collectArguments(MethodHandle target, int pos, MethodHandle filter) {
    Objects.requireNonNull(target, "target");
    Objects.requireNonNull(filter, "filter");
    return new MethodHandle.CollectArgumentsMethodHandle(target, pos, filter);
  }

  public static MethodHandle exactInvoker(MethodType type) {
    Objects.requireNonNull(type, "type");
    return new MethodHandle.ExactInvokerMethodHandle(type);
  }

  public static MethodHandle invoker(MethodType type) {
    return exactInvoker(type);
  }

  public static MethodHandle spreadInvoker(MethodType type, int leadingArgCount) {
    Objects.requireNonNull(type, "type");
    if (leadingArgCount < 0 || leadingArgCount > type.parameterCount()) {
      throw new IllegalArgumentException("bad argument count");
    }
    MethodHandle invoker = exactInvoker(type);
    int spreadArgCount = type.parameterCount() - leadingArgCount;
    return invoker.asSpreader(Object[].class, spreadArgCount);
  }

  public static MethodHandle guardWithTest(SwitchPoint switchPoint, MethodHandle target, MethodHandle fallback) {
    Objects.requireNonNull(switchPoint, "switchPoint");
    return switchPoint.guardWithTest(target, fallback);
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

    public Class<?> findClass(String targetName) throws ClassNotFoundException, IllegalAccessException {
      Objects.requireNonNull(targetName, "targetName");
      Class<?> decl = shim.SystemClassLoader.getClass(class_);
      ClassLoader loader = (decl != null) ? decl.getClassLoader() : ClassLoader.getSystemClassLoader();
      return Class.forName(targetName, false, loader);
    }

    public boolean hasPrivateAccess() {
      return (modes & PRIVATE) != 0;
    }

    public Class<?> accessClass(Class<?> targetClass) throws IllegalAccessException {
      Objects.requireNonNull(targetClass, "targetClass");
      return targetClass;
    }

    public Class<?> ensureInitialized(Class<?> targetClass) throws IllegalAccessException {
      Objects.requireNonNull(targetClass, "targetClass");
      if (targetClass.isPrimitive() || targetClass.isArray()) {
        throw new IllegalArgumentException("targetClass is primitive or array: " + targetClass);
      }
      try {
        Class.forName(targetClass.getName(), true, targetClass.getClassLoader());
      } catch (ClassNotFoundException e) {
        throw new NoClassDefFoundError(e.getMessage());
      }
      return targetClass;
    }

    public MethodHandle bind(Object receiver, String name, MethodType type)
        throws NoSuchMethodException, IllegalAccessException {
      Objects.requireNonNull(receiver, "receiver");
      Objects.requireNonNull(name, "name");
      Objects.requireNonNull(type, "type");
      return findVirtual(receiver.getClass(), name, type).bindTo(receiver);
    }

    public MethodHandleInfo revealDirect(MethodHandle target) {
      Objects.requireNonNull(target, "target");
      if (target instanceof MethodHandle.FieldMethodHandle) {
        MethodHandle.FieldMethodHandle fmh = (MethodHandle.FieldMethodHandle) target;
        return new DirectMethodHandleInfo(fmh.kind, fmh.targetField);
      }
      if (target.method != null) {
        return new DirectMethodHandleInfo(target.kind, target.method);
      }
      throw new IllegalArgumentException("not a direct method handle");
    }

    public Class<?> defineClass(byte[] bytes) throws IllegalAccessException {
      Objects.requireNonNull(bytes, "bytes");
      if (!hasFullPrivilegeAccess()) {
        throw new IllegalAccessException("Lookup does not have full privilege access");
      }
      ClassLoader loader = lookupClass().getClassLoader();
      if (loader == null) {
        loader = ClassLoader.getSystemClassLoader();
      }
      return shim.SystemClassLoader.getClass(shim.Classes.defineVMClass(loader, bytes, 0, bytes.length));
    }

    public String toString() {
      return "lookup[" + shim.SystemClassLoader.getClass(class_) + ", "
        + modes + "]";
    }
  }

  static class DirectMethodHandleInfo implements MethodHandleInfo {
    private final int kind;
    private final Member member;
    private final String name;
    private final MethodType type;

    DirectMethodHandleInfo(int kind, Field field) {
      this.kind = kind;
      this.member = field;
      this.name = field.getName();
      Class<?> fieldType = field.getType();
      if (kind == REF_getField || kind == REF_getStatic) {
        this.type = MethodType.methodType(fieldType);
      } else {
        this.type = MethodType.methodType(void.class, fieldType);
      }
    }

    DirectMethodHandleInfo(int kind, shim.VMMethod vmMethod) {
      this.kind = kind;
      Method m = new Method(vmMethod);
      if (kind == REF_newInvokeSpecial) {
        this.member = new Constructor<>(m);
        this.name = "<init>";
        this.type = MethodType.methodType(void.class, m.getParameterTypes());
      } else {
        this.member = m;
        this.name = m.getName();
        this.type = MethodType.methodType(m.getReturnType(), m.getParameterTypes());
      }
    }

    @Override
    public int getReferenceKind() {
      return kind;
    }

    @Override
    public Class<?> getDeclaringClass() {
      return member.getDeclaringClass();
    }

    @Override
    public String getName() {
      return name;
    }

    @Override
    public MethodType getMethodType() {
      return type;
    }

    @Override
    public int getModifiers() {
      return member.getModifiers();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Member> T reflectAs(Class<T> expected, Lookup lookup) {
      Objects.requireNonNull(expected, "expected");
      Objects.requireNonNull(lookup, "lookup");
      if (!expected.isInstance(member)) {
        throw new ClassCastException("Cannot cast " + member.getClass().getName() + " to " + expected.getName());
      }
      return (T) member;
    }

    @Override
    public String toString() {
      return MethodHandleInfo.toString(kind, getDeclaringClass(), name, type);
    }
  }
}
