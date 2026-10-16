/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.nio.charset;

public class CoderMalfunctionError extends Error {
  private static final long serialVersionUID = -1151412348057794301L;

  public CoderMalfunctionError(Exception cause) {
    super(cause);
  }
}
