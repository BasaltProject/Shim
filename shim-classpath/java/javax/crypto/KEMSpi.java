/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.crypto;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;

public interface KEMSpi {
  EncapsulatorSpi engineNewEncapsulator(PublicKey publicKey, AlgorithmParameterSpec spec, SecureRandom secureRandom)
      throws InvalidAlgorithmParameterException, InvalidKeyException;

  DecapsulatorSpi engineNewDecapsulator(PrivateKey privateKey, AlgorithmParameterSpec spec)
      throws InvalidAlgorithmParameterException, InvalidKeyException;

  interface EncapsulatorSpi {
    KEM.Encapsulated engineEncapsulate(int from, int to, String algorithm);

    int engineSecretSize();

    int engineEncapsulationSize();
  }

  interface DecapsulatorSpi {
    SecretKey engineDecapsulate(byte[] encapsulation, int from, int to, String algorithm) throws DecapsulateException;

    int engineSecretSize();

    int engineEncapsulationSize();
  }
}
