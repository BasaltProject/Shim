/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.crypto.provider;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidParameterException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import javax.crypto.KeyGeneratorSpi;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

class KeyGeneratorCore extends KeyGeneratorSpi {
  private final String name;
  private final int defaultKeySize;
  private int keySize;
  private SecureRandom random;

  KeyGeneratorCore(String name, int defaultKeySize) {
    this.name = name;
    this.defaultKeySize = defaultKeySize;
    this.keySize = defaultKeySize >> 3;
  }

  @Override
  protected void engineInit(SecureRandom random) {
    this.random = random;
  }

  @Override
  protected void engineInit(AlgorithmParameterSpec params, SecureRandom random)
      throws InvalidAlgorithmParameterException {
    throw new InvalidAlgorithmParameterException(name + " key generation does not take any parameters");
  }

  @Override
  protected void engineInit(int keysize, SecureRandom random) {
    if (keysize < 40) {
      throw new InvalidParameterException("Key length must be at least 40 bits");
    }
    this.keySize = keysize >> 3;
    engineInit(random);
  }

  @Override
  protected SecretKey engineGenerateKey() {
    if (random == null) {
      random = new SecureRandom();
    }
    byte[] keyBytes = new byte[keySize];
    random.nextBytes(keyBytes);
    return new SecretKeySpec(keyBytes, name);
  }
}
