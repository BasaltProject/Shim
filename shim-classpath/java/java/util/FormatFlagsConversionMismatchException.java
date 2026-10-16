/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.util;

public class FormatFlagsConversionMismatchException extends IllegalFormatException {
  private final String flags;
  private final char conversion;

  public FormatFlagsConversionMismatchException(String flags, char conversion) {
    this.flags = flags;
    this.conversion = conversion;
  }

  public String getFlags() {
    return flags;
  }

  public char getConversion() {
    return conversion;
  }

  public String getMessage() {
    return "Conversion = " + conversion + ", Flags = " + flags;
  }
}
