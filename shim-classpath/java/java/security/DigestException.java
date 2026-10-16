/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security;

public class DigestException extends GeneralSecurityException {
  private static final long serialVersionUID = 5821450303093652515L;

  public DigestException() {
    super();
  }

  public DigestException(String msg) {
    super(msg);
  }

  public DigestException(String message, Throwable cause) {
    super(message, cause);
  }

  public DigestException(Throwable cause) {
    super(cause);
  }
}
