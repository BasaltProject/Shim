/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management.loading;

public interface ClassLoaderRepository {
  Class<?> loadClass(String p0) throws ClassNotFoundException;

  Class<?> loadClassWithout(ClassLoader p0, String p1) throws ClassNotFoundException;

  Class<?> loadClassBefore(ClassLoader p0, String p1) throws ClassNotFoundException;
}
