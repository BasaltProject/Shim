/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.text;

public class ChangedCharSetException extends java.io.IOException {
  private java.lang.String bp_CharSetSpec;
  public ChangedCharSetException(java.lang.String p0, boolean p1) {
    super(p0);
  }
  public java.lang.String getCharSetSpec() { return bp_CharSetSpec; }
  public boolean keyEqualsCharSet() { throw new UnsupportedOperationException(); }
}
