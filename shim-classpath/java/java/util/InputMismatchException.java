/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.util;

public class InputMismatchException extends NoSuchElementException {
  private static final long serialVersionUID = 8811230760997066428L;

  public InputMismatchException() {
    super();
  }

  public InputMismatchException(String s) {
    super(s);
  }
}
