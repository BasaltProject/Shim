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

import shim.Classes;
import shim.SystemClassLoader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class MethodHandle implements java.lang.constant.Constable {
  public static final int REF_getField = 1;
  public static final int REF_getStatic = 2;
  public static final int REF_putField = 3;
  public static final int REF_putStatic = 4;
  public static final int REF_invokeVirtual = 5;
  public static final int REF_invokeStatic = 6;
  public static final int REF_invokeSpecial = 7;
  public static final int REF_newInvokeSpecial = 8;
  public static final int REF_invokeInterface = 9;

  final int kind;
  private final ClassLoader loader;
  final shim.VMMethod method;
  protected volatile MethodType type;

  public MethodHandle(int kind, ClassLoader loader, shim.VMMethod method) {
    this.kind = kind;
    this.loader = loader;
    this.method = method;
  }

  MethodHandle(String class_,
               String name,
               String spec,
               int kind)
  {
    this.kind = kind;
    this.loader = SystemClassLoader.appLoader();
    try {
      this.method = Classes.findMethod(this.loader, class_, name, spec);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  protected MethodHandle(MethodType type) {
    this(0, null, null);
    this.type = type;
  }

  static MethodHandle makeFieldHandle(int kind, Field field) {
    return new FieldMethodHandle(kind, field);
  }

  public static MethodHandle makeFieldHandle(int kind, shim.VMField vmField) {
    return new FieldMethodHandle(kind, new Field(vmField));
  }

  public MethodType type() {
    if (type == null) {
      if (method != null) {
        MethodType resolved = new MethodType(loader, method.spec);
        if (kind != REF_invokeStatic) {
          Class owner = SystemClassLoader.getClass(method.class_);
          resolved = kind == REF_newInvokeSpecial
              ? resolved.changeReturnType(owner)
              : resolved.insertParameterTypes(0, owner);
        }
        type = resolved;
      }
    }
    return type;
  }

  public Object invokeWithArguments(Object... arguments) throws Throwable {
    if (arguments == null) {
      arguments = new Object[0];
    }
    try {
      if (method != null) {
        switch (kind) {
        case REF_invokeStatic:
          return new Method(method).invoke(null, arguments);
        case REF_invokeVirtual:
        case REF_invokeInterface: {
          if (arguments.length < 1) {
            throw new IllegalArgumentException("Missing receiver instance");
          }
          Object instance = arguments[0];
          Object[] rest = new Object[arguments.length - 1];
          System.arraycopy(arguments, 1, rest, 0, rest.length);
          return new Method(method).invoke(instance, rest);
        }
        case REF_invokeSpecial: {
          if (arguments.length < 1) {
            throw new IllegalArgumentException("Missing receiver instance");
          }
          Object instance = arguments[0];
          Object[] rest = new Object[arguments.length - 1];
          System.arraycopy(arguments, 1, rest, 0, rest.length);
          return new Method(method).invokeNonVirtual(instance, rest);
        }
        case REF_newInvokeSpecial: {
          Constructor<?> ctor = new Constructor(new Method(method));
          return ctor.newInstance(arguments);
        }
        default:
          throw new UnsupportedOperationException("Unsupported kind: " + kind);
        }
      }

      throw new UnsupportedOperationException();
    } catch (InvocationTargetException e) {
      Throwable target = e.getTargetException();
      if (target != null) {
        throw target;
      }
      throw e;
    }
  }

  public Object invokeWithArguments(List<?> arguments) throws Throwable {
    return invokeWithArguments(arguments != null ? arguments.toArray() : new Object[0]);
  }

  public MethodHandle asType(MethodType newType) {
    if (newType.equals(type())) {
      return this;
    }
    return new AsTypeMethodHandle(this, newType);
  }

  public MethodHandle bindTo(Object x) {
    return new BoundMethodHandle(this, x);
  }

  public boolean isVarargsCollector() {
    return false;
  }

  public MethodHandle asFixedArity() {
    return this;
  }

  public MethodHandle asVarargsCollector(Class<?> arrayType) {
    Objects.requireNonNull(arrayType, "arrayType");
    if (!arrayType.isArray()) {
      throw new IllegalArgumentException("not an array type: " + arrayType);
    }
    int paramCount = type().parameterCount();
    if (paramCount == 0 || !type().parameterType(paramCount - 1).isAssignableFrom(arrayType)) {
      throw new IllegalArgumentException("last parameter type not compatible with array type");
    }
    return new VarargsCollectorMethodHandle(this, arrayType);
  }

  public final Object invokeExact(Object... args) throws Throwable {
    return invokeWithArguments(args);
  }

  public final Object invoke(Object... args) throws Throwable {
    return invokeWithArguments(args);
  }

  public MethodHandle withVarargs(boolean makeVarargs) {
    if (makeVarargs == isVarargsCollector()) {
      return this;
    }
    if (makeVarargs) {
      int n = type().parameterCount();
      if (n == 0 || !type().parameterType(n - 1).isArray()) {
        throw new IllegalArgumentException("not an array type: " + (n == 0 ? "none" : type().parameterType(n - 1)));
      }
      return asVarargsCollector(type().parameterType(n - 1));
    }
    return asFixedArity();
  }

  public MethodHandle asSpreader(int pos, Class<?> arrayType, int arrayLength) {
    Objects.requireNonNull(arrayType, "arrayType");
    if (!arrayType.isArray()) {
      throw new IllegalArgumentException("not an array type: " + arrayType);
    }
    MethodType targetType = type();
    int paramCount = targetType.parameterCount();
    if (pos < 0 || pos > paramCount || arrayLength > paramCount - pos) {
      throw new IllegalArgumentException("bad spread position");
    }
    if (arrayLength < 0) {
      throw new IllegalArgumentException("bad spread array length");
    }
    MethodType newType = targetType.dropParameterTypes(pos, pos + arrayLength).insertParameterTypes(pos, arrayType);
    return new SpreaderMethodHandle(this, newType, pos, arrayLength);
  }

  public MethodHandle asCollector(int pos, Class<?> arrayType, int arrayLength) {
    Objects.requireNonNull(arrayType, "arrayType");
    if (!arrayType.isArray()) {
      throw new IllegalArgumentException("not an array type: " + arrayType);
    }
    if (arrayLength < 0) {
      throw new IllegalArgumentException("bad collector length: " + arrayLength);
    }
    MethodType targetType = type();
    if (pos < 0 || pos >= targetType.parameterCount()) {
      throw new IllegalArgumentException("bad collect position");
    }
    if (!targetType.parameterType(pos).isAssignableFrom(arrayType)) {
      throw new IllegalArgumentException("array type not assignable to argument: " + this + ", " + arrayType);
    }
    Class<?> componentType = arrayType.getComponentType();
    Class<?>[] paramTypes = new Class<?>[arrayLength];
    Arrays.fill(paramTypes, componentType);
    MethodType newType = targetType.dropParameterTypes(pos, pos + 1).insertParameterTypes(pos, paramTypes);
    return new CollectorMethodHandle(this, newType, pos, arrayType, arrayLength);
  }

  interface Body {
    Object invoke(Object[] arguments) throws Throwable;
  }

  static class FunctionMethodHandle extends MethodHandle {
    private final Body body;

    FunctionMethodHandle(MethodType type, Body body) {
      super(type);
      this.body = body;
    }

    @Override
    public Object invokeWithArguments(Object... arguments) throws Throwable {
      return body.invoke(arguments);
    }
  }

  public MethodHandle asSpreader(Class<?> arrayType, int arrayLength) {
    Objects.requireNonNull(arrayType, "arrayType");
    if (!arrayType.isArray()) {
      throw new IllegalArgumentException("not an array type: " + arrayType);
    }
    MethodType targetType = type();
    int paramCount = targetType.parameterCount();
    if (arrayLength < 0 || arrayLength > paramCount) {
      throw new IllegalArgumentException("bad spread length: " + arrayLength);
    }
    int spreadPos = paramCount - arrayLength;
    MethodType newType = targetType.dropParameterTypes(spreadPos, paramCount).insertParameterTypes(spreadPos, arrayType);
    return new SpreaderMethodHandle(this, newType, spreadPos, arrayLength);
  }

  public MethodHandle asCollector(Class<?> arrayType, int arrayLength) {
    Objects.requireNonNull(arrayType, "arrayType");
    if (!arrayType.isArray()) {
      throw new IllegalArgumentException("not an array type: " + arrayType);
    }
    if (arrayLength < 0) {
      throw new IllegalArgumentException("bad collector length: " + arrayLength);
    }
    MethodType targetType = type();
    int collectPos = targetType.parameterCount() - 1;
    Class<?> componentType = arrayType.getComponentType();
    Class<?>[] paramTypes = new Class<?>[arrayLength];
    Arrays.fill(paramTypes, componentType);
    MethodType newType = targetType.dropParameterTypes(collectPos, collectPos + 1).insertParameterTypes(collectPos, paramTypes);
    return new CollectorMethodHandle(this, newType, collectPos, arrayType, arrayLength);
  }

  public String toString() {
    return "MethodHandle" + type();
  }

  static class FieldMethodHandle extends MethodHandle {
    final Field targetField;

    FieldMethodHandle(int kind, Field targetField) {
      super(kind, targetField.getDeclaringClass().getClassLoader(), null);
      this.targetField = targetField;
    }

    @Override
    public MethodType type() {
      if (type == null) {
        Class<?> fieldType = targetField.getType();
        Class<?> declaring = targetField.getDeclaringClass();
        switch (kind) {
        case REF_getField:
          type = MethodType.methodType(fieldType, declaring);
          break;
        case REF_getStatic:
          type = MethodType.methodType(fieldType);
          break;
        case REF_putField:
          type = MethodType.methodType(void.class, new Class<?>[] { declaring, fieldType });
          break;
        case REF_putStatic:
          type = MethodType.methodType(void.class, fieldType);
          break;
        default:
          throw new InternalError();
        }
      }
      return type;
    }

    @Override
    public Object invokeWithArguments(Object... arguments) throws Throwable {
      if (arguments == null) {
        arguments = new Object[0];
      }
      switch (kind) {
      case REF_getField:
        return targetField.get(arguments[0]);
      case REF_getStatic:
        return targetField.get(null);
      case REF_putField:
        targetField.set(arguments[0], arguments[1]);
        return null;
      case REF_putStatic:
        targetField.set(null, arguments[0]);
        return null;
      default:
        throw new InternalError();
      }
    }

    @Override
    public String toString() {
      return "MethodHandle[" + targetField.toString() + "]";
    }
  }

  private static class AsTypeMethodHandle extends MethodHandle {
    private static final String kinds = "ZBSCIJFD";

    private final MethodHandle target;

    AsTypeMethodHandle(MethodHandle target, MethodType newType) {
      super(newType);
      this.target = target;
      MethodType old = target.type();
      if (old.parameterCount() != newType.parameterCount()) throw mismatch(old, newType);
      for (int i = 0; i < old.parameterCount(); ++i) {
        if (!canConvert(newType.parameterType(i), old.parameterType(i))) throw mismatch(old, newType);
      }
      if (!canConvert(old.returnType(), newType.returnType())) throw mismatch(old, newType);
    }

    private static WrongMethodTypeException mismatch(MethodType from, MethodType to) {
      return new WrongMethodTypeException("cannot convert MethodHandle" + from + " to " + to);
    }

    private static int kind(Class<?> c) {
      return c.isPrimitive() && c != void.class ? kinds.indexOf(c.getName().equals("boolean") ? 'Z'
          : c.getName().equals("byte") ? 'B' : c.getName().equals("short") ? 'S' : c.getName().equals("char") ? 'C'
          : c.getName().equals("int") ? 'I' : c.getName().equals("long") ? 'J'
          : c.getName().equals("float") ? 'F' : 'D') : -1;
    }

    private static Class<?> wrapper(Class<?> c) {
      switch (kind(c)) {
      case 0: return Boolean.class;
      case 1: return Byte.class;
      case 2: return Short.class;
      case 3: return Character.class;
      case 4: return Integer.class;
      case 5: return Long.class;
      case 6: return Float.class;
      case 7: return Double.class;
      default: return c;
      }
    }

    private static int unwrapped(Class<?> c) {
      if (c == Boolean.class) return 0;
      if (c == Byte.class) return 1;
      if (c == Short.class) return 2;
      if (c == Character.class) return 3;
      if (c == Integer.class) return 4;
      if (c == Long.class) return 5;
      if (c == Float.class) return 6;
      if (c == Double.class) return 7;
      return -1;
    }

    private static boolean widens(int from, int to) {
      if (from == to) return true;
      if (from == 0 || to == 0) return false;
      switch (from) {
      case 1: return to == 2 || to >= 4;
      case 2: return to >= 4;
      case 3: return to >= 4;
      case 4: return to >= 5;
      case 5: return to >= 6;
      case 6: return to == 7;
      default: return false;
      }
    }

    private static boolean canConvert(Class<?> from, Class<?> to) {
      if (from == to || to == void.class) return true;
      if (from == void.class) return true;
      int fromKind = kind(from);
      int toKind = kind(to);
      if (fromKind >= 0 && toKind >= 0) return widens(fromKind, toKind);
      if (fromKind >= 0) return to.isAssignableFrom(wrapper(from));
      if (toKind >= 0) {
        int sourceKind = unwrapped(from);
        return sourceKind >= 0 ? widens(sourceKind, toKind) : from.isAssignableFrom(wrapper(to));
      }
      return true;
    }

    private static Object convert(Object value, Class<?> to) {
      if (to == void.class) return null;
      int toKind = kind(to);
      if (toKind < 0) return to.isInstance(value) || value == null ? value : to.cast(value);
      if (value == null) throw new NullPointerException();
      int fromKind = unwrapped(value.getClass());
      if (fromKind < 0 || !widens(fromKind, toKind)) throw new ClassCastException(value.getClass().getName());
      if (fromKind == toKind) return value;
      long integral = value instanceof Character ? (Character) value : ((Number) value).longValue();
      switch (toKind) {
      case 2: return (short) integral;
      case 4: return (int) integral;
      case 5: return integral;
      case 6: return value instanceof Number && fromKind >= 6 ? ((Number) value).floatValue() : (float) integral;
      default: return value instanceof Number && fromKind >= 6 ? ((Number) value).doubleValue() : (double) integral;
      }
    }

    @Override
    public Object invokeWithArguments(Object... arguments) throws Throwable {
      MethodType old = target.type();
      if (arguments.length != old.parameterCount()) throw new WrongMethodTypeException("wrong argument count");
      Object[] converted = new Object[arguments.length];
      for (int i = 0; i < arguments.length; ++i) converted[i] = convert(arguments[i], old.parameterType(i));
      return convert(target.invokeWithArguments(converted), type().returnType());
    }
  }

  private static class BoundMethodHandle extends MethodHandle {
    private final MethodHandle target;
    private final Object bound;

    BoundMethodHandle(MethodHandle target, Object bound) {
      super(target.type().dropParameterTypes(0, 1));
      this.target = target;
      this.bound = bound;
    }

    @Override
    public Object invokeWithArguments(Object... arguments) throws Throwable {
      Object[] fullArgs = new Object[arguments.length + 1];
      fullArgs[0] = bound;
      System.arraycopy(arguments, 0, fullArgs, 1, arguments.length);
      return target.invokeWithArguments(fullArgs);
    }
  }

  static class DropArgumentsMethodHandle extends MethodHandle {
    private final MethodHandle target;
    private final int pos;
    private final int count;

    DropArgumentsMethodHandle(MethodHandle target, int pos, List<Class<?>> valueTypes) {
      super(target.type().insertParameterTypes(pos, valueTypes));
      this.target = target;
      this.pos = pos;
      this.count = valueTypes.size();
    }

    @Override
    public Object invokeWithArguments(Object... arguments) throws Throwable {
      Object[] rest = new Object[arguments.length - count];
      System.arraycopy(arguments, 0, rest, 0, pos);
      System.arraycopy(arguments, pos + count, rest, pos, arguments.length - pos - count);
      return target.invokeWithArguments(rest);
    }
  }

  static class FilterReturnValueMethodHandle extends MethodHandle {
    private final MethodHandle target;
    private final MethodHandle filter;

    FilterReturnValueMethodHandle(MethodHandle target, MethodHandle filter) {
      super(target.type().changeReturnType(filter.type().returnType()));
      this.target = target;
      this.filter = filter;
    }

    @Override
    public Object invokeWithArguments(Object... arguments) throws Throwable {
      Object res = target.invokeWithArguments(arguments);
      return filter.invokeWithArguments(res);
    }
  }

  static class ConstantMethodHandle extends MethodHandle {
    private final Object value;

    ConstantMethodHandle(Class<?> type, Object value) {
      super(MethodType.methodType(type));
      this.value = value;
    }

    @Override
    public Object invokeWithArguments(Object... arguments) throws Throwable {
      return value;
    }
  }

  static class IdentityMethodHandle extends MethodHandle {
    IdentityMethodHandle(Class<?> type) {
      super(MethodType.methodType(type, type));
    }

    @Override
    public Object invokeWithArguments(Object... arguments) throws Throwable {
      return arguments[0];
    }
  }

  static class InsertArgumentsMethodHandle extends MethodHandle {
    private final MethodHandle target;
    private final int pos;
    private final Object[] values;

    InsertArgumentsMethodHandle(MethodHandle target, int pos, Object[] values) {
      super(computeType(target, pos, values));
      this.target = target;
      this.pos = pos;
      this.values = values.clone();
    }

    private static MethodType computeType(MethodHandle target, int pos, Object[] values) {
      MethodType targetType = target.type();
      int count = values.length;
      if (pos < 0 || pos + count > targetType.parameterCount()) {
        throw new IllegalArgumentException("insertArguments out of bounds");
      }
      return targetType.dropParameterTypes(pos, pos + count);
    }

    @Override
    public Object invokeWithArguments(Object... arguments) throws Throwable {
      Object[] targetArgs = new Object[arguments.length + values.length];
      System.arraycopy(arguments, 0, targetArgs, 0, pos);
      System.arraycopy(values, 0, targetArgs, pos, values.length);
      System.arraycopy(arguments, pos, targetArgs, pos + values.length, arguments.length - pos);
      return target.invokeWithArguments(targetArgs);
    }
  }

  static class FilterArgumentsMethodHandle extends MethodHandle {
    private final MethodHandle target;
    private final int pos;
    private final MethodHandle[] filters;

    FilterArgumentsMethodHandle(MethodHandle target, int pos, MethodHandle[] filters) {
      super(computeType(target, pos, filters));
      this.target = target;
      this.pos = pos;
      this.filters = filters.clone();
    }

    private static MethodType computeType(MethodHandle target, int pos, MethodHandle[] filters) {
      MethodType targetType = target.type();
      int paramCount = targetType.parameterCount();
      if (pos < 0 || pos + filters.length > paramCount) {
        throw new IllegalArgumentException("filterArguments out of range");
      }
      Class<?>[] newParams = new Class<?>[paramCount];
      for (int i = 0; i < paramCount; i++) {
        if (i >= pos && i < pos + filters.length) {
          MethodHandle filter = filters[i - pos];
          if (filter != null) {
            if (filter.type().parameterCount() != 1) {
              throw new IllegalArgumentException("filter must have arity 1: " + filter.type());
            }
            newParams[i] = filter.type().parameterType(0);
          } else {
            newParams[i] = targetType.parameterType(i);
          }
        } else {
          newParams[i] = targetType.parameterType(i);
        }
      }
      return MethodType.methodType(targetType.returnType(), newParams);
    }

    @Override
    public Object invokeWithArguments(Object... arguments) throws Throwable {
      Object[] targetArgs = arguments.clone();
      for (int i = 0; i < filters.length; i++) {
        MethodHandle filter = filters[i];
        if (filter != null) {
          int argIdx = pos + i;
          targetArgs[argIdx] = filter.invokeWithArguments(arguments[argIdx]);
        }
      }
      return target.invokeWithArguments(targetArgs);
    }
  }

  static class PermuteArgumentsMethodHandle extends MethodHandle {
    private final MethodHandle target;
    private final int[] reorder;

    PermuteArgumentsMethodHandle(MethodHandle target, MethodType newType, int[] reorder) {
      super(checkType(target, newType, reorder));
      this.target = target;
      this.reorder = reorder.clone();
    }

    private static MethodType checkType(MethodHandle target, MethodType newType, int[] reorder) {
      if (reorder.length != target.type().parameterCount()) {
        throw new IllegalArgumentException("reorder length mismatch");
      }
      if (!newType.returnType().equals(target.type().returnType())) {
        throw new IllegalArgumentException("return type mismatch");
      }
      int inCount = newType.parameterCount();
      for (int i = 0; i < reorder.length; i++) {
        int idx = reorder[i];
        if (idx < 0 || idx >= inCount) {
          throw new IllegalArgumentException("reorder index out of bounds: " + idx);
        }
      }
      return newType;
    }

    @Override
    public Object invokeWithArguments(Object... arguments) throws Throwable {
      Object[] targetArgs = new Object[reorder.length];
      for (int i = 0; i < reorder.length; i++) {
        targetArgs[i] = arguments[reorder[i]];
      }
      return target.invokeWithArguments(targetArgs);
    }
  }

  static class FoldArgumentsMethodHandle extends MethodHandle {
    private final MethodHandle target;
    private final int pos;
    private final MethodHandle combiner;
    private final boolean isVoidCombiner;

    FoldArgumentsMethodHandle(MethodHandle target, int pos, MethodHandle combiner) {
      super(computeType(target, pos, combiner));
      this.target = target;
      this.pos = pos;
      this.combiner = combiner;
      this.isVoidCombiner = combiner.type().returnType() == void.class;
    }

    private static MethodType computeType(MethodHandle target, int pos, MethodHandle combiner) {
      MethodType targetType = target.type();
      MethodType combinerType = combiner.type();
      boolean isVoid = combinerType.returnType() == void.class;
      int combCount = combinerType.parameterCount();
      if (isVoid) {
        if (pos < 0 || pos + combCount > targetType.parameterCount()) {
          throw new IllegalArgumentException("fold arguments out of range");
        }
        return targetType;
      } else {
        if (pos < 0 || pos + 1 + combCount > targetType.parameterCount()) {
          throw new IllegalArgumentException("fold arguments out of range");
        }
        return targetType.dropParameterTypes(pos, pos + 1);
      }
    }

    @Override
    public Object invokeWithArguments(Object... arguments) throws Throwable {
      int combCount = combiner.type().parameterCount();
      Object[] combArgs = new Object[combCount];
      System.arraycopy(arguments, pos, combArgs, 0, combCount);
      if (isVoidCombiner) {
        combiner.invokeWithArguments(combArgs);
        return target.invokeWithArguments(arguments);
      } else {
        Object combResult = combiner.invokeWithArguments(combArgs);
        Object[] targetArgs = new Object[arguments.length + 1];
        System.arraycopy(arguments, 0, targetArgs, 0, pos);
        targetArgs[pos] = combResult;
        System.arraycopy(arguments, pos, targetArgs, pos + 1, arguments.length - pos);
        return target.invokeWithArguments(targetArgs);
      }
    }
  }

  static class GuardWithTestMethodHandle extends MethodHandle {
    private final MethodHandle test;
    private final MethodHandle target;
    private final MethodHandle fallback;

    GuardWithTestMethodHandle(MethodHandle test, MethodHandle target, MethodHandle fallback) {
      super(checkType(test, target, fallback));
      this.test = test;
      this.target = target;
      this.fallback = fallback;
    }

    private static MethodType checkType(MethodHandle test, MethodHandle target, MethodHandle fallback) {
      if (!target.type().equals(fallback.type())) {
        throw new IllegalArgumentException("target and fallback must have same type");
      }
      if (test.type().returnType() != boolean.class) {
        throw new IllegalArgumentException("test must return boolean");
      }
      return target.type();
    }

    @Override
    public Object invokeWithArguments(Object... arguments) throws Throwable {
      int testCount = test.type().parameterCount();
      Object[] testArgs = new Object[testCount];
      System.arraycopy(arguments, 0, testArgs, 0, testCount);
      boolean cond = (Boolean) test.invokeWithArguments(testArgs);
      if (cond) {
        return target.invokeWithArguments(arguments);
      } else {
        return fallback.invokeWithArguments(arguments);
      }
    }
  }

  static class CatchExceptionMethodHandle extends MethodHandle {
    private final MethodHandle target;
    private final Class<? extends Throwable> exType;
    private final MethodHandle handler;

    CatchExceptionMethodHandle(MethodHandle target, Class<? extends Throwable> exType, MethodHandle handler) {
      super(target.type());
      this.target = target;
      this.exType = exType;
      this.handler = handler;
    }

    @Override
    public Object invokeWithArguments(Object... arguments) throws Throwable {
      try {
        return target.invokeWithArguments(arguments);
      } catch (Throwable t) {
        if (exType.isInstance(t)) {
          Object[] handlerArgs = new Object[arguments.length + 1];
          handlerArgs[0] = t;
          System.arraycopy(arguments, 0, handlerArgs, 1, arguments.length);
          return handler.invokeWithArguments(handlerArgs);
        }
        throw t;
      }
    }
  }

  static class ThrowExceptionMethodHandle extends MethodHandle {
    private final Class<? extends Throwable> exType;

    ThrowExceptionMethodHandle(Class<?> returnType, Class<? extends Throwable> exType) {
      super(MethodType.methodType(returnType, exType));
      this.exType = exType;
    }

    @Override
    public Object invokeWithArguments(Object... arguments) throws Throwable {
      Throwable t = (Throwable) arguments[0];
      throw t;
    }
  }

  static class DropReturnMethodHandle extends MethodHandle {
    private final MethodHandle target;

    DropReturnMethodHandle(MethodHandle target) {
      super(target.type().changeReturnType(void.class));
      this.target = target;
    }

    @Override
    public Object invokeWithArguments(Object... arguments) throws Throwable {
      target.invokeWithArguments(arguments);
      return null;
    }
  }

  static Object defaultValue(Class<?> type) {
    if (!type.isPrimitive()) return null;
    if (type == boolean.class) return Boolean.FALSE;
    if (type == byte.class) return (byte) 0;
    if (type == short.class) return (short) 0;
    if (type == char.class) return (char) 0;
    if (type == int.class) return 0;
    if (type == long.class) return 0L;
    if (type == float.class) return 0.0f;
    if (type == double.class) return 0.0d;
    return null;
  }

  static Object[] slice(Object[] arguments, int count) {
    if (count <= 0) return new Object[0];
    Object[] res = new Object[count];
    System.arraycopy(arguments, 0, res, 0, Math.min(count, arguments.length));
    return res;
  }

  static class TryFinallyMethodHandle extends MethodHandle {
    private final MethodHandle target;
    private final MethodHandle cleanup;

    TryFinallyMethodHandle(MethodHandle target, MethodHandle cleanup) {
      super(target.type());
      this.target = target;
      this.cleanup = cleanup;
    }

    @Override
    public Object invokeWithArguments(Object... arguments) throws Throwable {
      Class<?> rtype = target.type().returnType();
      boolean isVoid = (rtype == void.class);
      Object targetResult = null;
      Throwable targetEx = null;
      try {
        targetResult = target.invokeWithArguments(arguments);
      } catch (Throwable t) {
        targetEx = t;
      }

      int cleanupParamCount = cleanup.type().parameterCount();
      Object[] cleanupArgs = new Object[cleanupParamCount];
      int idx = 0;
      cleanupArgs[idx++] = targetEx;
      if (!isVoid) {
        cleanupArgs[idx++] = (targetEx == null ? targetResult : defaultValue(rtype));
      }
      for (int i = 0; idx < cleanupParamCount && i < arguments.length; i++) {
        cleanupArgs[idx++] = arguments[i];
      }

      Object cleanupResult = cleanup.invokeWithArguments(cleanupArgs);
      if (targetEx != null) {
        throw targetEx;
      }
      return isVoid ? null : cleanupResult;
    }
  }

  static class WhileLoopMethodHandle extends MethodHandle {
    private final MethodHandle init;
    private final MethodHandle pred;
    private final MethodHandle body;
    private final Class<?> varType;

    WhileLoopMethodHandle(MethodType type, MethodHandle init, MethodHandle pred, MethodHandle body, Class<?> varType) {
      super(type);
      this.init = init;
      this.pred = pred;
      this.body = body;
      this.varType = varType;
    }

    @Override
    public Object invokeWithArguments(Object... arguments) throws Throwable {
      Object state = (init != null) ? init.invokeWithArguments(slice(arguments, init.type().parameterCount()))
          : defaultValue(varType);
      while (true) {
        Object[] predArgs = packArgs(state, arguments, pred.type().parameterCount(), varType);
        boolean cond = (Boolean) pred.invokeWithArguments(predArgs);
        if (!cond) break;
        Object[] bodyArgs = packArgs(state, arguments, body.type().parameterCount(), varType);
        Object res = body.invokeWithArguments(bodyArgs);
        if (varType != void.class) {
          state = res;
        }
      }
      return (varType == void.class) ? null : state;
    }

    static Object[] packArgs(Object state, Object[] args, int targetCount, Class<?> vtype) {
      if (targetCount <= 0) return new Object[0];
      Object[] res = new Object[targetCount];
      int idx = 0;
      if (vtype != void.class) {
        res[idx++] = state;
      }
      for (int i = 0; idx < targetCount && i < args.length; i++) {
        res[idx++] = args[i];
      }
      return res;
    }
  }

  static class DoWhileLoopMethodHandle extends MethodHandle {
    private final MethodHandle init;
    private final MethodHandle body;
    private final MethodHandle pred;
    private final Class<?> varType;

    DoWhileLoopMethodHandle(MethodType type, MethodHandle init, MethodHandle body, MethodHandle pred,
        Class<?> varType) {
      super(type);
      this.init = init;
      this.body = body;
      this.pred = pred;
      this.varType = varType;
    }

    @Override
    public Object invokeWithArguments(Object... arguments) throws Throwable {
      Object state = (init != null) ? init.invokeWithArguments(slice(arguments, init.type().parameterCount()))
          : defaultValue(varType);
      do {
        Object[] bodyArgs = WhileLoopMethodHandle.packArgs(state, arguments, body.type().parameterCount(), varType);
        Object res = body.invokeWithArguments(bodyArgs);
        if (varType != void.class) {
          state = res;
        }
        Object[] predArgs = WhileLoopMethodHandle.packArgs(state, arguments, pred.type().parameterCount(), varType);
        boolean cond = (Boolean) pred.invokeWithArguments(predArgs);
        if (!cond) break;
      } while (true);
      return (varType == void.class) ? null : state;
    }
  }

  static class CountedLoopMethodHandle extends MethodHandle {
    private final MethodHandle start;
    private final MethodHandle end;
    private final MethodHandle init;
    private final MethodHandle body;
    private final Class<?> varType;

    CountedLoopMethodHandle(MethodType type, MethodHandle start, MethodHandle end, MethodHandle init,
        MethodHandle body, Class<?> varType) {
      super(type);
      this.start = start;
      this.end = end;
      this.init = init;
      this.body = body;
      this.varType = varType;
    }

    @Override
    public Object invokeWithArguments(Object... arguments) throws Throwable {
      int s = (start != null) ? ((Number) start.invokeWithArguments(slice(arguments,
          start.type().parameterCount()))).intValue() : 0;
      int e = ((Number) end.invokeWithArguments(slice(arguments, end.type().parameterCount()))).intValue();
      Object state = (init != null) ? init.invokeWithArguments(slice(arguments, init.type().parameterCount()))
          : defaultValue(varType);
      for (int i = s; i < e; i++) {
        Object[] bodyArgs = packCountedArgs(state, i, arguments, body.type().parameterCount(), varType);
        Object res = body.invokeWithArguments(bodyArgs);
        if (varType != void.class) {
          state = res;
        }
      }
      return (varType == void.class) ? null : state;
    }

    private static Object[] packCountedArgs(Object state, int counter, Object[] args, int targetCount, Class<?> vtype) {
      if (targetCount <= 0) return new Object[0];
      Object[] res = new Object[targetCount];
      int idx = 0;
      if (vtype != void.class) {
        res[idx++] = state;
      }
      if (idx < targetCount) {
        res[idx++] = Integer.valueOf(counter);
      }
      for (int i = 0; idx < targetCount && i < args.length; i++) {
        res[idx++] = args[i];
      }
      return res;
    }
  }

  static class IteratedLoopMethodHandle extends MethodHandle {
    private final MethodHandle iterator;
    private final MethodHandle init;
    private final MethodHandle body;
    private final Class<?> varType;

    IteratedLoopMethodHandle(MethodType type, MethodHandle iterator, MethodHandle init, MethodHandle body,
        Class<?> varType) {
      super(type);
      this.iterator = iterator;
      this.init = init;
      this.body = body;
      this.varType = varType;
    }

    @Override
    public Object invokeWithArguments(Object... arguments) throws Throwable {
      Iterator<?> iter;
      if (iterator != null) {
        Object itObj = iterator.invokeWithArguments(slice(arguments, iterator.type().parameterCount()));
        iter = toIterator(itObj);
      } else {
        iter = toIterator(arguments[0]);
      }
      Object state = (init != null) ? init.invokeWithArguments(slice(arguments, init.type().parameterCount()))
          : defaultValue(varType);
      while (iter.hasNext()) {
        Object elem = iter.next();
        Object[] bodyArgs = packIteratedArgs(state, elem, arguments, body.type().parameterCount(), varType);
        Object res = body.invokeWithArguments(bodyArgs);
        if (varType != void.class) {
          state = res;
        }
      }
      return (varType == void.class) ? null : state;
    }

    private static Iterator<?> toIterator(Object obj) {
      if (obj instanceof Iterator) {
        return (Iterator<?>) obj;
      }
      if (obj instanceof Iterable) {
        return ((Iterable<?>) obj).iterator();
      }
      throw new IllegalArgumentException("Cannot iterate over: " + obj);
    }

    private static Object[] packIteratedArgs(Object state, Object elem, Object[] args, int targetCount,
        Class<?> vtype) {
      if (targetCount <= 0) return new Object[0];
      Object[] res = new Object[targetCount];
      int idx = 0;
      if (vtype != void.class) {
        res[idx++] = state;
      }
      if (idx < targetCount) {
        res[idx++] = elem;
      }
      for (int i = 0; idx < targetCount && i < args.length; i++) {
        res[idx++] = args[i];
      }
      return res;
    }
  }

  static class LoopMethodHandle extends MethodHandle {
    private final MethodHandle[] inits;
    private final MethodHandle[] steps;
    private final MethodHandle[] preds;
    private final MethodHandle[] finis;
    private final Class<?>[] varTypes;
    private final int[] varIndices;
    private final Class<?> returnType;

    LoopMethodHandle(MethodType type,
                     MethodHandle[] inits,
                     MethodHandle[] steps,
                     MethodHandle[] preds,
                     MethodHandle[] finis,
                     Class<?>[] varTypes,
                     int[] varIndices,
                     Class<?> returnType) {
      super(type);
      this.inits = inits;
      this.steps = steps;
      this.preds = preds;
      this.finis = finis;
      this.varTypes = varTypes;
      this.varIndices = varIndices;
      this.returnType = returnType;
    }

    @Override
    public Object invokeWithArguments(Object... arguments) throws Throwable {
      int clauseCount = inits.length;
      Object[] vars = new Object[clauseCount];
      for (int j = 0; j < clauseCount; j++) {
        if (varTypes[j] != void.class) {
          MethodHandle init = inits[j];
          vars[j] = (init != null)
              ? init.invokeWithArguments(slice(arguments, init.type().parameterCount()))
              : defaultValue(varTypes[j]);
        }
      }

      while (true) {
        for (int j = 0; j < clauseCount; j++) {
          MethodHandle step = steps[j];
          if (step != null) {
            Object[] stepArgs = packLoopArgs(vars, varIndices, arguments, step.type().parameterCount());
            Object updated = step.invokeWithArguments(stepArgs);
            if (varTypes[j] != void.class) {
              vars[j] = updated;
            }
          }

          MethodHandle pred = preds[j];
          if (pred != null) {
            Object[] predArgs = packLoopArgs(vars, varIndices, arguments, pred.type().parameterCount());
            boolean cont = (Boolean) pred.invokeWithArguments(predArgs);
            if (!cont) {
              MethodHandle fini = finis[j];
              if (fini != null) {
                Object[] finiArgs = packLoopArgs(vars, varIndices, arguments, fini.type().parameterCount());
                return fini.invokeWithArguments(finiArgs);
              } else {
                return (varTypes[j] != void.class) ? vars[j] : defaultValue(returnType);
              }
            }
          }
        }
      }
    }

    private static Object[] packLoopArgs(Object[] vars, int[] varIndices, Object[] args, int targetCount) {
      if (targetCount <= 0) return new Object[0];
      Object[] res = new Object[targetCount];
      int idx = 0;
      for (int vi = 0; vi < varIndices.length && idx < targetCount; vi++) {
        res[idx++] = vars[varIndices[vi]];
      }
      for (int i = 0; idx < targetCount && i < args.length; i++) {
        res[idx++] = args[i];
      }
      return res;
    }
  }

  static class ArrayConstructorMethodHandle extends MethodHandle {
    private final Class<?> componentType;

    ArrayConstructorMethodHandle(Class<?> arrayClass) {
      super(MethodType.methodType(arrayClass, int.class));
      this.componentType = arrayClass.getComponentType();
    }

    @Override
    public Object invokeWithArguments(Object... arguments) throws Throwable {
      int len = ((Number) arguments[0]).intValue();
      return java.lang.reflect.Array.newInstance(componentType, len);
    }
  }

  static class ArrayLengthMethodHandle extends MethodHandle {
    ArrayLengthMethodHandle(Class<?> arrayClass) {
      super(MethodType.methodType(int.class, arrayClass));
    }

    @Override
    public Object invokeWithArguments(Object... arguments) throws Throwable {
      return java.lang.reflect.Array.getLength(arguments[0]);
    }
  }

  static class ArrayElementGetterMethodHandle extends MethodHandle {
    ArrayElementGetterMethodHandle(Class<?> arrayClass) {
      super(MethodType.methodType(arrayClass.getComponentType(), arrayClass, int.class));
    }

    @Override
    public Object invokeWithArguments(Object... arguments) throws Throwable {
      int idx = ((Number) arguments[1]).intValue();
      return java.lang.reflect.Array.get(arguments[0], idx);
    }
  }

  static class ArrayElementSetterMethodHandle extends MethodHandle {
    ArrayElementSetterMethodHandle(Class<?> arrayClass) {
      super(MethodType.methodType(void.class, arrayClass, int.class, arrayClass.getComponentType()));
    }

    @Override
    public Object invokeWithArguments(Object... arguments) throws Throwable {
      int idx = ((Number) arguments[1]).intValue();
      java.lang.reflect.Array.set(arguments[0], idx, arguments[2]);
      return null;
    }
  }

  static class DynamicInvokerMethodHandle extends MethodHandle {
    private final CallSite site;

    DynamicInvokerMethodHandle(CallSite site) {
      super(site.type());
      this.site = site;
    }

    @Override
    public Object invokeWithArguments(Object... arguments) throws Throwable {
      MethodHandle currentTarget = site.getTarget();
      if (currentTarget == null) {
        throw new IllegalStateException("call site target is null");
      }
      return currentTarget.invokeWithArguments(arguments);
    }
  }

  static class SwitchPointMethodHandle extends MethodHandle {
    private final SwitchPoint sp;
    private final MethodHandle target;
    private final MethodHandle fallback;

    SwitchPointMethodHandle(SwitchPoint sp, MethodHandle target, MethodHandle fallback) {
      super(target.type());
      this.sp = sp;
      this.target = target;
      this.fallback = fallback;
    }

    @Override
    public Object invokeWithArguments(Object... arguments) throws Throwable {
      if (!sp.hasBeenInvalidated()) {
        return target.invokeWithArguments(arguments);
      } else {
        return fallback.invokeWithArguments(arguments);
      }
    }
  }

  static class SpreaderMethodHandle extends MethodHandle {
    private final MethodHandle target;
    private final int spreadPos;
    private final int arrayLength;

    SpreaderMethodHandle(MethodHandle target, MethodType newType, int spreadPos, int arrayLength) {
      super(newType);
      this.target = target;
      this.spreadPos = spreadPos;
      this.arrayLength = arrayLength;
    }

    @Override
    public Object invokeWithArguments(Object... arguments) throws Throwable {
      Object array = arguments[spreadPos];
      int actualLen = array != null ? java.lang.reflect.Array.getLength(array) : 0;
      if (actualLen != arrayLength) {
        throw new IllegalArgumentException("array length " + actualLen + " != expected " + arrayLength);
      }
      Object[] targetArgs = new Object[arguments.length - 1 + arrayLength];
      System.arraycopy(arguments, 0, targetArgs, 0, spreadPos);
      for (int i = 0; i < arrayLength; i++) {
        targetArgs[spreadPos + i] = java.lang.reflect.Array.get(array, i);
      }
      System.arraycopy(arguments, spreadPos + 1, targetArgs, spreadPos + arrayLength, arguments.length - spreadPos - 1);
      return target.invokeWithArguments(targetArgs);
    }
  }

  static class CollectorMethodHandle extends MethodHandle {
    private final MethodHandle target;
    private final int collectPos;
    private final Class<?> componentType;
    private final int arrayLength;

    CollectorMethodHandle(MethodHandle target, MethodType newType, int collectPos, Class<?> arrayType, int arrayLength) {
      super(newType);
      this.target = target;
      this.collectPos = collectPos;
      this.componentType = arrayType.getComponentType();
      this.arrayLength = arrayLength;
    }

    @Override
    public Object invokeWithArguments(Object... arguments) throws Throwable {
      Object array = java.lang.reflect.Array.newInstance(componentType, arrayLength);
      for (int i = 0; i < arrayLength; i++) {
        java.lang.reflect.Array.set(array, i, arguments[collectPos + i]);
      }
      Object[] targetArgs = new Object[arguments.length - arrayLength + 1];
      System.arraycopy(arguments, 0, targetArgs, 0, collectPos);
      targetArgs[collectPos] = array;
      System.arraycopy(arguments, collectPos + arrayLength, targetArgs, collectPos + 1, arguments.length - collectPos - arrayLength);
      return target.invokeWithArguments(targetArgs);
    }
  }

  static class VarargsCollectorMethodHandle extends MethodHandle {
    private final MethodHandle target;
    private final Class<?> arrayType;
    private final Class<?> componentType;

    VarargsCollectorMethodHandle(MethodHandle target, Class<?> arrayType) {
      super(target.type());
      this.target = target;
      this.arrayType = arrayType;
      this.componentType = arrayType.getComponentType();
    }

    @Override
    public boolean isVarargsCollector() {
      return true;
    }

    @Override
    public MethodHandle asType(MethodType newType) {
      int paramCount = target.type().parameterCount();
      if (newType.parameterCount() == paramCount) {
        return super.asType(newType);
      }
      if (newType.parameterCount() < paramCount - 1) {
        throw new WrongMethodTypeException("cannot convert " + this + " to " + newType);
      }
      int collect = newType.parameterCount() - (paramCount - 1);
      return target.asCollector(arrayType, collect).asType(newType);
    }

    @Override
    public MethodHandle asFixedArity() {
      return target;
    }

    @Override
    public Object invokeWithArguments(Object... arguments) throws Throwable {
      int paramCount = target.type().parameterCount();
      int varargsPos = paramCount - 1;
      if (arguments.length == paramCount && arguments[varargsPos] != null &&
          arrayType.isInstance(arguments[varargsPos])) {
        return target.invokeWithArguments(arguments);
      }
      int count = Math.max(0, arguments.length - varargsPos);
      Object array = java.lang.reflect.Array.newInstance(componentType, count);
      for (int i = 0; i < count; i++) {
        java.lang.reflect.Array.set(array, i, arguments[varargsPos + i]);
      }
      Object[] targetArgs = new Object[paramCount];
      System.arraycopy(arguments, 0, targetArgs, 0, varargsPos);
      targetArgs[varargsPos] = array;
      return target.invokeWithArguments(targetArgs);
    }
  }

  static class CollectArgumentsMethodHandle extends MethodHandle {
    private final MethodHandle target;
    private final int pos;
    private final MethodHandle filter;
    private final int filterParamCount;

    CollectArgumentsMethodHandle(MethodHandle target, int pos, MethodHandle filter) {
      super(computeType(target, pos, filter));
      this.target = target;
      this.pos = pos;
      this.filter = filter;
      this.filterParamCount = filter.type().parameterCount();
    }

    private static MethodType computeType(MethodHandle target, int pos, MethodHandle filter) {
      MethodType targetType = target.type();
      MethodType filterType = filter.type();
      Class<?> filterRet = filterType.returnType();
      if (filterRet == void.class) {
        return targetType.insertParameterTypes(pos, filterType.parameterList());
      } else {
        if (pos < 0 || pos >= targetType.parameterCount() || targetType.parameterType(pos) != filterRet) {
          throw new IllegalArgumentException("target parameter type mismatch at " + pos);
        }
        MethodType dropped = targetType.dropParameterTypes(pos, pos + 1);
        return dropped.insertParameterTypes(pos, filterType.parameterList());
      }
    }

    @Override
    public Object invokeWithArguments(Object... arguments) throws Throwable {
      Object[] filterArgs = new Object[filterParamCount];
      System.arraycopy(arguments, pos, filterArgs, 0, filterParamCount);
      Object filterRes = filter.invokeWithArguments(filterArgs);

      Class<?> filterRet = filter.type().returnType();
      if (filterRet == void.class) {
        Object[] targetArgs = new Object[arguments.length - filterParamCount];
        System.arraycopy(arguments, 0, targetArgs, 0, pos);
        System.arraycopy(arguments, pos + filterParamCount, targetArgs, pos, arguments.length - pos - filterParamCount);
        return target.invokeWithArguments(targetArgs);
      } else {
        Object[] targetArgs = new Object[arguments.length - filterParamCount + 1];
        System.arraycopy(arguments, 0, targetArgs, 0, pos);
        targetArgs[pos] = filterRes;
        System.arraycopy(arguments, pos + filterParamCount, targetArgs, pos + 1, arguments.length - pos - filterParamCount);
        return target.invokeWithArguments(targetArgs);
      }
    }
  }

  static class ExactInvokerMethodHandle extends MethodHandle {
    ExactInvokerMethodHandle(MethodType targetType) {
      super(targetType.insertParameterTypes(0, MethodHandle.class));
    }

    @Override
    public Object invokeWithArguments(Object... arguments) throws Throwable {
      if (arguments == null || arguments.length < 1 || !(arguments[0] instanceof MethodHandle)) {
        throw new IllegalArgumentException("First argument must be a MethodHandle");
      }
      MethodHandle mh = (MethodHandle) arguments[0];
      Object[] targetArgs = new Object[arguments.length - 1];
      System.arraycopy(arguments, 1, targetArgs, 0, arguments.length - 1);
      return mh.invokeWithArguments(targetArgs);
    }
  }

  @Override
  public java.util.Optional<java.lang.constant.MethodHandleDesc> describeConstable() {
    if (method != null && method.class_ != null) {
      try {
        boolean isInterface = (method.class_.flags & 0x0200) != 0;
        java.lang.constant.DirectMethodHandleDesc.Kind k =
            java.lang.constant.DirectMethodHandleDesc.Kind.valueOf(this.kind, isInterface);
        Class<?> ownerClass = shim.SystemClassLoader.getClass(method.class_);
        java.lang.constant.ClassDesc owner = ownerClass.describeConstable().orElse(null);
        if (owner == null) return java.util.Optional.empty();
        String name = java.lang.reflect.Method.getName(method);
        String spec = java.lang.reflect.Method.getSpec(method);
        java.lang.constant.MethodTypeDesc mtd = java.lang.constant.MethodTypeDesc.ofDescriptor(spec);
        return java.util.Optional.of(java.lang.constant.MethodHandleDesc.ofMethod(k, owner, name, mtd));
      } catch (Exception ignored) {}
    }
    return java.util.Optional.empty();
  }
}
