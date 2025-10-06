/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security.spec;

public abstract class EncodedKeySpec implements KeySpec {
  private final byte[] encodedKey;
  private final String algorithmName;

  public EncodedKeySpec(byte[] encodedKey) {
    if (encodedKey == null) {
      throw new NullPointerException("encodedKey is null");
    }
    this.encodedKey = encodedKey.clone();
    this.algorithmName = null;
  }

  protected EncodedKeySpec(byte[] encodedKey, String algorithm) {
    if (algorithm == null) {
      throw new NullPointerException("algorithm name may not be null");
    }
    if (algorithm.isEmpty()) {
      throw new IllegalArgumentException("algorithm name may not be empty");
    }
    if (encodedKey == null) {
      throw new NullPointerException("encoded key is null");
    }
    this.encodedKey = encodedKey.clone();
    this.algorithmName = algorithm;
  }

  public String getAlgorithm() {
    return algorithmName;
  }

  public byte[] getEncoded() {
    return this.encodedKey.clone();
  }

  public abstract String getFormat();
}
