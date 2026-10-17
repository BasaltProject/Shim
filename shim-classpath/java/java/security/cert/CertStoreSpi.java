/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security.cert;

import java.security.InvalidAlgorithmParameterException;
import java.util.Collection;

public abstract class CertStoreSpi {
  public CertStoreSpi(CertStoreParameters params) throws InvalidAlgorithmParameterException {
  }

  public abstract Collection<? extends Certificate> engineGetCertificates(CertSelector selector)
      throws CertStoreException;

  public abstract Collection<? extends CRL> engineGetCRLs(CRLSelector selector) throws CertStoreException;
}
