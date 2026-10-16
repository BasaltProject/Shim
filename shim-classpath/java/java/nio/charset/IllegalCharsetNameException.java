/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.nio.charset;

public class IllegalCharsetNameException extends IllegalArgumentException {
  private static final long serialVersionUID = 1457525358470002989L;

  private final String charsetName;

  public IllegalCharsetNameException(String charsetName) {
    super(String.valueOf(charsetName));
    this.charsetName = charsetName;
  }

  public String getCharsetName() {
    return charsetName;
  }
}
