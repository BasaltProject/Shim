// Minimum release: 21
/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class ProxyDefaultModern {

  public interface Greeter {
    String normalMethod(String text);

    default String defaultGreeting(String name) {
      return "Hello, " + name + "!";
    }

    default int defaultCompute(int x, int y) {
      return x * 10 + y;
    }
  }

  public static void main(String[] args) throws Throwable {
    testProxyClassPredicates();
    testInvokeDefault();
    testGetInvocationHandler();
    System.out.println("ALL TESTS PASSED");
  }

  private static void testProxyClassPredicates() {
    Greeter proxy = (Greeter) Proxy.newProxyInstance(
        Greeter.class.getClassLoader(),
        new Class<?>[] { Greeter.class },
        new InvocationHandler() {
          @Override
          public Object invoke(Object p, Method m, Object[] a) {
            return null;
          }
        });

    if (!Proxy.isProxyClass(proxy.getClass())) {
      throw new AssertionError("expected true for proxy class");
    }
    if (Proxy.isProxyClass(Object.class) || Proxy.isProxyClass(Greeter.class)) {
      throw new AssertionError("expected false for non-proxy class");
    }
    try {
      Proxy.isProxyClass(null);
      throw new AssertionError("expected NullPointerException for null class");
    } catch (NullPointerException expected) {
    }
  }

  private static void testInvokeDefault() throws Throwable {
    Greeter proxy = (Greeter) Proxy.newProxyInstance(
        Greeter.class.getClassLoader(),
        new Class<?>[] { Greeter.class },
        new InvocationHandler() {
          @Override
          public Object invoke(Object p, Method m, Object[] a) throws Throwable {
            if ("normalMethod".equals(m.getName())) {
              return "proxied:" + a[0];
            }
            if (m.isDefault()) {
              return InvocationHandler.invokeDefault(p, m, a);
            }
            return null;
          }
        });

    String normal = proxy.normalMethod("abc");
    if (!"proxied:abc".equals(normal)) {
      throw new AssertionError("normal method mismatch: " + normal);
    }

    String greeted = proxy.defaultGreeting("Java25");
    if (!"Hello, Java25!".equals(greeted)) {
      throw new AssertionError("default greeting mismatch: " + greeted);
    }

    int computed = proxy.defaultCompute(4, 2);
    if (computed != 42) {
      throw new AssertionError("default compute mismatch: " + computed);
    }

    // Validation: invokeDefault rejects non-default method
    Method normalM = Greeter.class.getMethod("normalMethod", String.class);
    try {
      InvocationHandler.invokeDefault(proxy, normalM, "test");
      throw new AssertionError("expected IllegalArgumentException for non-default method");
    } catch (IllegalArgumentException expected) {
    }

    // Validation: invokeDefault rejects non-proxy instance
    Method defM = Greeter.class.getMethod("defaultGreeting", String.class);
    try {
      InvocationHandler.invokeDefault("not-a-proxy", defM, "test");
      throw new AssertionError("expected IllegalArgumentException for non-proxy instance");
    } catch (IllegalArgumentException expected) {
    }
  }

  private static void testGetInvocationHandler() {
    InvocationHandler handler = new InvocationHandler() {
      @Override
      public Object invoke(Object p, Method m, Object[] a) {
        return null;
      }
    };
    Greeter proxy = (Greeter) Proxy.newProxyInstance(
        Greeter.class.getClassLoader(),
        new Class<?>[] { Greeter.class },
        handler);

    if (Proxy.getInvocationHandler(proxy) != handler) {
      throw new AssertionError("expected same handler");
    }
    try {
      Proxy.getInvocationHandler(null);
      throw new AssertionError("expected NullPointerException for null proxy");
    } catch (NullPointerException expected) {
    }
    try {
      Proxy.getInvocationHandler("not a proxy");
      throw new AssertionError("expected IllegalArgumentException for non-proxy");
    } catch (IllegalArgumentException expected) {
    }
  }
}
