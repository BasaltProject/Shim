/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.security.cert;

import java.io.InputStream;
import java.math.BigInteger;
import java.security.Principal;
import java.util.Date;

public abstract class X509Certificate extends Certificate {
  public X509Certificate() {
  }

  public static final X509Certificate getInstance(InputStream inStream) throws CertificateException {
    return X509V1CertImpl.create(inStream);
  }

  public static final X509Certificate getInstance(byte[] certData) throws CertificateException {
    return X509V1CertImpl.create(certData);
  }

  public abstract void checkValidity() throws CertificateExpiredException, CertificateNotYetValidException;

  public abstract void checkValidity(Date date) throws CertificateExpiredException, CertificateNotYetValidException;

  public abstract int getVersion();

  public abstract BigInteger getSerialNumber();

  public abstract Principal getIssuerDN();

  public abstract Principal getSubjectDN();

  public abstract Date getNotBefore();

  public abstract Date getNotAfter();

  public abstract String getSigAlgName();

  public abstract String getSigAlgOID();

  public abstract byte[] getSigAlgParams();
}
