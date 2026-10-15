/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security;

public class GeneralSecurityException extends Exception {
  public GeneralSecurityException() {
    super();
  }

  public GeneralSecurityException(String msg) {
    super(msg);
  }

  public GeneralSecurityException(String message, Throwable cause) {
    super(message, cause);
  }

  public GeneralSecurityException(Throwable cause) {
    super(cause);
  }
}
