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
import java.util.List;

public class MethodHandle {
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

  MethodHandle(int kind, ClassLoader loader, shim.VMMethod method) {
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
        case REF_invokeSpecial:
        case REF_invokeInterface: {
          if (arguments.length < 1) {
            throw new IllegalArgumentException("Missing receiver instance");
          }
          Object instance = arguments[0];
          Object[] rest = new Object[arguments.length - 1];
          System.arraycopy(arguments, 1, rest, 0, rest.length);
          return new Method(method).invoke(instance, rest);
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
}
