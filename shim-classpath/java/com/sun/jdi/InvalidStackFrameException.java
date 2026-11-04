/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.jdi;

public class InvalidStackFrameException extends java.lang.RuntimeException {
  private static final long serialVersionUID = -1919378296505827922L;


  public InvalidStackFrameException() {
  }
  public InvalidStackFrameException(java.lang.String string) {
    super(string);
  }

}
