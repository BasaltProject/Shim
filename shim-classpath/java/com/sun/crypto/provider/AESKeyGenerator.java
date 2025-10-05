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

public final class AESKeyGenerator extends KeyGeneratorSpi {
  private SecureRandom random = null;
  private int keySize = 32;

  public AESKeyGenerator() {
  }

  @Override
  protected void engineInit(SecureRandom random) {
    this.random = random;
  }

  @Override
  protected void engineInit(AlgorithmParameterSpec params, SecureRandom random)
      throws InvalidAlgorithmParameterException {
    throw new InvalidAlgorithmParameterException("AES key generation does not take any parameters");
  }

  @Override
  protected void engineInit(int keysize, SecureRandom random) {
    if (((keysize % 8) != 0) || (!AESCrypt.isKeySizeValid(keysize / 8))) {
      throw new InvalidParameterException("Wrong keysize: must be equal to 128, 192 or 256");
    }
    this.keySize = keysize / 8;
    engineInit(random);
  }

  @Override
  protected SecretKey engineGenerateKey() {
    if (this.random == null) {
      this.random = new SecureRandom();
    }
    byte[] keyBytes = new byte[keySize];
    this.random.nextBytes(keyBytes);
    return new SecretKeySpec(keyBytes, "AES");
  }
}
