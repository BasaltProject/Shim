/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.util;

public class InvalidPropertiesFormatException extends java.io.IOException {
  private static final long serialVersionUID = 7763056076009360219L;

  public InvalidPropertiesFormatException(Throwable cause) {
    super(cause == null ? null : cause.toString());
    initCause(cause);
  }

  public InvalidPropertiesFormatException(String message) {
    super(message);
  }
}
