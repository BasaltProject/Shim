/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security.spec;

import java.math.BigInteger;

public final class EdECPoint {
  private final boolean xOdd;
  private final BigInteger y;

  public EdECPoint(boolean xOdd, BigInteger y) {
    java.util.Objects.requireNonNull(y, "y must not be null");
    this.xOdd = xOdd;
    this.y = y;
  }

  public boolean isXOdd() {
    return xOdd;
  }

  public BigInteger getY() {
    return y;
  }
}
