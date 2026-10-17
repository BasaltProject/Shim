/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.util.logging;

public final class LoggingPermission extends java.security.BasicPermission {
  private static final long serialVersionUID = 63564341580231582L;

  public LoggingPermission(String name, String actions) throws IllegalArgumentException {
    super(name);
    if (!name.equals("control")) {
      throw new IllegalArgumentException("name: " + name);
    }
    if (actions != null && !actions.isEmpty()) {
      throw new IllegalArgumentException("actions: " + actions);
    }
  }
}
