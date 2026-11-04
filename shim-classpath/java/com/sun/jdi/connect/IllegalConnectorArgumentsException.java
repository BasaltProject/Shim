/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.jdi.connect;

public class IllegalConnectorArgumentsException extends java.lang.Exception {
  private static final long serialVersionUID = -3042212603611350941L;


  public IllegalConnectorArgumentsException(java.lang.String string, java.lang.String string2) {
  }
  public IllegalConnectorArgumentsException(java.lang.String string, java.util.List<java.lang.String> list) {
  }

  public java.util.List<java.lang.String> argumentNames() {
    throw new UnsupportedOperationException();
  }
}
