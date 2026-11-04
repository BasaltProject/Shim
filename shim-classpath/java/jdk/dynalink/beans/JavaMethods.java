/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.dynalink.beans;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import jdk.dynalink.NoSuchDynamicMethodException;
import jdk.dynalink.linker.support.TypeUtilities;

abstract class JavaMethods {
  final String name;
  final List<? extends Executable> candidates;

  JavaMethods(String name, List<? extends Executable> candidates) {
    this.name = name;
    this.candidates = candidates;
  }

  final Object invokeWith(Object thisArg, Object[] args) throws Throwable {
    final Executable e = select(args);
    final Object[] converted = convert(e, args);
    try {
      if (e instanceof Method) {
        final Method m = (Method) e;
        return m.invoke(Modifier.isStatic(m.getModifiers()) ? null : thisArg, converted);
      }
      return ((Constructor<?>) e).newInstance(converted);
    } catch (InvocationTargetException ex) {
      throw ex.getCause();
    }
  }

  private Executable select(Object[] args) {
    List<Executable> applicable = new ArrayList<>();
    for (Executable e : candidates) {
      if (isApplicable(e, args, false)) {
        applicable.add(e);
      }
    }
    if (applicable.isEmpty()) {
      for (Executable e : candidates) {
        if (e.isVarArgs() && isApplicable(e, args, true)) {
          applicable.add(e);
        }
      }
    }
    if (applicable.isEmpty()) {
      throw new NoSuchDynamicMethodException("None of the " + (candidates.size() == 1 ? "" : "overloads of ")
          + name + " is applicable to the argument types " + argTypes(args));
    }
    Executable best = applicable.get(0);
    for (int i = 1; i < applicable.size(); i++) {
      final Executable c = applicable.get(i);
      if (moreSpecific(c, best)) {
        best = c;
      } else if (!moreSpecific(best, c)) {
        throw new IllegalArgumentException("Can't unambiguously select between fixed arity signatures "
            + applicable + " of the method " + name + " for argument types " + argTypes(args));
      }
    }
    return best;
  }

  private static String argTypes(Object[] args) {
    final List<String> l = new ArrayList<>();
    for (Object a : args) {
      l.add(a == null ? "null" : a.getClass().getName());
    }
    return l.toString();
  }

  private static boolean isApplicable(Executable e, Object[] args, boolean varargs) {
    final Class<?>[] p = e.getParameterTypes();
    if (!varargs) {
      if (p.length != args.length) {
        return false;
      }
      for (int i = 0; i < p.length; i++) {
        if (!accepts(p[i], args[i])) {
          return false;
        }
      }
      return true;
    }
    if (args.length < p.length - 1) {
      return false;
    }
    for (int i = 0; i < p.length - 1; i++) {
      if (!accepts(p[i], args[i])) {
        return false;
      }
    }
    final Class<?> component = p[p.length - 1].getComponentType();
    for (int i = p.length - 1; i < args.length; i++) {
      if (!accepts(component, args[i])) {
        return false;
      }
    }
    return true;
  }

  private static boolean accepts(Class<?> param, Object arg) {
    if (arg == null) {
      return !param.isPrimitive();
    }
    return TypeUtilities.isMethodInvocationConvertible(arg.getClass(), param)
        || (param.isPrimitive() && arg instanceof Number && TypeUtilities.isMethodInvocationConvertible(
            primitiveOfNumber(arg), param));
  }

  private static Class<?> primitiveOfNumber(Object arg) {
    final Class<?> p = TypeUtilities.getPrimitiveType(arg.getClass());
    return p == null ? arg.getClass() : p;
  }

  private static boolean moreSpecific(Executable a, Executable b) {
    final Class<?>[] pa = a.getParameterTypes();
    final Class<?>[] pb = b.getParameterTypes();
    if (pa.length != pb.length) {
      return pa.length > pb.length ? false : true;
    }
    for (int i = 0; i < pa.length; i++) {
      if (!TypeUtilities.isMethodInvocationConvertible(pa[i], pb[i])) {
        return false;
      }
    }
    return true;
  }

  private static Object[] convert(Executable e, Object[] args) {
    final Class<?>[] p = e.getParameterTypes();
    if (e.isVarArgs()) {
      final boolean direct = args.length == p.length && (args[args.length - 1] == null
          || p[p.length - 1].isInstance(args[args.length - 1]));
      if (!direct) {
        final Object[] r = new Object[p.length];
        System.arraycopy(args, 0, r, 0, p.length - 1);
        final Class<?> component = p[p.length - 1].getComponentType();
        final Object arr = java.lang.reflect.Array.newInstance(component, args.length - (p.length - 1));
        for (int i = p.length - 1; i < args.length; i++) {
          java.lang.reflect.Array.set(arr, i - (p.length - 1), coerce(component, args[i]));
        }
        r[p.length - 1] = arr;
        return r;
      }
    }
    final Object[] r = new Object[args.length];
    for (int i = 0; i < args.length; i++) {
      r[i] = coerce(p[i], args[i]);
    }
    return r;
  }

  private static Object coerce(Class<?> type, Object v) {
    if (v instanceof Number && type.isPrimitive()) {
      final Number n = (Number) v;
      if (type == int.class) {
        return n.intValue();
      } else if (type == long.class) {
        return n.longValue();
      } else if (type == double.class) {
        return n.doubleValue();
      } else if (type == float.class) {
        return n.floatValue();
      } else if (type == short.class) {
        return n.shortValue();
      } else if (type == byte.class) {
        return n.byteValue();
      }
    }
    return v;
  }

  static final class MethodGroup extends JavaMethods {
    MethodGroup(String name, List<Method> methods) {
      super(name, methods);
    }

    @Override
    public String toString() {
      return "[dynamic method " + name + "]";
    }
  }

  static final class ConstructorGroup extends JavaMethods {
    ConstructorGroup(String name, List<Constructor<?>> ctors) {
      super(name, ctors);
    }

    @Override
    public String toString() {
      return "[dynamic constructor " + name + "]";
    }
  }
}
