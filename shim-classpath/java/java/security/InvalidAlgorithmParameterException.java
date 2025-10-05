/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security;

public class InvalidAlgorithmParameterException extends GeneralSecurityException {
  private static final long serialVersionUID = 2864672297499471472L;

  public InvalidAlgorithmParameterException() {
    super();
  }

  public InvalidAlgorithmParameterException(String msg) {
    super(msg);
  }

  public InvalidAlgorithmParameterException(String message, Throwable cause) {
    super(message, cause);
  }

  public InvalidAlgorithmParameterException(Throwable cause) {
    super(cause);
  }
}
