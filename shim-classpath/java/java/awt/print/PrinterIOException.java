/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.print;

public class PrinterIOException extends java.awt.print.PrinterException {
  private final java.io.IOException mException;

  public PrinterIOException(java.io.IOException exception) {
    initCause(null);
    mException = exception;
  }
  public java.lang.Throwable getCause() { return mException; }
  public java.io.IOException getIOException() { return mException; }
}
