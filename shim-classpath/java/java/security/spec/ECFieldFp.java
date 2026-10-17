/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security.spec;

import java.math.BigInteger;

public class ECFieldFp implements ECField {
  private final BigInteger p;

  public ECFieldFp(BigInteger p) {
    if (p.signum() != 1) {
      throw new IllegalArgumentException("p is not positive");
    }
    this.p = p;
  }

  @Override
  public int getFieldSize() {
    return p.bitLength();
  }

  public BigInteger getP() {
    return p;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof ECFieldFp) {
      return p.equals(((ECFieldFp) obj).p);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return p.hashCode();
  }
}
