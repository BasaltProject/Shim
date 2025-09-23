/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.reflect;

import java.security.BasicPermission;

public final class ReflectPermission extends BasicPermission {
  private static final long serialVersionUID = 7412737110241507485L;

  public ReflectPermission(String name) {
    super(name);
  }

  public ReflectPermission(String name, String actions) {
    super(name, actions);
  }
}
