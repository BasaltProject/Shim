/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.tools.attach;

public final class AttachPermission extends java.security.BasicPermission {
  private static final long serialVersionUID = -4619447669752976181L;


  public AttachPermission(java.lang.String string) {
    super(string);
  }
  public AttachPermission(java.lang.String string, java.lang.String string2) {
    super(string, string2);
  }

}
