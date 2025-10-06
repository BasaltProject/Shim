/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.crypto.spec;

import java.security.spec.AlgorithmParameterSpec;

public class PBEParameterSpec implements AlgorithmParameterSpec {
  private final byte[] salt;
  private final int iterationCount;
  private final AlgorithmParameterSpec paramSpec;

  public PBEParameterSpec(byte[] salt, int iterationCount) {
    this.salt = salt.clone();
    this.iterationCount = iterationCount;
    this.paramSpec = null;
  }

  public PBEParameterSpec(byte[] salt, int iterationCount, AlgorithmParameterSpec paramSpec) {
    this.salt = salt.clone();
    this.iterationCount = iterationCount;
    this.paramSpec = paramSpec;
  }

  public byte[] getSalt() {
    return this.salt.clone();
  }

  public int getIterationCount() {
    return this.iterationCount;
  }

  public AlgorithmParameterSpec getParameterSpec() {
    return this.paramSpec;
  }
}
