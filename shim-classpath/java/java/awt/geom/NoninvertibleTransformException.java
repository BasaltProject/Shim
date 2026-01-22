/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.geom;

public class NoninvertibleTransformException extends java.lang.Exception {
  private static final long serialVersionUID = 6137225240503990466L;

  public NoninvertibleTransformException(String s) {
    super(s);
  }
}
