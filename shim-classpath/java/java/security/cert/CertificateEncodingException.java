/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security.cert;

public class CertificateEncodingException extends CertificateException {
  private static final long serialVersionUID = 6219492851589449162L;

  public CertificateEncodingException() {
    super();
  }

  public CertificateEncodingException(String msg) {
    super(msg);
  }

  public CertificateEncodingException(String message, Throwable cause) {
    super(message, cause);
  }

  public CertificateEncodingException(Throwable cause) {
    super(cause);
  }
}
