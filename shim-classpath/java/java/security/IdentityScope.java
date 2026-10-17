/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security;

import java.util.Enumeration;

@Deprecated
public abstract class IdentityScope extends Identity {
  private static final long serialVersionUID = -2337346281189773310L;

  private static IdentityScope scope;

  private static void initializeSystemScope() {
  }

  protected IdentityScope() {
    this("restoring...");
  }

  public IdentityScope(String name) {
    super(name);
  }

  public IdentityScope(String name, IdentityScope scope) throws KeyManagementException {
    super(name, scope);
  }

  public static IdentityScope getSystemScope() {
    if (scope == null) {
      initializeSystemScope();
    }
    return scope;
  }

  protected static void setSystemScope(IdentityScope scope) {
    IdentityScope.scope = scope;
  }

  public abstract int size();

  public abstract Identity getIdentity(String name);

  public Identity getIdentity(Principal principal) {
    return getIdentity(principal.getName());
  }

  public abstract Identity getIdentity(PublicKey key);

  public abstract void addIdentity(Identity identity) throws KeyManagementException;

  public abstract void removeIdentity(Identity identity) throws KeyManagementException;

  public abstract Enumeration<Identity> identities();

  @Override
  public String toString() {
    return super.toString() + "[" + size() + "]";
  }
}
