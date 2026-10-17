/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management;

public class DefaultLoaderRepository {
  public DefaultLoaderRepository() {
  }

  public static Class<?> loadClass(String className) throws ClassNotFoundException {
    return Class.forName(className, false, ClassLoader.getSystemClassLoader());
  }

  public static Class<?> loadClassWithout(ClassLoader loader, String className) throws ClassNotFoundException {
    return loadClass(className);
  }
}
