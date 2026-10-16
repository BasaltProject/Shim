/*
 * Copyright (c) 2026 BasaltMC.
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang;

public class IllegalCallerException extends RuntimeException {
  private static final long serialVersionUID = -2349421918363102232L;

  public IllegalCallerException() {
    super();
  }

  public IllegalCallerException(String s) {
    super(s);
  }

  public IllegalCallerException(String message, Throwable cause) {
    super(message, cause);
  }

  public IllegalCallerException(Throwable cause) {
    super(cause);
  }
}
