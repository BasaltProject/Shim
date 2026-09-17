/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.jshell;

public class JShellException extends java.lang.Exception {
  private static final long serialVersionUID = 157096081016530777L;

  JShellException(java.lang.String string) {
    super(string);
  }

}
