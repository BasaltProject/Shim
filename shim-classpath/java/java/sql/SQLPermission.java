/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.sql;

import java.security.BasicPermission;

public final class SQLPermission extends BasicPermission {
  static final long serialVersionUID = -1439323187199563495L;

  public SQLPermission(String name) {
    super(name);
  }

  public SQLPermission(String name, String actions) {
    super(name, actions);
  }
}
