/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.crypto.spec;

import java.math.BigInteger;
import java.security.spec.AlgorithmParameterSpec;

public class DHParameterSpec implements AlgorithmParameterSpec {
  private final BigInteger p;
  private final BigInteger g;
  private final int l;

  public DHParameterSpec(BigInteger p, BigInteger g) {
    this.p = p;
    this.g = g;
    this.l = 0;
  }

  public DHParameterSpec(BigInteger p, BigInteger g, int l) {
    this.p = p;
    this.g = g;
    this.l = l;
  }

  public BigInteger getP() {
    return this.p;
  }

  public BigInteger getG() {
    return this.g;
  }

  public int getL() {
    return this.l;
  }
}
