/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package shim;

public final class PlatformClassLoader extends ClassLoader {
  public PlatformClassLoader() {
    super("platform", null);
  }

  protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
    return Class.forName(name, false, null);
  }
}
