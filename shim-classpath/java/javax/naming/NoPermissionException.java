/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.naming;

public class NoPermissionException extends javax.naming.NamingSecurityException {
  private static final long serialVersionUID = 8395332708699751775L;


  public NoPermissionException(java.lang.String string) {
    super(string);
  }
  public NoPermissionException() {
  }

}
