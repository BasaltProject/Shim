/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.xml.crypto;

public class KeySelectorException extends Exception {
  private static final long serialVersionUID = -7480033639322531109L;

  private Throwable cause;

  public KeySelectorException() {
    super();
  }

  public KeySelectorException(String message) {
    super(message);
  }

  public KeySelectorException(String message, Throwable cause) {
    super(message);
    this.cause = cause;
  }

  public KeySelectorException(Throwable cause) {
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
