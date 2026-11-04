/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.jdi;

public class NativeMethodException extends com.sun.jdi.OpaqueFrameException {
  private static final long serialVersionUID = 3924951669039469992L;


  public NativeMethodException() {
  }
  public NativeMethodException(java.lang.String string) {
    super(string);
  }

}
