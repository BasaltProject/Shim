/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security.spec;

import java.math.BigInteger;

public class XECPublicKeySpec implements KeySpec {
  private final AlgorithmParameterSpec params;
  private final BigInteger u;

  public XECPublicKeySpec(AlgorithmParameterSpec params, BigInteger u) {
    java.util.Objects.requireNonNull(params, "params must not be null");
    java.util.Objects.requireNonNull(u, "u must not be null");
    this.params = params;
    this.u = u;
  }

  public AlgorithmParameterSpec getParams() {
    return params;
  }

  public BigInteger getU() {
    return u;
  }
}
