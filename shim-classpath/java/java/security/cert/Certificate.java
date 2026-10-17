/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security.cert;

import java.io.ByteArrayInputStream;
import java.io.NotSerializableException;
import java.io.ObjectStreamException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.PublicKey;
import java.security.SignatureException;
import java.util.Arrays;

public abstract class Certificate implements java.io.Serializable {
  private static final long serialVersionUID = -3585440601605666277L;

  private final String type;
  private int hash = -1;

  protected Certificate(String type) {
    this.type = type;
  }

  public final String getType() {
    return this.type;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof Certificate)) {
      return false;
    }
    try {
      byte[] thisCert = this.getEncoded();
      byte[] otherCert = ((Certificate) other).getEncoded();
      return MessageDigest.isEqual(thisCert, otherCert);
    } catch (CertificateException e) {
      return false;
    }
  }

  @Override
  public int hashCode() {
    int h = hash;
    if (h == -1) {
      try {
        h = Arrays.hashCode(getEncoded());
      } catch (CertificateException e) {
        h = 0;
      }
      hash = h;
    }
    return h;
  }

  public abstract byte[] getEncoded() throws CertificateEncodingException;

  public abstract void verify(PublicKey key) throws CertificateException, NoSuchAlgorithmException,
      InvalidKeyException, NoSuchProviderException, SignatureException;

  public abstract void verify(PublicKey key, String sigProvider) throws CertificateException,
      NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException;

  public void verify(PublicKey key, Provider sigProvider) throws CertificateException, NoSuchAlgorithmException,
      InvalidKeyException, SignatureException {
    throw new UnsupportedOperationException();
  }

  @Override
  public abstract String toString();

  public abstract PublicKey getPublicKey();

  protected Object writeReplace() throws ObjectStreamException {
    try {
      return new CertificateRep(type, getEncoded());
    } catch (CertificateException e) {
      throw new NotSerializableException("java.security.cert.Certificate: " + type + ": " + e.getMessage());
    }
  }

  protected static class CertificateRep implements java.io.Serializable {
    private static final long serialVersionUID = -8563758940495660020L;

    private String type;
    private byte[] data;

    protected CertificateRep(String type, byte[] data) {
      this.type = type;
      this.data = data;
    }

    protected Object readResolve() throws ObjectStreamException {
      try {
        CertificateFactory cf = CertificateFactory.getInstance(type);
        return cf.generateCertificate(new ByteArrayInputStream(data));
      } catch (CertificateException e) {
        throw new NotSerializableException("java.security.cert.Certificate: " + type + ": " + e.getMessage());
      }
    }
  }
}
