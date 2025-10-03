/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.util;

public class MissingFormatWidthException extends IllegalFormatException {
  private static final long serialVersionUID = 15560123L;

  private final String specifier;

  public MissingFormatWidthException(String specifier) {
    this.specifier = specifier;
  }

  public String getFormatSpecifier() {
    return specifier;
  }

  public String getMessage() {
    return specifier;
  }
}
