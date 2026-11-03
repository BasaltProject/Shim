/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.provider;

import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.SignatureSpi;
import java.security.interfaces.DSAParams;
import java.security.interfaces.DSAPrivateKey;
import java.security.interfaces.DSAPublicKey;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

abstract class DSA extends SignatureSpi {
  private final String digestName;
  private MessageDigest digest;
  private java.io.ByteArrayOutputStream raw;
  private DSAPrivateKey privateKey;
  private DSAPublicKey publicKey;
  private SecureRandom random;

  DSA(String digestName) {
    this.digestName = digestName;
  }

  private void reset() {
    try {
      if (digestName == null) {
        raw = new java.io.ByteArrayOutputStream();
      } else {
        digest = MessageDigest.getInstance(digestName);
      }
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void engineInitVerify(PublicKey key) throws InvalidKeyException {
    if (!(key instanceof DSAPublicKey)) {
      throw new InvalidKeyException("not a DSA public key: " + key);
    }
    DSAPublicKey k = (DSAPublicKey) key;
    if (k.getParams() == null) {
      throw new InvalidKeyException("DSA public key lacks parameters");
    }
    publicKey = k;
    privateKey = null;
    reset();
  }

  @Override
  protected void engineInitSign(PrivateKey key) throws InvalidKeyException {
    engineInitSign(key, null);
  }

  @Override
  protected void engineInitSign(PrivateKey key, SecureRandom random) throws InvalidKeyException {
    if (!(key instanceof DSAPrivateKey)) {
      throw new InvalidKeyException("not a DSA private key: " + key);
    }
    DSAPrivateKey k = (DSAPrivateKey) key;
    if (k.getParams() == null) {
      throw new InvalidKeyException("DSA private key lacks parameters");
    }
    privateKey = k;
    publicKey = null;
    this.random = random;
    reset();
  }

  @Override
  protected void engineUpdate(byte b) {
    if (digest != null) {
      digest.update(b);
    } else {
      raw.write(b);
    }
  }

  @Override
  protected void engineUpdate(byte[] b, int off, int len) {
    if (digest != null) {
      digest.update(b, off, len);
    } else {
      raw.write(b, off, len);
    }
  }

  private BigInteger hashToInt(BigInteger q) {
    byte[] h = digest != null ? digest.digest() : raw.toByteArray();
    reset();
    int qLen = (q.bitLength() + 7) / 8;
    if (h.length > qLen) {
      h = java.util.Arrays.copyOf(h, qLen);
    }
    return new BigInteger(1, h);
  }

  @Override
  protected byte[] engineSign() throws SignatureException {
    if (privateKey == null) {
      throw new SignatureException("object not initialized for signing");
    }
    DSAParams params = privateKey.getParams();
    BigInteger p = params.getP();
    BigInteger q = params.getQ();
    BigInteger g = params.getG();
    BigInteger x = privateKey.getX();
    BigInteger z = hashToInt(q);
    SecureRandom rnd = random != null ? random : new SecureRandom();
    BigInteger r;
    BigInteger s;
    do {
      BigInteger k;
      do {
        k = new BigInteger(q.bitLength() + 64, rnd).mod(q.subtract(BigInteger.ONE)).add(BigInteger.ONE);
      } while (k.signum() <= 0);
      r = g.modPow(k, p).mod(q);
      s = k.modInverse(q).multiply(z.add(x.multiply(r))).mod(q);
    } while (r.signum() == 0 || s.signum() == 0);
    try {
      DerOutputStream body = new DerOutputStream();
      body.putInteger(r);
      body.putInteger(s);
      DerOutputStream out = new DerOutputStream();
      out.write(DerValue.tag_Sequence, body);
      return out.toByteArray();
    } catch (RuntimeException e) {
      throw new SignatureException(e.getMessage());
    }
  }

  @Override
  protected boolean engineVerify(byte[] sig) throws SignatureException {
    if (publicKey == null) {
      throw new SignatureException("object not initialized for verification");
    }
    DSAParams params = publicKey.getParams();
    BigInteger p = params.getP();
    BigInteger q = params.getQ();
    BigInteger g = params.getG();
    BigInteger r;
    BigInteger s;
    try {
      DerInputStream in = new DerValue(sig).data();
      r = in.getDerValue().getBigInteger();
      s = in.getDerValue().getBigInteger();
    } catch (IOException | RuntimeException e) {
      throw new SignatureException("Invalid encoding for signature", e);
    }
    BigInteger z = hashToInt(q);
    if (r.signum() <= 0 || r.compareTo(q) >= 0 || s.signum() <= 0 || s.compareTo(q) >= 0) {
      return false;
    }
    BigInteger w = s.modInverse(q);
    BigInteger u1 = z.multiply(w).mod(q);
    BigInteger u2 = r.multiply(w).mod(q);
    BigInteger v = g.modPow(u1, p).multiply(publicKey.getY().modPow(u2, p)).mod(p).mod(q);
    return v.equals(r);
  }

  @Override
  @Deprecated
  protected void engineSetParameter(String key, Object param) {
    throw new InvalidParameterException("No parameter accepted");
  }

  @Override
  @Deprecated
  protected Object engineGetParameter(String key) {
    return null;
  }

  public static final class SHA1withDSA extends DSA {
    public SHA1withDSA() {
      super("SHA-1");
    }
  }

  public static final class SHA224withDSA extends DSA {
    public SHA224withDSA() {
      super("SHA-224");
    }
  }

  public static final class SHA256withDSA extends DSA {
    public SHA256withDSA() {
      super("SHA-256");
    }
  }

  public static final class SHA384withDSA extends DSA {
    public SHA384withDSA() {
      super("SHA-384");
    }
  }

  public static final class SHA512withDSA extends DSA {
    public SHA512withDSA() {
      super("SHA-512");
    }
  }

  public static final class RawDSA extends DSA {
    public RawDSA() {
      super(null);
    }
  }
}
