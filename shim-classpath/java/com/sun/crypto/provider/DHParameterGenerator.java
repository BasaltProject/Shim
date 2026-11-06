/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.crypto.provider;

import java.math.BigInteger;
import java.security.AlgorithmParameterGeneratorSpi;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidParameterSpecException;
import javax.crypto.spec.DHGenParameterSpec;
import javax.crypto.spec.DHParameterSpec;

public final class DHParameterGenerator extends AlgorithmParameterGeneratorSpi {
  private static final String kSupported = "Supported DH key size must be multiple of 64 and range from 512 to 1024 "
      + "(inclusive), or 2048, 3072. The specified key size ";
  private int primeSize = 3072;
  private int exponentSize;
  private SecureRandom random;

  public DHParameterGenerator() {
  }

  private static boolean ok(int size) {
    return (size >= 512 && size <= 1024 && size % 64 == 0) || size == 2048 || size == 3072;
  }

  @Override
  protected void engineInit(int size, SecureRandom random) {
    if (!ok(size)) {
      throw new InvalidParameterException(kSupported + size + " is not supported");
    }
    this.primeSize = size;
    this.exponentSize = 0;
    this.random = random;
  }

  @Override
  protected void engineInit(AlgorithmParameterSpec genParamSpec, SecureRandom random)
      throws InvalidAlgorithmParameterException {
    if (!(genParamSpec instanceof DHGenParameterSpec)) {
      throw new InvalidAlgorithmParameterException("Inappropriate parameter type");
    }
    DHGenParameterSpec s = (DHGenParameterSpec) genParamSpec;
    if (!ok(s.getPrimeSize())) {
      throw new InvalidAlgorithmParameterException(kSupported + s.getPrimeSize() + " is not supported");
    }
    if (s.getExponentSize() <= 0 || s.getExponentSize() > s.getPrimeSize()) {
      throw new InvalidAlgorithmParameterException("Exponent size must be positive and no larger than modulus size");
    }
    this.primeSize = s.getPrimeSize();
    this.exponentSize = s.getExponentSize();
    this.random = random;
  }

  @Override
  protected AlgorithmParameters engineGenerateParameters() {
    SecureRandom rnd = random != null ? random : new SecureRandom();
    DHParameterSpec base = DHParameterCache.predefined(primeSize);
    if (base == null) {
      base = DHParameterCache.generate(primeSize, rnd);
    }
    try {
      AlgorithmParameters ap = AlgorithmParameters.getInstance("DH", "SunJCE");
      ap.init(new DHParameterSpec(base.getP(), base.getG(), exponentSize));
      return ap;
    } catch (NoSuchAlgorithmException | java.security.NoSuchProviderException | InvalidParameterSpecException e) {
      throw new java.security.ProviderException(e.getMessage());
    }
  }
}
