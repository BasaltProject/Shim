/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security;

public class SignatureException extends GeneralSecurityException {
  private static final long serialVersionUID = 7509989324975124438L;

  public SignatureException() {
    super();
  }

  public SignatureException(String msg) {
    super(msg);
  }

  public SignatureException(String message, Throwable cause) {
    super(message, cause);
  }

  public SignatureException(Throwable cause) {
    super(cause);
  }
}
