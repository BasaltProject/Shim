/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.util;

public class IllformedLocaleException extends RuntimeException {
  private static final long serialVersionUID = -5245986824925681401L;

  private final int errorIndex;

  public IllformedLocaleException() {
    super();
    errorIndex = -1;
  }

  public IllformedLocaleException(String message) {
    super(message);
    errorIndex = -1;
  }

  public IllformedLocaleException(String message, int errorIndex) {
    super(message + (errorIndex < 0 ? "" : " [at index " + errorIndex + "]"));
    this.errorIndex = errorIndex;
  }

  public int getErrorIndex() {
    return errorIndex;
  }
}
