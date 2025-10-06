/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security.spec;

public final class EdECPublicKeySpec implements KeySpec {
  private final NamedParameterSpec params;
  private final EdECPoint point;

  public EdECPublicKeySpec(NamedParameterSpec params, EdECPoint point) {
    java.util.Objects.requireNonNull(params, "params must not be null");
    java.util.Objects.requireNonNull(point, "point must not be null");
    this.params = params;
    this.point = point;
  }

  public NamedParameterSpec getParams() {
    return params;
  }

  public EdECPoint getPoint() {
    return point;
  }
}
