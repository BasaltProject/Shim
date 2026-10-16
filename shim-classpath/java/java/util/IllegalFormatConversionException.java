/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.util;

public class IllegalFormatConversionException extends IllegalFormatException {
  private static final long serialVersionUID = 17000126L;

  private final char c;
  private final Class<?> arg;

  public IllegalFormatConversionException(char c, Class<?> arg) {
    this.c = c;
    this.arg = arg;
  }

  public char getConversion() {
    return c;
  }

  public Class<?> getArgumentClass() {
    return arg;
  }

  public String getMessage() {
    return String.format("%c != %s", c, arg.getName());
  }
}
