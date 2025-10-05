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

public final class DESedeKeyGenerator extends KeyGeneratorSpi {
  private SecureRandom random = null;
  private int keysize = 168;

  public DESedeKeyGenerator() {
  }

  @Override
  protected void engineInit(SecureRandom random) {
    this.random = random;
  }

  @Override
  protected void engineInit(AlgorithmParameterSpec params, SecureRandom random)
      throws InvalidAlgorithmParameterException {
    throw new InvalidAlgorithmParameterException("Triple DES key generation does not take any parameters");
  }

  @Override
  protected void engineInit(int keysize, SecureRandom random) {
    if ((keysize != 112) && (keysize != 168)) {
      throw new InvalidParameterException("Wrong keysize: must be equal to 112 or 168");
    }
    this.keysize = keysize;
    this.engineInit(random);
  }

  @Override
  protected SecretKey engineGenerateKey() {
    if (this.random == null) {
      this.random = new SecureRandom();
    }
    byte[] rawkey = new byte[24];
    if (keysize == 168) {
      this.random.nextBytes(rawkey);
      DESKeyGenerator.setParityBit(rawkey, 0, 24);
    } else {
      byte[] tmp = new byte[16];
      this.random.nextBytes(tmp);
      DESKeyGenerator.setParityBit(tmp, 0, 16);
      System.arraycopy(tmp, 0, rawkey, 0, 16);
      System.arraycopy(tmp, 0, rawkey, 16, 8);
    }
    try {
      return new DESedeKey(rawkey);
    } catch (InvalidKeyException e) {
      throw new ProviderException(e);
    }
  }
}
