/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.jdi;

public class InvocationException extends java.lang.Exception {
  private static final long serialVersionUID = 6066780907971918568L;


  public InvocationException(com.sun.jdi.ObjectReference objectReference) {
  }

  public com.sun.jdi.ObjectReference exception() {
    throw new UnsupportedOperationException();
  }
}
