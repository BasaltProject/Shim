/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.crypto.spec;

import java.security.spec.AlgorithmParameterSpec;

public final class ChaCha20ParameterSpec implements AlgorithmParameterSpec {
  private final byte[] nonce;
  private final int counter;

  public ChaCha20ParameterSpec(byte[] nonce, int counter) {
    this.counter = counter;
    java.util.Objects.requireNonNull(nonce, "Nonce must be non-null");
    this.nonce = nonce.clone();
    if (this.nonce.length != 12) {
      throw new IllegalArgumentException("Nonce must be 96-bits in length");
    }
  }

  public byte[] getNonce() {
    return nonce.clone();
  }

  public int getCounter() {
    return counter;
  }
}
