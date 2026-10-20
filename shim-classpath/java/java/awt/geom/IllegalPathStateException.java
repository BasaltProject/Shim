/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.geom;

public class IllegalPathStateException extends RuntimeException {
  private static final long serialVersionUID = -5158084205220481094L;

  public IllegalPathStateException() { }

  public IllegalPathStateException(String s) {
    super(s);
  }
}
