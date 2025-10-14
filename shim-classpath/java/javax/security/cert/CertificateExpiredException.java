/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.security.cert;

public class CertificateExpiredException extends CertificateException {
  private static final long serialVersionUID = 5091601212177261883L;

  public CertificateExpiredException() {
  }

  public CertificateExpiredException(String msg) {
    super(msg);
  }
}
