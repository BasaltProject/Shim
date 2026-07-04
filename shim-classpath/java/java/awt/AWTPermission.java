/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt;

public final class AWTPermission extends java.security.BasicPermission {
  public AWTPermission(java.lang.String p0) {
    super(p0);
  }
  public AWTPermission(java.lang.String p0, java.lang.String p1) {
    super(p0, p1);
  }
}
