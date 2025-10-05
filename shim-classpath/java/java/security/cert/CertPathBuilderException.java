/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security.cert;

public class CertPathBuilderException extends java.security.GeneralSecurityException {
  private static final long serialVersionUID = 5316471420178794402L;

  public CertPathBuilderException() {
    super();
  }

  public CertPathBuilderException(String msg) {
    super(msg);
  }

  public CertPathBuilderException(String message, Throwable cause) {
    super(message, cause);
  }

  public CertPathBuilderException(Throwable cause) {
    super(cause);
  }
}
