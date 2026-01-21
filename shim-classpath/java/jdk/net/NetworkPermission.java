/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package jdk.net;

import java.security.BasicPermission;

public final class NetworkPermission extends BasicPermission {
  private static final long serialVersionUID = -2012939586906722291L;

  public NetworkPermission(String name) {
    super(name);
  }

  public NetworkPermission(String name, String actions) {
    super(name, actions);
  }
}
