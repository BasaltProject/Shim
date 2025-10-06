/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.crypto.spec;

import java.security.spec.AlgorithmParameterSpec;

public class DHGenParameterSpec implements AlgorithmParameterSpec {
  private final int primeSize;
  private final int exponentSize;

  public DHGenParameterSpec(int primeSize, int exponentSize) {
    this.primeSize = primeSize;
    this.exponentSize = exponentSize;
  }

  public int getPrimeSize() {
    return this.primeSize;
  }

  public int getExponentSize() {
    return this.exponentSize;
  }
}
