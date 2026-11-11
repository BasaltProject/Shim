/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.security;

import java.security.Provider;
import java.util.Collections;
import java.util.HashMap;

public final class JdkLDAPProvider extends Provider {
  private static final long serialVersionUID = 1L;

  public JdkLDAPProvider() {
    super("JdkLDAP", "25", "JdkLDAP Provider (implements LDAP CertStore)");
    putService(new Provider.Service(this, "CertStore", "LDAP", "sun.security.provider.certpath.ldap.LDAPCertStore",
        Collections.<String>emptyList(), new HashMap<String, String>() {
          private static final long serialVersionUID = 1L;
          {
            put("LDAPSchema", "RFC2587");
          }
        }));
  }
}
