/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security.cert;

public class CertificateExpiredException extends CertificateException {
  private static final long serialVersionUID = 9071001339691533771L;

  public CertificateExpiredException() {
    super();
  }

  public CertificateExpiredException(String msg) {
    super(msg);
  }
}
