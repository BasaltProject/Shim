/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.rmi.server;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.rmi.Remote;
import java.rmi.server.RemoteObjectInvocationHandler;
import java.rmi.server.RemoteRef;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class Util {
  private Util() {
  }

  public static String descriptor(Class<?> c) {
    if (c.isPrimitive()) {
      if (c == int.class) {
        return "I";
      } else if (c == boolean.class) {
        return "Z";
      } else if (c == byte.class) {
        return "B";
      } else if (c == char.class) {
        return "C";
      } else if (c == short.class) {
        return "S";
      } else if (c == long.class) {
        return "J";
      } else if (c == float.class) {
        return "F";
      } else if (c == double.class) {
        return "D";
      } else {
        return "V";
      }
    } else if (c.isArray()) {
      return "[" + descriptor(c.getComponentType());
    }
    return "L" + c.getName().replace('.', '/') + ";";
  }

  public static String methodNameAndDescriptor(Method m) {
    StringBuilder b = new StringBuilder(m.getName()).append('(');
    for (Class<?> p : m.getParameterTypes()) {
      b.append(descriptor(p));
    }
    return b.append(')').append(descriptor(m.getReturnType())).toString();
  }

  public static long computeMethodHash(Method m) {
    long hash = 0;
    try {
      ByteArrayOutputStream bo = new ByteArrayOutputStream(512);
      MessageDigest md = MessageDigest.getInstance("SHA");
      DataOutputStream out = new DataOutputStream(bo);
      out.writeUTF(methodNameAndDescriptor(m));
      out.flush();
      byte[] digest = md.digest(bo.toByteArray());
      for (int i = 0; i < Math.min(8, digest.length); i++) {
        hash += ((long) (digest[i] & 0xFF)) << (i * 8);
      }
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
    return hash;
  }

  public static Class<?>[] remoteInterfaces(Class<?> implClass) {
    Set<Class<?>> out = new LinkedHashSet<Class<?>>();
    for (Class<?> c = implClass; c != null; c = c.getSuperclass()) {
      for (Class<?> i : c.getInterfaces()) {
        if (Remote.class.isAssignableFrom(i) && i != Remote.class) {
          out.add(i);
        }
      }
    }
    return out.toArray(new Class<?>[0]);
  }

  public static Map<Long, Method> methodsByHash(Class<?> implClass) {
    Map<Long, Method> map = new HashMap<Long, Method>();
    for (Class<?> i : remoteInterfaces(implClass)) {
      for (Method m : i.getMethods()) {
        map.put(computeMethodHash(m), m);
      }
    }
    return map;
  }

  public static Remote createStub(Class<?> implClass, RemoteRef ref) throws java.rmi.StubNotFoundException {
    Class<?>[] ifaces = remoteInterfaces(implClass);
    if (ifaces.length == 0) {
      throw new java.rmi.StubNotFoundException("remote object implements no remote interfaces");
    }
    ClassLoader loader = implClass.getClassLoader();
    List<Class<?>> list = new ArrayList<Class<?>>();
    for (Class<?> i : ifaces) {
      list.add(i);
    }
    return (Remote) Proxy.newProxyInstance(loader, list.toArray(new Class<?>[0]),
        new RemoteObjectInvocationHandler(ref));
  }

  public static String unqualifiedName(Class<?> c) {
    String name = c.getName();
    return name.substring(name.lastIndexOf('.') + 1);
  }
}
