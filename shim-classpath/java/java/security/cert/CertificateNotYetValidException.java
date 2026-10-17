/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security.cert;

public class CertificateNotYetValidException extends CertificateException {
  private static final long serialVersionUID = 4355919900041064702L;

  public CertificateNotYetValidException() {
    super();
  }

  public CertificateNotYetValidException(String msg) {
    super(msg);
  }
}
