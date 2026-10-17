/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.security.cert;

public class CertificateEncodingException extends CertificateException {
  private static final long serialVersionUID = -8187642723048403470L;

  public CertificateEncodingException() {
  }

  public CertificateEncodingException(String msg) {
    super(msg);
  }
}
