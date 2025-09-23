/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.nio.file;

import java.security.BasicPermission;

public final class LinkPermission extends BasicPermission {
  private static final long serialVersionUID = 1L;

  public LinkPermission(String name) {
    super(name);
    checkName(name);
  }

  public LinkPermission(String name, String actions) {
    super(name);
    checkName(name);
    if (actions != null && !actions.isEmpty()) {
      throw new IllegalArgumentException("actions: " + actions);
    }
  }

  private static void checkName(String name) {
    if (!name.equals("hard") && !name.equals("symbolic")) {
      throw new IllegalArgumentException("name: " + name);
    }
  }
}
