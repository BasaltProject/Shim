/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.crypto.provider;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import javax.crypto.KeyAgreementSpi;
import javax.crypto.SecretKey;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public final class DHKeyAgreement extends KeyAgreementSpi {
  private boolean generateSecret;
  private BigInteger init_p;
  private BigInteger init_g;
  private BigInteger x;
  private byte[] secret;

  public DHKeyAgreement() {
  }

  @Override
  protected void engineInit(Key key, SecureRandom random) throws InvalidKeyException {
    try {
      engineInit(key, null, random);
    } catch (InvalidAlgorithmParameterException e) {
      throw new InvalidKeyException("Diffie-Hellman key agreement requires DHPrivateKey for initialization");
    }
  }

  @Override
  protected void engineInit(Key key, AlgorithmParameterSpec params, SecureRandom random)
      throws InvalidKeyException, InvalidAlgorithmParameterException {
    generateSecret = false;
    secret = null;
    if (!(key instanceof javax.crypto.interfaces.DHPrivateKey)) {
      throw new InvalidKeyException("Diffie-Hellman private key expected");
    }
    javax.crypto.interfaces.DHPrivateKey dhKey = (javax.crypto.interfaces.DHPrivateKey) key;
    if (params != null) {
      if (!(params instanceof DHParameterSpec)) {
        throw new InvalidAlgorithmParameterException("Diffie-Hellman parameter spec expected");
      }
      DHParameterSpec ps = (DHParameterSpec) params;
      init_p = ps.getP();
      init_g = ps.getG();
    } else {
      DHParameterSpec ps = dhKey.getParams();
      init_p = ps.getP();
      init_g = ps.getG();
    }
    x = dhKey.getX();
    BigInteger kp = dhKey.getParams().getP();
    BigInteger kg = dhKey.getParams().getG();
    if (params != null && (!kp.equals(init_p) || !kg.equals(init_g))) {
      throw new InvalidKeyException("Incompatible parameters");
    }
  }

  @Override
  protected Key engineDoPhase(Key key, boolean lastPhase) throws InvalidKeyException, IllegalStateException {
    if (init_p == null) {
      throw new IllegalStateException("Not initialized");
    }
    if (generateSecret) {
      throw new IllegalStateException("Phase already executed");
    }
    if (!(key instanceof javax.crypto.interfaces.DHPublicKey)) {
      throw new InvalidKeyException("Diffie-Hellman public key expected");
    }
    javax.crypto.interfaces.DHPublicKey pub = (javax.crypto.interfaces.DHPublicKey) key;
    DHParameterSpec ps = pub.getParams();
    if (!init_p.equals(ps.getP()) || !init_g.equals(ps.getG())) {
      throw new InvalidKeyException("Incompatible parameters");
    }
    BigInteger y = pub.getY();
    if (y.compareTo(BigInteger.ONE) <= 0) {
      throw new InvalidKeyException("Diffie-Hellman public key is too small");
    }
    if (y.compareTo(init_p.subtract(BigInteger.ONE)) >= 0) {
      throw new InvalidKeyException("Diffie-Hellman public key is too large");
    }
    BigInteger shared = y.modPow(x, init_p);
    if (lastPhase) {
      int len = (init_p.bitLength() + 7) >>> 3;
      byte[] raw = shared.toByteArray();
      byte[] out = new byte[len];
      int copy = Math.min(raw.length, len);
      System.arraycopy(raw, raw.length - copy, out, len - copy, copy);
      secret = out;
      generateSecret = true;
      return null;
    }
    return new DHPublicKey(shared, init_p, init_g);
  }

  @Override
  protected byte[] engineGenerateSecret() throws IllegalStateException {
    int expected = (init_p.bitLength() + 7) >>> 3;
    if (!generateSecret) {
      throw new IllegalStateException("Key agreement has not been completed yet");
    }
    byte[] out = secret.clone();
    reset();
    return out;
  }

  @Override
  protected int engineGenerateSecret(byte[] sharedSecret, int offset) throws IllegalStateException, ShortBufferException {
    if (init_p == null) {
      throw new IllegalStateException("Not initialized");
    }
    if (!generateSecret) {
      throw new IllegalStateException("Key agreement has not been completed yet");
    }
    if (sharedSecret == null || sharedSecret.length - offset < secret.length) {
      throw new ShortBufferException("Buffer too short for shared secret");
    }
    System.arraycopy(secret, 0, sharedSecret, offset, secret.length);
    int n = secret.length;
    reset();
    return n;
  }

  @Override
  protected SecretKey engineGenerateSecret(String algorithm) throws IllegalStateException, NoSuchAlgorithmException,
      InvalidKeyException {
    if (algorithm == null) {
      throw new NoSuchAlgorithmException("null algorithm");
    }
    if (!algorithm.equals("TlsPremasterSecret")) {
      throw new NoSuchAlgorithmException("Unsupported secret key algorithm: " + algorithm);
    }
    if (!generateSecret) {
      throw new IllegalStateException("Key agreement has not been completed yet");
    }
    SecretKey key = new SecretKeySpec(secret, "TlsPremasterSecret");
    reset();
    return key;
  }

  private void reset() {
    generateSecret = false;
    secret = null;
  }
}
