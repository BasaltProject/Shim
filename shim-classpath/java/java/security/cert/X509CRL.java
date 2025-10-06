/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security.cert;

import java.math.BigInteger;
import java.security.DEREncodable;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Principal;
import java.security.Provider;
import java.security.PublicKey;
import java.security.SignatureException;
import java.util.Date;
import java.util.Set;
import javax.security.auth.x500.X500Principal;

public abstract class X509CRL extends CRL implements X509Extension, DEREncodable {
  private transient X500Principal issuerPrincipal;

  protected X509CRL() {
    super("X.509");
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof X509CRL)) {
      return false;
    }
    try {
      byte[] thisCRL = this.getEncoded();
      byte[] otherCRL = ((X509CRL) other).getEncoded();
      return java.util.Arrays.equals(thisCRL, otherCRL);
    } catch (CRLException e) {
      return false;
    }
  }

  @Override
  public int hashCode() {
    int retval = 0;
    try {
      byte[] crlData = X509CRLImplHelper.encoded(this);
      for (int i = 1; i < crlData.length; i++) {
        retval += crlData[i] * i;
      }
    } catch (CRLException e) {
      // ignore
    }
    return retval;
  }

  public abstract byte[] getEncoded() throws CRLException;

  public abstract void verify(PublicKey key) throws CRLException, NoSuchAlgorithmException, InvalidKeyException,
      NoSuchProviderException, SignatureException;

  public abstract void verify(PublicKey key, String sigProvider) throws CRLException, NoSuchAlgorithmException,
      InvalidKeyException, NoSuchProviderException, SignatureException;

  public void verify(PublicKey key, Provider sigProvider) throws CRLException, NoSuchAlgorithmException,
      InvalidKeyException, SignatureException {
    throw new UnsupportedOperationException();
  }

  public abstract int getVersion();

  public abstract Principal getIssuerDN();

  public X500Principal getIssuerX500Principal() {
    if (issuerPrincipal == null) {
      issuerPrincipal = X509CRLImplHelper.issuer(this);
    }
    return issuerPrincipal;
  }

  public abstract Date getThisUpdate();

  public abstract Date getNextUpdate();

  public abstract X509CRLEntry getRevokedCertificate(BigInteger serialNumber);

  public X509CRLEntry getRevokedCertificate(X509Certificate certificate) {
    X500Principal certIssuer = certificate.getIssuerX500Principal();
    X500Principal crlIssuer = getIssuerX500Principal();
    if (!certIssuer.equals(crlIssuer)) {
      return null;
    }
    return getRevokedCertificate(certificate.getSerialNumber());
  }

  public abstract Set<? extends X509CRLEntry> getRevokedCertificates();

  public abstract byte[] getTBSCertList() throws CRLException;

  public abstract byte[] getSignature();

  public abstract String getSigAlgName();

  public abstract String getSigAlgOID();

  public abstract byte[] getSigAlgParams();
}
