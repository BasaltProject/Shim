/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing;

public abstract class InputVerifier {
  protected InputVerifier() {
  }
  public abstract boolean verify(javax.swing.JComponent p0);
  public boolean shouldYieldFocus(javax.swing.JComponent source, javax.swing.JComponent target) {
    return shouldYieldFocus(source);
  }
  public boolean shouldYieldFocus(javax.swing.JComponent input) { return verify(input); }
  public boolean verifyTarget(javax.swing.JComponent input) { return true; }
}
