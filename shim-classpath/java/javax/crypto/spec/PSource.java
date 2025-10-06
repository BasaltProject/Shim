/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.crypto.spec;

public class PSource {
  private final String pSrcName;

  protected PSource(String pSrcName) {
    if (pSrcName == null) {
      throw new NullPointerException("pSource algorithm is null");
    }
    this.pSrcName = pSrcName;
  }

  public String getAlgorithm() {
    return pSrcName;
  }

  public static final class PSpecified extends PSource {
    private final byte[] p;

    public static final PSpecified DEFAULT = new PSpecified(new byte[0]);

    public PSpecified(byte[] p) {
      super("PSpecified");
      this.p = p.clone();
    }

    public byte[] getValue() {
      return p.length == 0 ? p : p.clone();
    }
  }
}
