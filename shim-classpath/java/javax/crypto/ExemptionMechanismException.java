/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.crypto;

public class ExemptionMechanismException extends java.security.GeneralSecurityException {
  private static final long serialVersionUID = 1572699429277957109L;

  public ExemptionMechanismException() {
    super();
  }

  public ExemptionMechanismException(String msg) {
    super(msg);
  }
}
