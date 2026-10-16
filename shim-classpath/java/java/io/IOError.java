/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.io;

public class IOError extends Error {
  private static final long serialVersionUID = 67100927991680413L;

  public IOError(Throwable cause) {
    super(cause);
  }
}
