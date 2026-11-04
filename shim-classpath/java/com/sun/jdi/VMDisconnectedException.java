/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.jdi;

public class VMDisconnectedException extends java.lang.RuntimeException {
  private static final long serialVersionUID = 2892975269768351637L;


  public VMDisconnectedException() {
  }
  public VMDisconnectedException(java.lang.String string) {
    super(string);
  }

}
