/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.util;

public class IllegalFormatWidthException extends IllegalFormatException {
  private final int width;

  public IllegalFormatWidthException(int width) {
    this.width = width;
  }

  public int getWidth() {
    return width;
  }

  public String getMessage() {
    return Integer.toString(width);
  }
}
