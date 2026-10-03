/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package netscape.javascript;

public class JSException extends java.lang.RuntimeException {
  private static final long serialVersionUID = 2778103758223661489L;


  public JSException() {
  }
  public JSException(java.lang.String string) {
    super(string);
  }
  public JSException(java.lang.Throwable throwable) {
    super(throwable);
  }

}
