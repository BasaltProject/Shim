/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.security.auth;

import java.security.BasicPermission;

public final class AuthPermission extends BasicPermission {
  private static final long serialVersionUID = 5806031445061587174L;

  public AuthPermission(String name) {
    super("createLoginContext".equals(name) ? "createLoginContext.*" : name);
  }

  public AuthPermission(String name, String actions) {
    super("createLoginContext".equals(name) ? "createLoginContext.*" : name, actions);
  }
}
