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
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.PSSParameterSpec;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public final class RSAPublicKeyImpl implements RSAPublicKey {
  private static final long serialVersionUID = 2644735423591199609L;

  private final BigInteger n;
  private final BigInteger e;
  private final String type;
  private final AlgorithmParameterSpec keyParams;
  private transient byte[] encoded;

  public RSAPublicKeyImpl(String type, AlgorithmParameterSpec params, BigInteger n, BigInteger e)
      throws InvalidKeyException {
    RSAKeyFactory.checkRSAProviderKeyLengths(n.bitLength(), e);
    checkExponent(n, e);
    this.type = type;
    this.n = n;
    this.e = e;
    this.keyParams = RSAKeyFactory.checkParams(type, params);
  }

  static void checkExponent(BigInteger n, BigInteger e) throws InvalidKeyException {
    if (e.compareTo(BigInteger.valueOf(3)) < 0) {
      throw new InvalidKeyException("exponent is smaller than 3");
    }
    if (e.compareTo(n) >= 0) {
      throw new InvalidKeyException("exponent is larger than modulus");
    }
  }

  public RSAPublicKeyImpl(String type, byte[] encoded) throws InvalidKeyException {
    try {
      DerValue spki = new DerValue(encoded);
      DerInputStream in = spki.data();
      DerValue algId = in.getDerValue();
      DerInputStream ai = algId.data();
      String oid = ai.getOID().toString();
      AlgorithmParameterSpec params = null;
      if (oid.equals(RSAUtil.kOidPss)) {
        if (ai.available() > 0) {
          params = RSAUtil.decodePssParams(ai.getDerValue().toByteArray());
        }
      } else if (!oid.equals(RSAUtil.kOidRsa)) {
        throw new InvalidKeyException("Expected a RSA key, but got " + oid);
      }
      byte[] bits = in.getDerValue().getBitString();
      DerValue keySeq = new DerValue(bits);
      if (keySeq.getTag() != DerValue.tag_Sequence) {
        throw new InvalidKeyException("Invalid RSA public key");
      }
      DerInputStream k = keySeq.data();
      this.n = k.getBigInteger();
      this.e = k.getBigInteger();
      if (k.available() != 0) {
        throw new InvalidKeyException("Invalid RSA public key");
      }
      RSAKeyFactory.checkRSAProviderKeyLengths(n.bitLength(), e);
      checkExponent(n, e);
      this.type = oid.equals(RSAUtil.kOidPss) ? "RSASSA-PSS" : "RSA";
      this.keyParams = params;
      this.encoded = encoded.clone();
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
  public AlgorithmParameterSpec getParams() {
    return keyParams;
  }

  @Override
  public String getAlgorithm() {
    return type;
  }

  @Override
  public String getFormat() {
    return "X.509";
  }

  @Override
  public synchronized byte[] getEncoded() {
    if (encoded == null) {
      try {
        DerOutputStream keyBody = new DerOutputStream();
        keyBody.putInteger(n);
        keyBody.putInteger(e);
        DerOutputStream keySeq = new DerOutputStream();
        keySeq.write(DerValue.tag_Sequence, keyBody);
        DerValue alg;
        if (type.equals("RSASSA-PSS")) {
          DerValue params = keyParams == null ? null
              : new DerValue(RSAUtil.encodePssParams((PSSParameterSpec) keyParams));
          alg = RSAUtil.algId(RSAUtil.kOidPss, params);
        } else {
          alg = RSAUtil.algId(RSAUtil.kOidRsa, DerValue.nullValue());
        }
        DerOutputStream out = new DerOutputStream();
        DerOutputStream inner = new DerOutputStream();
        inner.putDerValue(alg);
        inner.putBitString(keySeq.toByteArray());
        out.write(DerValue.tag_Sequence, inner);
        encoded = out.toByteArray();
      } catch (IOException ex) {
        throw new ProviderException(ex);
      }
    }
    return encoded.clone();
  }

  @Override
  public int hashCode() {
    return n.hashCode() * 31 + e.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof PublicKey)) {
      return false;
    }
    if (obj instanceof RSAPublicKey) {
      RSAPublicKey other = (RSAPublicKey) obj;
      if (!n.equals(other.getModulus()) || !e.equals(other.getPublicExponent())) {
        return false;
      }
      return getAlgorithm().equals(other.getAlgorithm());
    }
    return false;
  }

  @Override
  public String toString() {
    return "Sun " + type + " public key, " + n.bitLength() + " bits\n  params: " + keyParams + "\n  modulus: " + n
        + "\n  public exponent: " + e;
  }
}
