/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.naming.ldap;

public interface UnsolicitedNotification extends javax.naming.ldap.ExtendedResponse, javax.naming.ldap.HasControls {
  javax.naming.NamingException getException();
  java.lang.String[] getReferrals();
}
