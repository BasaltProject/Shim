/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security.spec;

import java.math.BigInteger;

public class ECPoint {
  private final BigInteger x;
  private final BigInteger y;

  public static final ECPoint POINT_INFINITY = new ECPoint();

  private ECPoint() {
    this.x = null;
    this.y = null;
  }

  public ECPoint(BigInteger x, BigInteger y) {
    if ((x == null) || (y == null)) {
      throw new NullPointerException("affine coordinate x or y is null");
    }
    this.x = x;
    this.y = y;
  }

  public BigInteger getAffineX() {
    return x;
  }

  public BigInteger getAffineY() {
    return y;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (this == POINT_INFINITY) {
      return false;
    }
    if (obj instanceof ECPoint) {
      return ((x.equals(((ECPoint) obj).x)) && (y.equals(((ECPoint) obj).y)));
    }
    return false;
  }

  @Override
  public int hashCode() {
    if (this == POINT_INFINITY) {
      return 0;
    }
    return x.hashCode() << 5 + y.hashCode();
  }
}
