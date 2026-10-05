/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.jdi;

public class VMOutOfMemoryException extends java.lang.RuntimeException {
  private static final long serialVersionUID = 71504228548910686L;


  public VMOutOfMemoryException() {
  }
  public VMOutOfMemoryException(java.lang.String string) {
    super(string);
  }

}
