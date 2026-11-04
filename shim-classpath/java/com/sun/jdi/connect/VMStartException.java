/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.jdi.connect;

public class VMStartException extends java.lang.Exception {
  private static final long serialVersionUID = 6408644824640801020L;


  public VMStartException(java.lang.Process process) {
  }
  public VMStartException(java.lang.String string, java.lang.Process process) {
  }

  public java.lang.Process process() {
    throw new UnsupportedOperationException();
  }
}
