/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.xml.crypto;

public class URIReferenceException extends Exception {
  private static final long serialVersionUID = 7173469703932561419L;

  private Throwable cause;
  private URIReference uriReference;

  public URIReferenceException() {
    super();
  }

  public URIReferenceException(String message) {
    super(message);
  }

  public URIReferenceException(String message, Throwable cause) {
    super(message);
    this.cause = cause;
  }

  public URIReferenceException(Throwable cause) {
    super(cause == null ? null : cause.toString());
    this.cause = cause;
  }

  public URIReferenceException(String message, Throwable cause, URIReference uriReference) {
    this(message, cause);
    if (uriReference == null) {
      throw new NullPointerException("uriReference cannot be null");
    }
    this.uriReference = uriReference;
  }

  public URIReference getURIReference() {
    return uriReference;
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
