/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security.spec;

import java.math.BigInteger;

public class RSAOtherPrimeInfo {
  private final BigInteger prime;
  private final BigInteger primeExponent;
  private final BigInteger crtCoefficient;

  public RSAOtherPrimeInfo(BigInteger prime, BigInteger primeExponent, BigInteger crtCoefficient) {
    if (prime == null) {
      throw new NullPointerException("the prime parameter must be non-null");
    }
    if (primeExponent == null) {
      throw new NullPointerException("the primeExponent parameter must be non-null");
    }
    if (crtCoefficient == null) {
      throw new NullPointerException("the crtCoefficient parameter must be non-null");
    }
    this.prime = prime;
    this.primeExponent = primeExponent;
    this.crtCoefficient = crtCoefficient;
  }

  public final BigInteger getPrime() {
    return prime;
  }

  public final BigInteger getExponent() {
    return primeExponent;
  }

  public final BigInteger getCrtCoefficient() {
    return crtCoefficient;
  }
}
