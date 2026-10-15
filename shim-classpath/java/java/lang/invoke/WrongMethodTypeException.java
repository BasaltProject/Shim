/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.invoke;

public class WrongMethodTypeException extends RuntimeException {
  private static final long serialVersionUID = 292L;

  public WrongMethodTypeException() {
    super();
  }

  public WrongMethodTypeException(String s) {
    super(s);
  }
}
