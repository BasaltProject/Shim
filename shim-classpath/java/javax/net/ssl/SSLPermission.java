/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.net.ssl;

import java.security.BasicPermission;

public final class SSLPermission extends BasicPermission {
  private static final long serialVersionUID = -3456898025505876775L;

  public SSLPermission(String name) {
    super(name);
  }

  public SSLPermission(String name, String actions) {
    super(name, actions);
  }
}
