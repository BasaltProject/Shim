/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.io;

public class CharConversionException extends IOException {
  public CharConversionException() { }

  public CharConversionException(String s) {
    super(s);
  }
}
