/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security.cert;

public class CertStoreException extends java.security.GeneralSecurityException {
  private static final long serialVersionUID = 2395296107471573245L;

  public CertStoreException() {
    super();
  }

  public CertStoreException(String msg) {
    super(msg);
  }

  public CertStoreException(String message, Throwable cause) {
    super(message, cause);
  }

  public CertStoreException(Throwable cause) {
    super(cause);
  }
}
