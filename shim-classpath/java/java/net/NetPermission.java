/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.net;

import java.security.BasicPermission;

public final class NetPermission extends BasicPermission {
  private static final long serialVersionUID = -8343910153355041693L;

  public NetPermission(String name) {
    super(name);
  }

  public NetPermission(String name, String actions) {
    super(name, actions);
  }
}
