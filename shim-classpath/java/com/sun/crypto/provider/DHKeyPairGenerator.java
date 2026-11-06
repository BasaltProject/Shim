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
import java.security.InvalidParameterException;
import java.security.KeyPair;
import java.security.KeyPairGeneratorSpi;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import javax.crypto.spec.DHParameterSpec;

public final class DHKeyPairGenerator extends KeyPairGeneratorSpi {
  private int keySize = 3072;
  private DHParameterSpec params;
  private int exponentSize;
  private SecureRandom random;

  public DHKeyPairGenerator() {
  }

  static boolean supported(int size) {
    return size <= 1024 || size == 1536 || size == 2048 || size == 3072 || size == 4096 || size == 6144 || size == 8192;
  }

  static boolean validRange(int size) {
    return size >= 512 && size <= 8192 && size % 64 == 0;
  }

  static final String kRangeMessage = "DH key size must be multiple of 64, and can only range from 512 to 8192 (inclusive). "
      + "The specific key size ";

  @Override
  public void initialize(int keysize, SecureRandom random) {
    if (!validRange(keysize)) {
      throw new InvalidParameterException(kRangeMessage + keysize + " is not supported");
    }
    if (!supported(keysize)) {
      throw new InvalidParameterException("Supported DH key size must be multiple of 64 and range from 512 to 1024 "
          + "(inclusive), or 2048, 3072. The specified key size " + keysize + " is not supported");
    }
    this.keySize = keysize;
    this.params = null;
    this.exponentSize = 0;
    this.random = random;
  }

  @Override
  public void initialize(AlgorithmParameterSpec spec, SecureRandom random) throws InvalidAlgorithmParameterException {
    if (!(spec instanceof DHParameterSpec)) {
      throw new InvalidAlgorithmParameterException("Inappropriate parameter type");
    }
    DHParameterSpec ps = (DHParameterSpec) spec;
    int bits = ps.getP().bitLength();
    if (!validRange(bits)) {
      throw new InvalidAlgorithmParameterException(kRangeMessage + bits + " is not supported");
    }
    int l = ps.getL();
    if (l < 0 || l > bits) {
      throw new InvalidAlgorithmParameterException("Exponent size must be positive and no larger than modulus size");
    }
    this.params = ps;
    this.keySize = bits;
    this.exponentSize = l;
    this.random = random;
  }

  @Override
  public KeyPair generateKeyPair() {
    SecureRandom rnd = random != null ? random : new SecureRandom();
    DHParameterSpec ps = params;
    int l = exponentSize;
    if (ps == null) {
      ps = DHParameterCache.predefined(keySize);
      if (ps == null) {
        ps = DHParameterCache.generate(keySize, rnd);
      }
      l = ps.getL();
    } else if (l == 0) {
      l = Math.min(DHParameterCache.defaultExponentSize(keySize), keySize - 1);
    }
    BigInteger p = ps.getP();
    BigInteger g = ps.getG();
    BigInteger limit = p.subtract(BigInteger.ONE);
    BigInteger x;
    do {
      x = new BigInteger(l, rnd).setBit(l - 1);
    } while (x.compareTo(BigInteger.TWO) < 0 || x.compareTo(limit) >= 0);
    BigInteger y = g.modPow(x, p);
    int lw = ps.getL() != 0 || params == null ? l : 0;
    return new KeyPair(new DHPublicKey(y, p, g, lw), new DHPrivateKey(x, p, g, lw));
  }
}
