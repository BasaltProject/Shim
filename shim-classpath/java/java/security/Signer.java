/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security;

@Deprecated
public abstract class Signer extends Identity {
  private static final long serialVersionUID = -1763464102261361480L;

  private PrivateKey privateKey;

  protected Signer() {
    super();
  }

  public Signer(String name) {
    super(name);
  }

  public Signer(String name, IdentityScope scope) throws KeyManagementException {
    super(name, scope);
  }

  public PrivateKey getPrivateKey() {
    return privateKey;
  }

  public final void setKeyPair(KeyPair pair) throws InvalidParameterException, KeyException {
    if (pair.getPublic() == null || pair.getPrivate() == null) {
      throw new InvalidParameterException();
    }
    try {
      setPublicKey(pair.getPublic());
      this.privateKey = pair.getPrivate();
    } catch (KeyManagementException e) {
      throw new KeyException(e.getMessage());
    }
  }

  @Override
  public String toString() {
    return "[Signer]" + super.toString();
  }
}
