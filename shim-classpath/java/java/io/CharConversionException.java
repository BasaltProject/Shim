/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.io;

public class CharConversionException extends IOException {
  private static final long serialVersionUID = -8680016352018427031L;

  public CharConversionException() { }

  public CharConversionException(String s) {
    super(s);
  }
}
