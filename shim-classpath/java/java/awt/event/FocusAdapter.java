/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.event;

public abstract class FocusAdapter implements java.awt.event.FocusListener {
  protected FocusAdapter() {
  }
  public void focusGained(java.awt.event.FocusEvent p0) { }
  public void focusLost(java.awt.event.FocusEvent p0) { }
}
