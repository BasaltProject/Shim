/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security.spec;

public class ECPublicKeySpec implements KeySpec {
  private final ECPoint w;
  private final ECParameterSpec params;

  public ECPublicKeySpec(ECPoint w, ECParameterSpec params) {
    if (w == null) {
      throw new NullPointerException("w is null");
    }
    if (params == null) {
      throw new NullPointerException("params is null");
    }
    if (w == ECPoint.POINT_INFINITY) {
      throw new IllegalArgumentException("w is ECPoint.POINT_INFINITY");
    }
    this.w = w;
    this.params = params;
  }

  public ECPoint getW() {
    return w;
  }

  public ECParameterSpec getParams() {
    return params;
  }
}
