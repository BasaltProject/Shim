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
    if (method != null) {
      StringBuilder sb = new StringBuilder();
      if (method.class_ != null) {
        sb.append(Classes.makeString(method.class_.name, 0,
                                     method.class_.name.length - 1));
        sb.append(".");
      }
      sb.append(Classes.makeString(method.name, 0,
                                   method.name.length - 1));
      sb.append(Classes.makeString(method.spec, 0,
                                   method.spec.length - 1));
      return sb.toString();
    }
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
    private final MethodHandle target;

    AsTypeMethodHandle(MethodHandle target, MethodType newType) {
      super(newType);
      this.target = target;
    }

    @Override
    public Object invokeWithArguments(Object... arguments) throws Throwable {
      return target.invokeWithArguments(arguments);
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
