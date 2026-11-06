/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.security;

import java.security.Provider;

public final class JdkLDAPProvider extends Provider {
  private static final long serialVersionUID = 1L;

  public JdkLDAPProvider() {
    super("JdkLDAP", "25", "JdkLDAP Provider (implements LDAP CertStore)");
  }
}
