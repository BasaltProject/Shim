/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.jshell.spi;

public class SPIResolutionException extends java.lang.RuntimeException {
  private static final long serialVersionUID = -4508042524147839363L;


  public SPIResolutionException(int i) {
  }

  public int id() {
    throw new UnsupportedOperationException();
  }
}
