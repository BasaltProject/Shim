/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.invoke;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

final class LambdaProxies {
  private LambdaProxies() { }

  static boolean isDirect(MethodHandle handle) {
    return handle.method != null
        && handle.kind >= MethodHandle.REF_invokeVirtual
        && handle.kind <= MethodHandle.REF_invokeInterface;
  }

  static CallSite callSite(MethodType invokedType, String name, MethodHandle implementation, Class<?>[] markers) {
    try {
      Method make = LambdaProxies.class.getDeclaredMethod
        ("make", Class.class, String.class, MethodHandle.class, Class[].class, Object[].class);
      MethodHandle factory = new MethodHandle
        (MethodHandle.REF_invokeStatic, invokedType.loader, shim.Classes.toVMMethod(make));
      factory = MethodHandles.insertArguments(factory, 0, invokedType.returnType(), name, implementation, markers);
      return new ConstantCallSite
        (factory.asCollector(Object[].class, invokedType.parameterCount()).asType(invokedType));
    } catch (NoSuchMethodException e) {
      throw new AssertionError(e);
    }
  }

  static Object make(Class<?> type, String name, MethodHandle implementation, Class<?>[] markers,
                     Object[] captured) {
    Class<?>[] interfaces = new Class<?>[markers.length + 1];
    interfaces[0] = type;
    System.arraycopy(markers, 0, interfaces, 1, markers.length);
    return Proxy.newProxyInstance(type.getClassLoader(), interfaces, new InvocationHandler() {
      public Object invoke(Object proxy, Method method, Object[] arguments) throws Throwable {
        if (method.getDeclaringClass() == Object.class) {
          switch (method.getName()) {
          case "equals": return proxy == arguments[0];
          case "hashCode": return System.identityHashCode(proxy);
          default: return type.getName() + "$$Lambda@" + Integer.toHexString(System.identityHashCode(proxy));
          }
        }
        if (method.isDefault() && !method.getName().equals(name)) {
          return InvocationHandler.invokeDefault(proxy, method, arguments);
        }
        int count = arguments == null ? 0 : arguments.length;
        Object[] all = new Object[captured.length + count];
        System.arraycopy(captured, 0, all, 0, captured.length);
        if (count > 0) System.arraycopy(arguments, 0, all, captured.length, count);
        return implementation.invokeWithArguments(all);
      }
    });
  }
}
