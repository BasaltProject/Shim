/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.util;

public class IllegalFormatPrecisionException extends IllegalFormatException {
  private final int precision;

  public IllegalFormatPrecisionException(int precision) {
    this.precision = precision;
  }

  public int getPrecision() {
    return precision;
  }

  public String getMessage() {
    return Integer.toString(precision);
  }
}
