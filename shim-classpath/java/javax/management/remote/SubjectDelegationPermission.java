/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management.remote;

public final class SubjectDelegationPermission extends java.security.BasicPermission {
  private static final long serialVersionUID = 1481618113008682343L;


  public SubjectDelegationPermission(java.lang.String string) {
    super(string);
  }
  public SubjectDelegationPermission(java.lang.String string, java.lang.String string2) {
    super(string, string2);
  }

}
