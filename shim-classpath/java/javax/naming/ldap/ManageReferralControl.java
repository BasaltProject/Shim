/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.naming.ldap;

public final class ManageReferralControl extends BasicControl {
  private static final long serialVersionUID = 3017756160149982566L;

  public static final String OID = "2.16.840.1.113730.3.4.2";

  public ManageReferralControl() {
    super(OID, true, null);
  }

  public ManageReferralControl(boolean criticality) {
    super(OID, criticality, null);
  }
}
