/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security.cert;

import java.security.GeneralSecurityException;

public class CRLException extends GeneralSecurityException {
  private static final long serialVersionUID = -6694728944094197147L;

  public CRLException() {
    super();
  }

  public CRLException(String message) {
    super(message);
  }

  public CRLException(String message, Throwable cause) {
    super(message, cause);
  }

  public CRLException(Throwable cause) {
    super(cause);
  }
}
