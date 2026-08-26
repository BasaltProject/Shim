/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.security.sasl;

public class SaslException extends java.io.IOException {
  private Throwable _exception;
  private static final long serialVersionUID = 4579784287983423626L;

  public SaslException() {
    super();
  }

  public SaslException(String detail) {
    super(detail);
  }

  public SaslException(String detail, Throwable ex) {
    super(detail);
    if (ex != null) {
      initCause(ex);
    }
  }

  public Throwable getCause() {
    return _exception;
  }

  public Throwable initCause(Throwable cause) {
    super.initCause(cause);
    _exception = cause;
    return this;
  }

  public String toString() {
    String answer = super.toString();
    if (_exception != null && _exception != this) {
      answer += " [Caused by " + _exception.toString() + "]";
    }
    return answer;
  }
}
