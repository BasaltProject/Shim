/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang;

public final class MatchException extends RuntimeException {
  private static final long serialVersionUID = 0L;

  public MatchException(String message, Throwable cause) {
    super(message, cause);
  }
}
