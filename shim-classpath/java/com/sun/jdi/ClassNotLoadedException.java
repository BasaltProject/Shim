/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.jdi;

public class ClassNotLoadedException extends java.lang.Exception {
  private static final long serialVersionUID = -6242978768444298722L;


  public ClassNotLoadedException(java.lang.String string) {
    super(string);
  }
  public ClassNotLoadedException(java.lang.String string, java.lang.String string2) {
  }

  public java.lang.String className() {
    throw new UnsupportedOperationException();
  }
}
