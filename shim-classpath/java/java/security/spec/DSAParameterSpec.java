/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security.spec;

import java.math.BigInteger;

public class DSAParameterSpec implements AlgorithmParameterSpec, java.security.interfaces.DSAParams {
  BigInteger p;
  BigInteger q;
  BigInteger g;

  public DSAParameterSpec(BigInteger p, BigInteger q, BigInteger g) {
    this.p = p;
    this.q = q;
    this.g = g;
  }

  @Override
  public BigInteger getP() {
    return p;
  }

  @Override
  public BigInteger getQ() {
    return q;
  }

  @Override
  public BigInteger getG() {
    return g;
  }
}
