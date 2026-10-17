/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.rsa;

import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.ProviderException;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.AlgorithmParameterSpec;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public final class RSAPrivateKeyImpl implements RSAPrivateKey {
  private static final long serialVersionUID = -33106691987952810L;

  private final BigInteger n;
  private final BigInteger d;
  private final String type;
  private final AlgorithmParameterSpec keyParams;
  private transient byte[] encoded;

  public RSAPrivateKeyImpl(String type, AlgorithmParameterSpec params, BigInteger n, BigInteger d)
      throws InvalidKeyException {
    RSAKeyFactory.checkRSAProviderKeyLengths(n.bitLength(), null);
    this.type = type;
    this.n = n;
    this.d = d;
    this.keyParams = RSAKeyFactory.checkParams(type, params);
  }

  @Override
  public BigInteger getModulus() {
    return n;
  }

  @Override
  public BigInteger getPrivateExponent() {
    return d;
  }

  @Override
  public AlgorithmParameterSpec getParams() {
    return keyParams;
  }

  @Override
  public String getAlgorithm() {
    return type;
  }

  @Override
  public String getFormat() {
    return "PKCS#8";
  }

  @Override
  public synchronized byte[] getEncoded() {
    if (encoded == null) {
      try {
        DerOutputStream body = new DerOutputStream();
        body.putInteger(0);
        body.putInteger(n);
        body.putInteger(BigInteger.ZERO);
        body.putInteger(d);
        body.putInteger(BigInteger.ZERO);
        body.putInteger(BigInteger.ZERO);
        body.putInteger(BigInteger.ZERO);
        body.putInteger(BigInteger.ZERO);
        body.putInteger(BigInteger.ZERO);
        DerOutputStream keySeq = new DerOutputStream();
        keySeq.write(DerValue.tag_Sequence, body);
        encoded = RSAKeyFactory.pkcs8(type, keyParams, keySeq.toByteArray());
      } catch (IOException ex) {
        throw new ProviderException(ex);
      }
    }
    return encoded.clone();
  }

  @Override
  public int hashCode() {
    return n.hashCode() * 31 + d.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof RSAPrivateKey)) {
      return false;
    }
    RSAPrivateKey other = (RSAPrivateKey) obj;
    return n.equals(other.getModulus()) && d.equals(other.getPrivateExponent())
        && type.equals(other.getAlgorithm());
  }
}
