/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security;

public final class KeyPair implements java.io.Serializable, DEREncodable {
  private static final long serialVersionUID = -7565189502268009837L;

  private PrivateKey privateKey;
  private PublicKey publicKey;

  public KeyPair(PublicKey publicKey, PrivateKey privateKey) {
    this.publicKey = publicKey;
    this.privateKey = privateKey;
  }

  public PublicKey getPublic() {
    return publicKey;
  }

  public PrivateKey getPrivate() {
    return privateKey;
  }
}
