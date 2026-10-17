/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.crypto;

public class BadPaddingException extends java.security.GeneralSecurityException {
  private static final long serialVersionUID = -5315033893984728443L;

  public BadPaddingException() {
    super();
  }

  public BadPaddingException(String msg) {
    super(msg);
  }
}
