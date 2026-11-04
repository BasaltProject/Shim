/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.xml.crypto.dsig.spec;

public final class HMACParameterSpec implements SignatureMethodParameterSpec {
  private int outputLength;

  public HMACParameterSpec(int outputLength) {
    this.outputLength = outputLength;
  }

  public int getOutputLength() {
    return outputLength;
  }
}
