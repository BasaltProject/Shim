/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security.spec;

public class InvalidKeySpecException extends java.security.GeneralSecurityException {
  private static final long serialVersionUID = 3546139293998810778L;

  public InvalidKeySpecException() {
    super();
  }

  public InvalidKeySpecException(String msg) {
    super(msg);
  }

  public InvalidKeySpecException(String message, Throwable cause) {
    super(message, cause);
  }

  public InvalidKeySpecException(Throwable cause) {
    super(cause);
  }
}
