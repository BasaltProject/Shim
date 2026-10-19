/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.instrument;

public class IllegalClassFormatException extends Exception {
  private static final long serialVersionUID = -3720185115690124304L;

  public IllegalClassFormatException() {
    super();
  }

  public IllegalClassFormatException(String s) {
    super(s);
  }
}
