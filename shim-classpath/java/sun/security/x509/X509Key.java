/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.x509;

import java.security.PublicKey;

final class X509Key implements PublicKey {
  private static final long serialVersionUID = -5359250853002055002L;

  private final String algorithm;
  private final byte[] encoded;

  X509Key(String algorithm, byte[] encoded) {
    this.algorithm = algorithm;
    this.encoded = encoded.clone();
  }

  @Override
  public String getAlgorithm() {
    return algorithm;
  }

  @Override
  public String getFormat() {
    return "X.509";
  }

  @Override
  public byte[] getEncoded() {
    return encoded.clone();
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof PublicKey && java.util.Arrays.equals(encoded, ((PublicKey) o).getEncoded());
  }

  @Override
  public int hashCode() {
    return java.util.Arrays.hashCode(encoded);
  }

  @Override
  public String toString() {
    return "algorithm = " + algorithm + ", unparsed keybits = \n" + new sun.security.util.HexDumpEncoder()
        .encodeBuffer(encoded);
  }
}
