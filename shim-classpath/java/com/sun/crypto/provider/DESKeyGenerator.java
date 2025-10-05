/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.crypto.provider;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.ProviderException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import javax.crypto.KeyGeneratorSpi;
import javax.crypto.SecretKey;
import javax.crypto.spec.DESKeySpec;

public final class DESKeyGenerator extends KeyGeneratorSpi {
  private SecureRandom random = null;

  public DESKeyGenerator() {
  }

  @Override
  protected void engineInit(SecureRandom random) {
    this.random = random;
  }

  @Override
  protected void engineInit(AlgorithmParameterSpec params, SecureRandom random)
      throws InvalidAlgorithmParameterException {
    throw new InvalidAlgorithmParameterException("DES key generation does not take any parameters");
  }

  @Override
  protected void engineInit(int keysize, SecureRandom random) {
    if (keysize != 56) {
      throw new InvalidParameterException("Wrong keysize: must be equal to 56");
    }
    engineInit(random);
  }

  @Override
  protected SecretKey engineGenerateKey() {
    if (this.random == null) {
      this.random = new SecureRandom();
    }
    byte[] key = new byte[8];
    try {
      do {
        this.random.nextBytes(key);
        setParityBit(key, 0, 8);
      } while (DESKeySpec.isWeak(key, 0));
      return new DESKey(key);
    } catch (InvalidKeyException e) {
      throw new ProviderException(e);
    }
  }

  static void setParityBit(byte[] key, int offset, int len) {
    for (int i = offset; i < offset + len; i++) {
      int b = key[i] & 0xfe;
      b |= (Integer.bitCount(b) & 1) ^ 1;
      key[i] = (byte) b;
    }
  }
}
