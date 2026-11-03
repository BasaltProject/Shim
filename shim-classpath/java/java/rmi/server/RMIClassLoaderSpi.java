/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.rmi.server;

import java.net.MalformedURLException;

public abstract class RMIClassLoaderSpi {
  public RMIClassLoaderSpi() {
  }

  public abstract Class<?> loadClass(String codebase, String name, ClassLoader defaultLoader)
      throws MalformedURLException, ClassNotFoundException;

  public abstract Class<?> loadProxyClass(String codebase, String[] interfaces, ClassLoader defaultLoader)
      throws MalformedURLException, ClassNotFoundException;

  public abstract ClassLoader getClassLoader(String codebase) throws MalformedURLException;

  public abstract String getClassAnnotation(Class<?> cl);
}
