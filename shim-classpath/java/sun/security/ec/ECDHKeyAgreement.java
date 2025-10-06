/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.ec;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import javax.crypto.KeyAgreementSpi;
import javax.crypto.SecretKey;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.SecretKeySpec;

public final class ECDHKeyAgreement extends KeyAgreementSpi {
  private ECPrivateKey privateKey;
  private ECPublicKey publicKey;

  public ECDHKeyAgreement() {
  }

  @Override
  protected void engineInit(Key key, SecureRandom random) throws InvalidKeyException {
    if (!(key instanceof PrivateKey)) {
      throw new InvalidKeyException("Key must be instance of PrivateKey");
    }
    privateKey = (ECPrivateKey) ECKeyFactory.toECKey(key);
    publicKey = null;
  }

  @Override
  protected void engineInit(Key key, AlgorithmParameterSpec params, SecureRandom random) throws InvalidKeyException,
      InvalidAlgorithmParameterException {
    if (params != null) {
      throw new InvalidAlgorithmParameterException("Parameters not supported");
    }
    engineInit(key, random);
  }

  @Override
  protected Key engineDoPhase(Key key, boolean lastPhase) throws InvalidKeyException, IllegalStateException {
    if (privateKey == null) {
      throw new IllegalStateException("Not initialized");
    }
    if (publicKey != null) {
      throw new IllegalStateException("Phase already executed");
    }
    if (!lastPhase) {
      throw new IllegalStateException("Only two party agreement supported, lastPhase must be true");
    }
    if (!(key instanceof ECPublicKey)) {
      throw new InvalidKeyException("Key must be a PublicKey with algorithm EC");
    }
    ECPublicKey pub = (ECPublicKey) key;
    ECParameterSpec ps = privateKey.getParams();
    BigInteger p = ((java.security.spec.ECFieldFp) ps.getCurve().getField()).getP();
    ECPoint w = pub.getW();
    if (w.getAffineX().signum() < 0 || w.getAffineX().compareTo(p) >= 0 || w.getAffineY().signum() < 0
        || w.getAffineY().compareTo(p) >= 0) {
      throw new InvalidKeyException("Invalid coordinate");
    }
    ECOperations ops = new ECOperations(ps);
    if (!ops.isOnCurve(w)) {
      throw new InvalidKeyException("Point is not on curve");
    }
    publicKey = pub;
    return null;
  }

  private byte[] deriveKey() {
    ECOperations ops = new ECOperations(privateKey.getParams());
    ECPoint shared = ops.multiply(publicKey.getW(), privateKey.getS());
    int len = ops.fieldBytes();
    byte[] x = shared.getAffineX().toByteArray();
    byte[] out = new byte[len];
    if (x.length >= len) {
      System.arraycopy(x, x.length - len, out, 0, len);
    } else {
      System.arraycopy(x, 0, out, len - x.length, x.length);
    }
    return out;
  }

  @Override
  protected byte[] engineGenerateSecret() throws IllegalStateException {
    if (privateKey == null || publicKey == null) {
      throw new IllegalStateException("Not initialized correctly");
    }
    byte[] result = deriveKey();
    publicKey = null;
    return result;
  }

  @Override
  protected int engineGenerateSecret(byte[] sharedSecret, int offset) throws IllegalStateException,
      ShortBufferException {
    if (privateKey == null || publicKey == null) {
      throw new IllegalStateException("Not initialized correctly");
    }
    int secretLen = (privateKey.getParams().getCurve().getField().getFieldSize() + 7) >> 3;
    if (offset + secretLen > sharedSecret.length) {
      throw new ShortBufferException("Need " + secretLen + " bytes, only " + (sharedSecret.length - offset)
          + " available");
    }
    byte[] secret = engineGenerateSecret();
    System.arraycopy(secret, 0, sharedSecret, offset, secret.length);
    return secret.length;
  }

  @Override
  protected SecretKey engineGenerateSecret(String algorithm) throws IllegalStateException, NoSuchAlgorithmException,
      InvalidKeyException {
    if (algorithm == null) {
      throw new NoSuchAlgorithmException("Algorithm must not be null");
    }
    if (!(algorithm.equals("TlsPremasterSecret"))) {
      throw new NoSuchAlgorithmException("Unsupported secret key algorithm: " + algorithm);
    }
    return new SecretKeySpec(engineGenerateSecret(), "TlsPremasterSecret");
  }
}
