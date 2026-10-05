/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.jdi.connect.spi;

public class ClosedConnectionException extends java.io.IOException {
  private static final long serialVersionUID = 3877032124297204774L;


  public ClosedConnectionException() {
  }
  public ClosedConnectionException(java.lang.String string) {
    super(string);
  }

}
