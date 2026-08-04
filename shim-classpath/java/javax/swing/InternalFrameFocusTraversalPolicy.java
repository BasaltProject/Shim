/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing;

public abstract class InternalFrameFocusTraversalPolicy extends java.awt.FocusTraversalPolicy {
  protected InternalFrameFocusTraversalPolicy() {
  }
  public java.awt.Component getInitialComponent(javax.swing.JInternalFrame frame) {
    java.awt.Component c = frame.getMostRecentFocusOwner();
    if (c != null) {
      return c;
    }
    return getDefaultComponent(frame);
  }
}
