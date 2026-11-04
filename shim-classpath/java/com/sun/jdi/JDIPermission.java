/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.jdi;

public final class JDIPermission extends java.security.BasicPermission {
  private static final long serialVersionUID = -6988461416938786271L;


  public JDIPermission(java.lang.String string) {
    super(string);
  }
  public JDIPermission(java.lang.String string, java.lang.String string2) throws java.lang.IllegalArgumentException {
    super(string, string2);
  }

}
