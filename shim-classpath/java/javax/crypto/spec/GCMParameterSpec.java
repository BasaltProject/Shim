/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.crypto.spec;

import java.security.spec.AlgorithmParameterSpec;

public class GCMParameterSpec implements AlgorithmParameterSpec {
  private final byte[] iv;
  private final int tLen;

  public GCMParameterSpec(int tLen, byte[] src) {
    if (src == null) {
      throw new IllegalArgumentException("src array is null");
    }
    init(tLen, src, 0, src.length);
    this.tLen = tLen;
    this.iv = src.clone();
  }

  public GCMParameterSpec(int tLen, byte[] src, int offset, int len) {
    if (src == null) {
      throw new IllegalArgumentException("src array is null");
    }
    init(tLen, src, offset, len);
    this.tLen = tLen;
    this.iv = new byte[len];
    System.arraycopy(src, offset, this.iv, 0, len);
  }

  private void init(int tLen, byte[] src, int offset, int len) {
    if (tLen < 0) {
      throw new IllegalArgumentException("Length argument is negative");
    }
    if (len < 0) {
      throw new IllegalArgumentException("Length argument is negative");
    }
    if ((offset < 0) || (len > (src.length - offset))) {
      throw new IllegalArgumentException("Invalid buffer arguments");
    }
  }

  public int getTLen() {
    return tLen;
  }

  public byte[] getIV() {
    return iv.clone();
  }
}
