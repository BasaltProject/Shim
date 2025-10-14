/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.security.cert;

public class CertificateException extends Exception {
  private static final long serialVersionUID = -5757213374030785290L;

  public CertificateException() {
  }

  public CertificateException(String msg) {
    super(msg);
  }
}
