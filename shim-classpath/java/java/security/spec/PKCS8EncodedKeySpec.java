/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security.spec;

public class PKCS8EncodedKeySpec extends EncodedKeySpec implements java.security.DEREncodable {
  public PKCS8EncodedKeySpec(byte[] encodedKey) {
    super(encodedKey);
  }

  public PKCS8EncodedKeySpec(byte[] encodedKey, String algorithm) {
    super(encodedKey, algorithm);
  }

  @Override
  public byte[] getEncoded() {
    return super.getEncoded();
  }

  @Override
  public final String getFormat() {
    return "PKCS#8";
  }
}
