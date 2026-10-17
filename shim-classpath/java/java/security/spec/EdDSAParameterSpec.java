/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security.spec;

public class EdDSAParameterSpec implements AlgorithmParameterSpec {
  private final boolean prehash;
  private final byte[] context;

  public EdDSAParameterSpec(boolean prehash) {
    this.prehash = prehash;
    this.context = null;
  }

  public EdDSAParameterSpec(boolean prehash, byte[] context) {
    java.util.Objects.requireNonNull(context, "context must not be null");
    if (context.length > 255) {
      throw new IllegalArgumentException("context length cannot be greater than 255");
    }
    this.prehash = prehash;
    this.context = context.clone();
  }

  public boolean isPrehash() {
    return prehash;
  }

  public java.util.Optional<byte[]> getContext() {
    return context == null ? java.util.Optional.<byte[]>empty() : java.util.Optional.of(context.clone());
  }
}
