/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security.cert;

import java.security.GeneralSecurityException;

public class CertStoreException extends GeneralSecurityException {
  private static final long serialVersionUID = 2395296107471573245L;

  public CertStoreException() {
    super();
  }

  public CertStoreException(String msg) {
    super(msg);
  }

  public CertStoreException(Throwable cause) {
    super(cause);
  }

  public CertStoreException(String msg, Throwable cause) {
    super(msg, cause);
  }
}
