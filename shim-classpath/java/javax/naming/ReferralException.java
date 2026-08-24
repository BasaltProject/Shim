/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.naming;

public abstract class ReferralException extends javax.naming.NamingException {
  private static final long serialVersionUID = -2881363844695698876L;


  protected ReferralException(java.lang.String string) {
    super(string);
  }
  protected ReferralException() {
  }

  public abstract javax.naming.Context getReferralContext() throws javax.naming.NamingException;
  public abstract javax.naming.Context getReferralContext(java.util.Hashtable<?, ?> hashtable) throws javax.naming.NamingException;
  public abstract java.lang.Object getReferralInfo();
  public abstract void retryReferral();
  public abstract boolean skipReferral();
}
