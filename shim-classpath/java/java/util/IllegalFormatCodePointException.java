/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.util;

public class IllegalFormatCodePointException extends IllegalFormatException {
  private static final long serialVersionUID = 19080630L;

  private final int c;

  public IllegalFormatCodePointException(int c) {
    this.c = c;
  }

  public int getCodePoint() {
    return c;
  }

  public String getMessage() {
    return String.format("Code point = %#x", c);
  }
}
