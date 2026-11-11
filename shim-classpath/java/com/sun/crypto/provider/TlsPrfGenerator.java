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
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import javax.crypto.KeyGeneratorSpi;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import sun.security.internal.spec.TlsPrfParameterSpec;

abstract class TlsPrfGenerator extends KeyGeneratorSpi {
  private static final String kMsg = "TlsPrfGenerator must be initialized using a TlsPrfParameterSpec";

  private TlsPrfParameterSpec spec;

  @Override
  protected void engineInit(SecureRandom random) {
    throw new InvalidParameterException(kMsg);
  }

  @Override
  protected void engineInit(AlgorithmParameterSpec params, SecureRandom random)
      throws InvalidAlgorithmParameterException {
    if (!(params instanceof TlsPrfParameterSpec)) {
      throw new InvalidAlgorithmParameterException(kMsg);
    }
    TlsPrfParameterSpec s = (TlsPrfParameterSpec) params;
    if (s.getSecret() != null && !"RAW".equals(s.getSecret().getFormat())) {
      throw new InvalidAlgorithmParameterException("Key encoding format must be RAW");
    }
    spec = s;
  }

  @Override
  protected void engineInit(int keysize, SecureRandom random) {
    throw new InvalidParameterException(kMsg);
  }

  abstract byte[] prf(byte[] secret, byte[] labelSeed, int length) throws NoSuchAlgorithmException;

  @Override
  protected SecretKey engineGenerateKey() {
    if (spec == null) {
      throw new IllegalStateException("TlsPrfGenerator must be initialized");
    }
    byte[] secret = spec.getSecret() == null ? new byte[0] : spec.getSecret().getEncoded();
    try {
      byte[] labelSeed = TlsKdf.concat(spec.getLabel().getBytes(java.nio.charset.StandardCharsets.UTF_8),
          spec.getSeed());
      return new SecretKeySpec(prf(secret, labelSeed, spec.getOutputLength()), spec.getKeyAlg());
    } catch (NoSuchAlgorithmException e) {
      throw new java.security.ProviderException("Could not generate PRF", e);
    }
  }

  public static class V10 extends TlsPrfGenerator {
    @Override
    byte[] prf(byte[] secret, byte[] labelSeed, int length) throws NoSuchAlgorithmException {
      return TlsKdf.prf10(secret, labelSeed, length);
    }
  }

  public static class V12 extends TlsPrfGenerator {
    @Override
    byte[] prf(byte[] secret, byte[] labelSeed, int length) throws NoSuchAlgorithmException {
      return TlsKdf.prf12(secret, labelSeed, length, spec().getPRFHashAlg(), spec().getPRFBlockSize());
    }
  }

  TlsPrfParameterSpec spec() {
    return spec;
  }
}
