/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.naming.ldap;

public abstract class LdapReferralException extends javax.naming.ReferralException {
  private static final long serialVersionUID = -1668992791764950804L;


  protected LdapReferralException(java.lang.String string) {
    super(string);
  }
  protected LdapReferralException() {
  }

  public abstract javax.naming.Context getReferralContext() throws javax.naming.NamingException;
  public abstract javax.naming.Context getReferralContext(java.util.Hashtable<?, ?> hashtable, javax.naming.ldap.Control[] controls) throws javax.naming.NamingException;
  public abstract javax.naming.Context getReferralContext(java.util.Hashtable<?, ?> hashtable) throws javax.naming.NamingException;
}
