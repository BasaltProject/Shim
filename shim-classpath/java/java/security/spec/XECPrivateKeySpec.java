/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security.spec;

public class XECPrivateKeySpec implements KeySpec {
  private final AlgorithmParameterSpec params;
  private final byte[] scalar;

  public XECPrivateKeySpec(AlgorithmParameterSpec params, byte[] scalar) {
    java.util.Objects.requireNonNull(params, "params must not be null");
    java.util.Objects.requireNonNull(scalar, "scalar must not be null");
    this.params = params;
    this.scalar = scalar.clone();
  }

  public AlgorithmParameterSpec getParams() {
    return params;
  }

  public byte[] getScalar() {
    return scalar.clone();
  }
}
