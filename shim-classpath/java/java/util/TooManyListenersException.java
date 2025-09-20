/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.util;

public class TooManyListenersException extends Exception {
  public TooManyListenersException() { }

  public TooManyListenersException(String s) {
    super(s);
  }
}
