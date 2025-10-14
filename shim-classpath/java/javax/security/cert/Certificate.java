/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.security.cert;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.util.Arrays;

public abstract class Certificate {
  public Certificate() {
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
      return Arrays.equals(thisCert, otherCert);
    } catch (CertificateException e) {
      return false;
    }
  }

  @Override
  public int hashCode() {
    int retval = 0;
    try {
      byte[] certData = this.getEncoded();
      for (int i = 1; i < certData.length; i++) {
        retval += certData[i] * i;
      }
      return retval;
    } catch (CertificateException e) {
      return retval;
    }
  }

  public abstract byte[] getEncoded() throws CertificateEncodingException;

  public abstract void verify(PublicKey key) throws CertificateException, NoSuchAlgorithmException,
      InvalidKeyException, NoSuchProviderException, SignatureException;

  public abstract void verify(PublicKey key, String sigProvider) throws CertificateException,
      NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException;

  @Override
  public abstract String toString();

  public abstract PublicKey getPublicKey();
}
