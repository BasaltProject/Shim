/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.net.ssl;

public abstract class SNIMatcher {
  private final int type;

  protected SNIMatcher(int type) {
    if (type < 0) {
      throw new IllegalArgumentException("Server name type cannot be less than zero");
    } else if (type > 255) {
      throw new IllegalArgumentException("Server name type cannot be greater than 255");
    }
    this.type = type;
  }

  public final int getType() {
    return type;
  }

  public abstract boolean matches(SNIServerName serverName);
}
