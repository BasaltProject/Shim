/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.print;

public class PrintException extends java.lang.Exception {
  public PrintException(java.lang.String p0, java.lang.Exception p1) {
    super(p0, p1);
  }
  public PrintException(java.lang.Exception p0) {
    super(p0);
  }
  public PrintException(java.lang.String p0) {
    super(p0);
  }
  public PrintException() {
  }
}
