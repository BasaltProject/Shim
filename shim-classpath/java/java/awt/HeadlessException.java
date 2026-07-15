/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt;

public class HeadlessException extends java.lang.UnsupportedOperationException {
  public HeadlessException() {
  }
  public HeadlessException(java.lang.String p0) {
    super(p0);
  }
  public java.lang.String getMessage() { return super.getMessage(); }
}
