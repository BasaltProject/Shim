/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.util;

public class IllegalFormatFlagsException extends IllegalFormatException {
  private static final long serialVersionUID = 790824L;

  private final String flags;

  public IllegalFormatFlagsException(String flags) {
    this.flags = flags;
  }

  public String getFlags() {
    return flags;
  }

  public String getMessage() {
    return "Flags = '" + flags + "'";
  }
}
