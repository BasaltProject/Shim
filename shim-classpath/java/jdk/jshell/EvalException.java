/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.jshell;

public class EvalException extends JShellException {
  private static final long serialVersionUID = -1530766567050978814L;
  private final String exceptionClassName;

  EvalException(String message, String exceptionClassName, StackTraceElement[] stackTrace, JShellException cause) {
    super(message);
    this.exceptionClassName = exceptionClassName;
    setStackTrace(stackTrace);
    if (cause != null) {
      initCause(cause);
    }
  }

  public JShellException getCause() {
    return (JShellException) super.getCause();
  }

  public String getExceptionClassName() {
    return exceptionClassName;
  }
}
