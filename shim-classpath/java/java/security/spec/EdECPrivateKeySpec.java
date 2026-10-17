/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security.spec;

public final class EdECPrivateKeySpec implements KeySpec {
  private final NamedParameterSpec params;
  private final byte[] bytes;

  public EdECPrivateKeySpec(NamedParameterSpec params, byte[] bytes) {
    java.util.Objects.requireNonNull(params, "params must not be null");
    java.util.Objects.requireNonNull(bytes, "bytes must not be null");
    this.params = params;
    this.bytes = bytes.clone();
  }

  public NamedParameterSpec getParams() {
    return params;
  }

  public byte[] getBytes() {
    return bytes.clone();
  }
}
