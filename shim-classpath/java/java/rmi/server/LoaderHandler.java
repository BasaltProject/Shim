/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.rmi.server;

public interface LoaderHandler {
  java.lang.String packagePrefix = "sun.rmi.server";

  java.lang.Object getSecurityContext(java.lang.ClassLoader classLoader);
  java.lang.Class<?> loadClass(java.lang.String string) throws java.net.MalformedURLException, java.lang.ClassNotFoundException;
  java.lang.Class<?> loadClass(java.net.URL uRL, java.lang.String string) throws java.net.MalformedURLException, java.lang.ClassNotFoundException;
}
