/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security;

import java.security.spec.AlgorithmParameterSpec;

public abstract class KeyPairGeneratorSpi {
  public KeyPairGeneratorSpi() {
  }

  public abstract void initialize(int keysize, SecureRandom random);

  public void initialize(AlgorithmParameterSpec params, SecureRandom random) throws InvalidAlgorithmParameterException {
    throw new UnsupportedOperationException();
  }

  public abstract KeyPair generateKeyPair();
}
