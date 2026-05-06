/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.rmi.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.lang.reflect.Proxy;
import java.rmi.server.RMIClassLoader;

public class MarshalInputStream extends ObjectInputStream {
  public MarshalInputStream(InputStream in) throws IOException {
    super(in);
  }

  private String readAnnotation() throws IOException, ClassNotFoundException {
    Object o = readObject();
    return o instanceof String ? (String) o : null;
  }

  @Override
  protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
    String codebase = readAnnotation();
    ClassLoader loader = Thread.currentThread().getContextClassLoader();
    return RMIClassLoader.loadClass(codebase, desc.getName(), loader);
  }

  @Override
  protected Class<?> resolveProxyClass(String[] interfaces) throws IOException, ClassNotFoundException {
    String codebase = readAnnotation();
    ClassLoader loader = Thread.currentThread().getContextClassLoader();
    return RMIClassLoader.loadProxyClass(codebase, interfaces, loader);
  }
}
