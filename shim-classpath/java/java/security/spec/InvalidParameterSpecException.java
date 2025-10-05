/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security.spec;

public class InvalidParameterSpecException extends java.security.GeneralSecurityException {
  private static final long serialVersionUID = -970468769593399342L;

  public InvalidParameterSpecException() {
    super();
  }

  public InvalidParameterSpecException(String msg) {
    super(msg);
  }
}
