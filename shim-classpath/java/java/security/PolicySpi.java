/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security;

public abstract class PolicySpi {
  public PolicySpi() {
  }

  protected abstract boolean engineImplies(ProtectionDomain domain, Permission permission);

  protected void engineRefresh() {
  }

  protected PermissionCollection engineGetPermissions(CodeSource codesource) {
    return Policy.UNSUPPORTED_EMPTY_COLLECTION;
  }

  protected PermissionCollection engineGetPermissions(ProtectionDomain domain) {
    return Policy.UNSUPPORTED_EMPTY_COLLECTION;
  }
}
