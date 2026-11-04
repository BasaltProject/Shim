/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.jdi;

public class InternalException extends java.lang.RuntimeException {
  private static final long serialVersionUID = -9171606393104480607L;


  public InternalException(java.lang.String string, int i) {
  }
  public InternalException(int i) {
  }
  public InternalException(java.lang.String string) {
    super(string);
  }
  public InternalException() {
  }

  public int errorCode() {
    throw new UnsupportedOperationException();
  }
}
