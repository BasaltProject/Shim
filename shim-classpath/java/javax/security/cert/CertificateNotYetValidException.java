/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.security.cert;

public class CertificateNotYetValidException extends CertificateException {
  private static final long serialVersionUID = -8976172474266822818L;

  public CertificateNotYetValidException() {
  }

  public CertificateNotYetValidException(String msg) {
    super(msg);
  }
}
