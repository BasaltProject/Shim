/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.xml.crypto.dsig;

public class XMLSignatureException extends Exception {
  private static final long serialVersionUID = -3438102491013869995L;

  private Throwable cause;

  public XMLSignatureException() {
    super();
  }

  public XMLSignatureException(String message) {
    super(message);
  }

  public XMLSignatureException(String message, Throwable cause) {
    super(message);
    this.cause = cause;
  }

  public XMLSignatureException(Throwable cause) {
    super(cause == null ? null : cause.toString());
    this.cause = cause;
  }

  public Throwable getCause() {
    return cause;
  }

  public void printStackTrace() {
    super.printStackTrace();
    if (cause != null) {
      cause.printStackTrace();
    }
  }

  public void printStackTrace(java.io.PrintStream s) {
    super.printStackTrace(s);
    if (cause != null) {
      cause.printStackTrace(s);
    }
  }

  public void printStackTrace(java.io.PrintWriter s) {
    super.printStackTrace(s);
    if (cause != null) {
      cause.printStackTrace(s);
    }
  }
}
