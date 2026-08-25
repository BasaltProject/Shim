/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.naming.ldap.spi;

public abstract class LdapDnsProvider {
  protected LdapDnsProvider() {
  }

  public abstract java.util.Optional<javax.naming.ldap.spi.LdapDnsProviderResult> lookupEndpoints(java.lang.String string, java.util.Map<?, ?> map) throws javax.naming.NamingException;
}
