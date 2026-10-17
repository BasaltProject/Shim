/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security;

public class PrivilegedActionException extends Exception {
  private static final long serialVersionUID = 4724086851538908602L;

  public PrivilegedActionException(Exception exception) {
    super(null, exception);
  }

  public Exception getException() {
    return (Exception) super.getCause();
  }

  @Override
  public Throwable getCause() {
    return super.getCause();
  }

  @Override
  public String toString() {
    String s = getClass().getName();
    Throwable cause = super.getCause();
    return cause != null ? s + ": " + cause.toString() : s;
  }
}
