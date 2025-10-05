/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security;

public class InvalidParameterException extends IllegalArgumentException {
  private static final long serialVersionUID = -857968536935667808L;

  public InvalidParameterException() {
    super();
  }

  public InvalidParameterException(String msg) {
    super(msg);
  }

  public InvalidParameterException(String message, Throwable cause) {
    super(message, cause);
  }

  public InvalidParameterException(Throwable cause) {
    super(cause);
  }
}
