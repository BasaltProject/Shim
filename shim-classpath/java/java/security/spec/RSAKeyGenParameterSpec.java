/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security.spec;

import java.math.BigInteger;

public class RSAKeyGenParameterSpec implements AlgorithmParameterSpec {
  private final int keysize;
  private final BigInteger publicExponent;
  private final AlgorithmParameterSpec keyParams;

  public static final BigInteger F0 = BigInteger.valueOf(3);
  public static final BigInteger F4 = BigInteger.valueOf(65537);

  public RSAKeyGenParameterSpec(int keysize, BigInteger publicExponent) {
    this(keysize, publicExponent, null);
  }

  public RSAKeyGenParameterSpec(int keysize, BigInteger publicExponent, AlgorithmParameterSpec keyParams) {
    this.keysize = keysize;
    this.publicExponent = publicExponent;
    this.keyParams = keyParams;
  }

  public int getKeysize() {
    return keysize;
  }

  public BigInteger getPublicExponent() {
    return publicExponent;
  }

  public AlgorithmParameterSpec getKeyParams() {
    return keyParams;
  }
}
