/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security;

public class NoSuchAlgorithmException extends GeneralSecurityException {
  private static final long serialVersionUID = -7443947487218346562L;

  public NoSuchAlgorithmException() {
    super();
  }

  public NoSuchAlgorithmException(String msg) {
    super(msg);
  }

  public NoSuchAlgorithmException(String message, Throwable cause) {
    super(message, cause);
  }

  public NoSuchAlgorithmException(Throwable cause) {
    super(cause);
  }
}
