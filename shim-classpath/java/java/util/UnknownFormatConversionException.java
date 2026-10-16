/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.util;

public class UnknownFormatConversionException extends IllegalFormatException {
  private static final long serialVersionUID = 19060418L;

  private final String conversion;

  public UnknownFormatConversionException(String conversion) {
    this.conversion = conversion;
  }

  public String getConversion() {
    return conversion;
  }

  public String getMessage() {
    return String.format("Conversion = '%s'", conversion);
  }
}
