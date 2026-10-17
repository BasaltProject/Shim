/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security.cert;

public class CertificateException extends java.security.GeneralSecurityException {
  private static final long serialVersionUID = 3192535253797119798L;

  public CertificateException() {
    super();
  }

  public CertificateException(String msg) {
    super(msg);
  }

  public CertificateException(String message, Throwable cause) {
    super(message, cause);
  }

  public CertificateException(Throwable cause) {
    super(cause);
  }
}
