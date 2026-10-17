/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.crypto.spec;

import java.math.BigInteger;
import java.security.spec.KeySpec;

public class DHPublicKeySpec implements KeySpec {
  private final BigInteger y;
  private final BigInteger p;
  private final BigInteger g;

  public DHPublicKeySpec(BigInteger y, BigInteger p, BigInteger g) {
    this.y = y;
    this.p = p;
    this.g = g;
  }

  public BigInteger getY() {
    return this.y;
  }

  public BigInteger getP() {
    return this.p;
  }

  public BigInteger getG() {
    return this.g;
  }
}
