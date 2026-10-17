/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security.cert;

import javax.security.auth.x500.X500Principal;

final class X509CRLImplHelper {
  private X509CRLImplHelper() {
  }

  static byte[] encoded(X509CRL crl) throws CRLException {
    return crl.getEncoded();
  }

  static X500Principal issuer(X509CRL crl) {
    return new X500Principal(crl.getIssuerDN().getName());
  }
}
