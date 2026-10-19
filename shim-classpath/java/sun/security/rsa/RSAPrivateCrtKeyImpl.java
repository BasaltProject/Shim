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
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.AlgorithmParameterSpec;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public final class RSAPrivateCrtKeyImpl implements RSAPrivateCrtKey {
  private static final long serialVersionUID = -1326088454257084918L;

  private final BigInteger n;
  private final BigInteger e;
  private final BigInteger d;
  private final BigInteger p;
  private final BigInteger q;
  private final BigInteger pe;
  private final BigInteger qe;
  private final BigInteger coeff;
  private final String type;
  private final AlgorithmParameterSpec keyParams;
  private transient byte[] encoded;

  public RSAPrivateCrtKeyImpl(String type, AlgorithmParameterSpec params, BigInteger n, BigInteger e, BigInteger d,
      BigInteger p, BigInteger q, BigInteger pe, BigInteger qe, BigInteger coeff) throws InvalidKeyException {
    RSAKeyFactory.checkRSAProviderKeyLengths(n.bitLength(), e);
    this.type = type;
    this.n = n;
    this.e = e;
    this.d = d;
    this.p = p;
    this.q = q;
    this.pe = pe;
    this.qe = qe;
    this.coeff = coeff;
    this.keyParams = RSAKeyFactory.checkParams(type, params);
  }

  public static RSAPrivateKey newKey(String type, AlgorithmParameterSpec params, byte[] encoded)
      throws InvalidKeyException {
    try {
      DerValue pkcs8 = new DerValue(encoded);
      DerInputStream in = pkcs8.data();
      int version = in.getInteger();
      if (version != 0 && version != 1) {
        throw new InvalidKeyException("Unsupported PKCS#8 version: " + version);
      }
      DerValue algId = in.getDerValue();
      DerInputStream ai = algId.data();
      String oid = ai.getOID().toString();
      AlgorithmParameterSpec keyParams = null;
      boolean pss = oid.equals(RSAUtil.kOidPss);
      if (pss) {
        if (ai.available() > 0) {
          keyParams = RSAUtil.decodePssParams(ai.getDerValue().toByteArray());
        }
      } else if (!oid.equals(RSAUtil.kOidRsa)) {
        throw new InvalidKeyException("Expected a RSA key, but got " + oid);
      }
      byte[] inner = in.getOctetString();
      DerValue keySeq = new DerValue(inner);
      if (keySeq.getTag() != DerValue.tag_Sequence) {
        throw new InvalidKeyException("Invalid RSA private key");
      }
      DerInputStream k = keySeq.data();
      k.getInteger();
      BigInteger n = k.getBigInteger();
      BigInteger e = k.getBigInteger();
      BigInteger d = k.getBigInteger();
      BigInteger p = k.getBigInteger();
      BigInteger q = k.getBigInteger();
      BigInteger pe = k.getBigInteger();
      BigInteger qe = k.getBigInteger();
      BigInteger coeff = k.getBigInteger();
      String t = pss ? "RSASSA-PSS" : "RSA";
      if (e.signum() == 0 && p.signum() == 0) {
        return new RSAPrivateKeyImpl(t, keyParams, n, d);
      }
      RSAPrivateCrtKeyImpl key = new RSAPrivateCrtKeyImpl(t, keyParams, n, e, d, p, q, pe, qe, coeff);
      key.encoded = encoded.clone();
      return key;
    } catch (IOException ex) {
      throw new InvalidKeyException("Unable to decode key", ex);
    }
  }

  @Override
  public BigInteger getModulus() {
    return n;
  }

  @Override
  public BigInteger getPublicExponent() {
    return e;
  }

  @Override
  public BigInteger getPrivateExponent() {
    return d;
  }

  @Override
  public BigInteger getPrimeP() {
    return p;
  }

  @Override
  public BigInteger getPrimeQ() {
    return q;
  }

  @Override
  public BigInteger getPrimeExponentP() {
    return pe;
  }

  @Override
  public BigInteger getPrimeExponentQ() {
    return qe;
  }

  @Override
  public BigInteger getCrtCoefficient() {
    return coeff;
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
        body.putInteger(e);
        body.putInteger(d);
        body.putInteger(p);
        body.putInteger(q);
        body.putInteger(pe);
        body.putInteger(qe);
        body.putInteger(coeff);
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
