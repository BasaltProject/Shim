/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.security.cert;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Principal;
import java.security.PublicKey;
import java.security.SignatureException;
import java.util.Date;

final class X509V1CertImpl extends X509Certificate {
  private final java.security.cert.X509Certificate wrapped;

  private X509V1CertImpl(java.security.cert.X509Certificate wrapped) {
    this.wrapped = wrapped;
  }

  static X509Certificate create(InputStream in) throws CertificateException {
    try {
      java.security.cert.CertificateFactory cf = java.security.cert.CertificateFactory.getInstance("X.509");
      return new X509V1CertImpl((java.security.cert.X509Certificate) cf.generateCertificate(in));
    } catch (java.security.cert.CertificateException e) {
      throw new CertificateException(e.getMessage());
    }
  }

  static X509Certificate create(byte[] data) throws CertificateException {
    return create(new ByteArrayInputStream(data));
  }

  @Override
  public byte[] getEncoded() throws CertificateEncodingException {
    try {
      return wrapped.getEncoded();
    } catch (java.security.cert.CertificateEncodingException e) {
      throw new CertificateEncodingException(e.getMessage());
    }
  }

  @Override
  public void verify(PublicKey key) throws CertificateException, NoSuchAlgorithmException, InvalidKeyException,
      NoSuchProviderException, SignatureException {
    try {
      wrapped.verify(key);
    } catch (java.security.cert.CertificateException e) {
      throw new CertificateException(e.getMessage());
    }
  }

  @Override
  public void verify(PublicKey key, String sigProvider) throws CertificateException, NoSuchAlgorithmException,
      InvalidKeyException, NoSuchProviderException, SignatureException {
    try {
      wrapped.verify(key, sigProvider);
    } catch (java.security.cert.CertificateException e) {
      throw new CertificateException(e.getMessage());
    }
  }

  @Override
  public String toString() {
    return wrapped.toString();
  }

  @Override
  public PublicKey getPublicKey() {
    return wrapped.getPublicKey();
  }

  @Override
  public void checkValidity() throws CertificateExpiredException, CertificateNotYetValidException {
    checkValidity(new Date());
  }

  @Override
  public void checkValidity(Date date) throws CertificateExpiredException, CertificateNotYetValidException {
    try {
      wrapped.checkValidity(date);
    } catch (java.security.cert.CertificateNotYetValidException e) {
      throw new CertificateNotYetValidException(e.getMessage());
    } catch (java.security.cert.CertificateExpiredException e) {
      throw new CertificateExpiredException(e.getMessage());
    }
  }

  @Override
  public int getVersion() {
    return wrapped.getVersion() - 1;
  }

  @Override
  public BigInteger getSerialNumber() {
    return wrapped.getSerialNumber();
  }

  @Override
  public Principal getIssuerDN() {
    return wrapped.getIssuerDN();
  }

  @Override
  public Principal getSubjectDN() {
    return wrapped.getSubjectDN();
  }

  @Override
  public Date getNotBefore() {
    return wrapped.getNotBefore();
  }

  @Override
  public Date getNotAfter() {
    return wrapped.getNotAfter();
  }

  @Override
  public String getSigAlgName() {
    return wrapped.getSigAlgName();
  }

  @Override
  public String getSigAlgOID() {
    return wrapped.getSigAlgOID();
  }

  @Override
  public byte[] getSigAlgParams() {
    return wrapped.getSigAlgParams();
  }
}
