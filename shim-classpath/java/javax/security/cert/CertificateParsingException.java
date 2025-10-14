/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.security.cert;

public class CertificateParsingException extends CertificateException {
  private static final long serialVersionUID = 7989222416793322029L;

  public CertificateParsingException() {
  }

  public CertificateParsingException(String msg) {
    super(msg);
  }
}
