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
import sun.security.internal.spec.TlsRsaPremasterSecretParameterSpec;

public final class TlsRsaPremasterSecretGenerator extends KeyGeneratorSpi {
  private static final String kMsg = "TlsRsaPremasterSecretGenerator must be initialized using a "
      + "TlsRsaPremasterSecretParameterSpec";

  private TlsRsaPremasterSecretParameterSpec spec;
  private SecureRandom random;

  @Override
  protected void engineInit(SecureRandom random) {
    throw new InvalidParameterException(kMsg);
  }

  @Override
  protected void engineInit(AlgorithmParameterSpec params, SecureRandom random)
      throws InvalidAlgorithmParameterException {
    if (!(params instanceof TlsRsaPremasterSecretParameterSpec)) {
      throw new InvalidAlgorithmParameterException(kMsg);
    }
    this.spec = (TlsRsaPremasterSecretParameterSpec) params;
    this.random = random;
  }

  @Override
  protected void engineInit(int keysize, SecureRandom random) {
    throw new InvalidParameterException(kMsg);
  }

  @Override
  protected SecretKey engineGenerateKey() {
    if (spec == null) {
      throw new IllegalStateException("TlsRsaPremasterSecretGenerator must be initialized");
    }
    byte[] secret = spec.getEncodedSecret();
    if (secret == null) {
      SecureRandom r = random == null ? new SecureRandom() : random;
      secret = new byte[48];
      r.nextBytes(secret);
    }
    secret[0] = (byte) spec.getMajorVersion();
    secret[1] = (byte) spec.getMinorVersion();
    return new SecretKeySpec(secret, "TlsRsaPremasterSecret");
  }
}
