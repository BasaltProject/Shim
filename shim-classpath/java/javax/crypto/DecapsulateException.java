/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.crypto;

import java.security.GeneralSecurityException;

public class DecapsulateException extends GeneralSecurityException {
  private static final long serialVersionUID = 21L;

  public DecapsulateException(String message) {
    super(message);
  }

  public DecapsulateException(String message, Throwable cause) {
    super(message, cause);
  }
}
