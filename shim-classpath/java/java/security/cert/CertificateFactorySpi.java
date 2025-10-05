/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security.cert;

import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public abstract class CertificateFactorySpi {
  public CertificateFactorySpi() {
  }

  public abstract Certificate engineGenerateCertificate(InputStream inStream) throws CertificateException;

  public CertPath engineGenerateCertPath(InputStream inStream) throws CertificateException {
    throw new UnsupportedOperationException();
  }

  public CertPath engineGenerateCertPath(InputStream inStream, String encoding) throws CertificateException {
    throw new UnsupportedOperationException();
  }

  public CertPath engineGenerateCertPath(List<? extends Certificate> certificates) throws CertificateException {
    throw new UnsupportedOperationException();
  }

  public Iterator<String> engineGetCertPathEncodings() {
    throw new UnsupportedOperationException();
  }

  public abstract Collection<? extends Certificate> engineGenerateCertificates(InputStream inStream)
      throws CertificateException;

  public abstract CRL engineGenerateCRL(InputStream inStream) throws CRLException;

  public abstract Collection<? extends CRL> engineGenerateCRLs(InputStream inStream) throws CRLException;
}
