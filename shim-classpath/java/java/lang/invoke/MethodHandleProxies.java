/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.invoke;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MethodHandleProxies {
  private MethodHandleProxies() {
  }

  private static final class Handler implements InvocationHandler {
    final Class<?> type;
    final MethodHandle target;
    final Method single;

    Handler(Class<?> type, MethodHandle target, Method single) {
      this.type = type;
      this.target = target;
      this.single = single;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      if (method.getDeclaringClass() == Object.class) {
        switch (method.getName()) {
        case "equals":
          return proxy == args[0];
        case "hashCode":
          return System.identityHashCode(proxy);
        default:
          return type.getName() + "@" + Integer.toHexString(System.identityHashCode(proxy));
        }
      }
      if (method.isDefault()) {
        return InvocationHandler.invokeDefault(proxy, method, args);
      }
      if (sameShape(method, single)) {
        Object r = target.invokeWithArguments(args == null ? new Object[0] : args);
        if (method.getReturnType() == void.class) {
          return null;
        }
        return r;
      }
      if (method.getName().equals("equals") && method.getParameterCount() == 1
          && method.getParameterTypes()[0] == Object.class) {
        return proxy == args[0];
      }
      if (method.getName().equals("hashCode") && method.getParameterCount() == 0) {
        return System.identityHashCode(proxy);
      }
      if (method.getName().equals("toString") && method.getParameterCount() == 0) {
        return type.getName() + "@" + Integer.toHexString(System.identityHashCode(proxy));
      }
      throw new InternalError("unexpected method: " + method);
    }
  }

  private static boolean sameShape(Method a, Method b) {
    return a.getName().equals(b.getName()) && java.util.Arrays.equals(a.getParameterTypes(), b.getParameterTypes());
  }

  private static boolean isObjectMethod(Method m) {
    String n = m.getName();
    Class<?>[] p = m.getParameterTypes();
    return (n.equals("equals") && p.length == 1 && p[0] == Object.class)
        || (n.equals("hashCode") && p.length == 0) || (n.equals("toString") && p.length == 0);
  }

  public static <T> T asInterfaceInstance(final Class<T> intfc, final MethodHandle target) {
    if (!intfc.isInterface() || !Modifier.isPublic(intfc.getModifiers())) {
      throw new IllegalArgumentException("not a public interface: " + intfc.getName());
    }
    Objects.requireNonNull(target);
    List<Method> abstracts = new ArrayList<Method>();
    for (Method m : intfc.getMethods()) {
      if (!Modifier.isAbstract(m.getModifiers()) || isObjectMethod(m)) {
        continue;
      }
      boolean dup = false;
      for (Method o : abstracts) {
        if (sameShape(o, m)) {
          dup = true;
        }
      }
      if (!dup) {
        abstracts.add(m);
      }
    }
    if (abstracts.size() != 1) {
      throw new IllegalArgumentException("not a single-method interface: " + intfc.getName());
    }
    Method single = abstracts.get(0);
    MethodType want = MethodType.methodType(single.getReturnType(), single.getParameterTypes());
    MethodType have = target.type();
    if (have.parameterCount() != want.parameterCount() && !target.isVarargsCollector()) {
      throw new WrongMethodTypeException("cannot convert " + target + " to " + want);
    }
    Object proxy = Proxy.newProxyInstance(intfc.getClassLoader(), new Class<?>[] {intfc},
        new Handler(intfc, target, single));
    return intfc.cast(proxy);
  }

  public static boolean isWrapperInstance(Object x) {
    return x != null && Proxy.isProxyClass(x.getClass()) && Proxy.getInvocationHandler(x) instanceof Handler;
  }

  private static Handler handlerOf(Object x) {
    if (isWrapperInstance(x)) {
      return (Handler) Proxy.getInvocationHandler(x);
    }
    throw new IllegalArgumentException("not a wrapper instance: " + x);
  }

  public static MethodHandle wrapperInstanceTarget(Object x) {
    return handlerOf(x).target;
  }

  public static Class<?> wrapperInstanceType(Object x) {
    return handlerOf(x).type;
  }
}
