/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.jdi;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

final class Proxies {
  private Proxies() {
  }

  @SuppressWarnings("unchecked")
  static <T> T make(Object impl, Class<?>... ifaces) {
    final Map<String, Method> cache = new HashMap<String, Method>();
    final Class<?> ic = impl.getClass();
    return (T) Proxy.newProxyInstance(Proxies.class.getClassLoader(), ifaces, new H(impl) {
      public Object invoke(Object proxy, Method m, Object[] args) throws Throwable {
        String n = m.getName();
        int argc = args == null ? 0 : args.length;
        if (n.equals("equals") && argc == 1) {
          return proxy == args[0] || impl.equals(implOf(args[0]));
        }
        if (n.equals("hashCode") && argc == 0) {
          return impl.hashCode();
        }
        String key = n + "/" + argc + "/" + (argc > 0 ? m.getParameterTypes()[0].getName() : "");
        Method target;
        synchronized (cache) {
          target = cache.get(key);
          if (target == null && !cache.containsKey(key)) {
            target = find(ic, m);
            if (target != null) {
              target.setAccessible(true);
            }
            cache.put(key, target);
          }
        }
        if (target == null) {
          if (n.equals("toString")) {
            return ifaces[0].getSimpleName() + "@" + System.identityHashCode(impl);
          }
          if (n.equals("compareTo") && argc == 1) {
            return 0;
          }
          throw new UnsupportedOperationException(ifaces[0].getSimpleName() + "." + n);
        }
        try {
          return target.invoke(impl, args);
        } catch (InvocationTargetException e) {
          Throwable c = e.getCause();
          if (c instanceof Vm.IllegalThreadStateExceptionWrapper) {
            throw new com.sun.jdi.IncompatibleThreadStateException();
          }
          if (c instanceof Vm.AbsentInfoWrapper) {
            throw new com.sun.jdi.AbsentInformationException();
          }
          throw c;
        }
      }
    });
  }

  abstract static class H implements InvocationHandler {
    final Object impl;

    H(Object impl) {
      this.impl = impl;
    }
  }

  static Object implOf(Object proxy) {
    if (proxy != null && Proxy.isProxyClass(proxy.getClass())) {
      InvocationHandler h = Proxy.getInvocationHandler(proxy);
      if (h instanceof H) {
        return ((H) h).impl;
      }
    }
    return proxy;
  }

  private static Method find(Class<?> c, Method want) {
    Method best = null;
    for (Method m : c.getMethods()) {
      if (m.getName().equals(want.getName()) && m.getParameterCount() == want.getParameterCount()) {
        if (best == null) {
          best = m;
        }
        boolean exact = true;
        for (int i = 0; i < m.getParameterCount(); i++) {
          if (m.getParameterTypes()[i] != want.getParameterTypes()[i]) {
            exact = false;
          }
        }
        if (exact) {
          return m;
        }
        boolean assignable = true;
        for (int i = 0; i < m.getParameterCount(); i++) {
          if (!m.getParameterTypes()[i].isAssignableFrom(want.getParameterTypes()[i])
              && !want.getParameterTypes()[i].isAssignableFrom(m.getParameterTypes()[i])) {
            assignable = false;
          }
        }
        if (assignable) {
          best = m;
        }
      }
    }
    return best;
  }
}
