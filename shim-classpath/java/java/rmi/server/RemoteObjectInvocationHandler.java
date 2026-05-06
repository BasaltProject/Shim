/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.rmi.server;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.rmi.Remote;
import java.rmi.UnexpectedException;
import sun.rmi.server.Util;

public class RemoteObjectInvocationHandler extends RemoteObject implements InvocationHandler {
  private static final long serialVersionUID = 2L;

  public RemoteObjectInvocationHandler(RemoteRef ref) {
    super(ref);
    if (ref == null) {
      throw new NullPointerException();
    }
  }

  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    if (method.getDeclaringClass() == Object.class) {
      return invokeObjectMethod(proxy, method, args);
    } else if ("finalize".equals(method.getName()) && method.getParameterTypes().length == 0) {
      return null;
    } else {
      return invokeRemoteMethod(proxy, method, args);
    }
  }

  private Object invokeObjectMethod(Object proxy, Method method, Object[] args) {
    String name = method.getName();
    if (name.equals("hashCode")) {
      return hashCode();
    } else if (name.equals("equals")) {
      Object obj = args[0];
      return proxy == obj || (obj != null && Proxy.isProxyClass(obj.getClass())
          && equals(Proxy.getInvocationHandler(obj)));
    } else if (name.equals("toString")) {
      return proxyToString(proxy);
    } else {
      throw new IllegalArgumentException("unexpected Object method: " + method);
    }
  }

  private Object invokeRemoteMethod(Object proxy, Method method, Object[] args) throws Exception {
    try {
      if (!(proxy instanceof Remote)) {
        throw new IllegalArgumentException("proxy not Remote instance");
      }
      return ref.invoke((Remote) proxy, method, args, Util.computeMethodHash(method));
    } catch (Exception e) {
      if (!(e instanceof RuntimeException)) {
        Class<?>[] declaredExceptions = method.getExceptionTypes();
        Class<?> cl = proxy.getClass();
        try {
          Class<?>[] viaProxy = cl.getMethod(method.getName(), method.getParameterTypes()).getExceptionTypes();
          if (viaProxy.length > declaredExceptions.length) {
            declaredExceptions = viaProxy;
          }
        } catch (NoSuchMethodException nsme) {
          throw (IllegalArgumentException) new IllegalArgumentException().initCause(nsme);
        }
        Class<?> thrownType = e.getClass();
        for (Class<?> declaredType : declaredExceptions) {
          if (declaredType.isAssignableFrom(thrownType)) {
            throw e;
          }
        }
        e = new UnexpectedException("unexpected exception", e);
      }
      throw e;
    }
  }

  private String proxyToString(Object proxy) {
    Class<?>[] interfaces = proxy.getClass().getInterfaces();
    if (interfaces.length == 0) {
      return "Proxy[" + this + "]";
    }
    String iface = interfaces[0].getName();
    if (iface.equals("java.rmi.Remote") && interfaces.length > 1) {
      iface = interfaces[1].getName();
    }
    String ifaceName = iface.substring(iface.lastIndexOf('.') + 1);
    return "Proxy[" + ifaceName + "," + this + "]";
  }

  private void readObjectNoData() throws InvalidObjectException {
    throw new InvalidObjectException("no data in stream; class: " + this.getClass().getName());
  }
}
