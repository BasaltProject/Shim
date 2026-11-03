/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing;

public interface MenuElement {
  java.awt.Component getComponent();
  void processKeyEvent(java.awt.event.KeyEvent p0, javax.swing.MenuElement[] p1, javax.swing.MenuSelectionManager p2);
  void processMouseEvent(java.awt.event.MouseEvent p0, javax.swing.MenuElement[] p1, javax.swing.MenuSelectionManager p2);
  javax.swing.MenuElement[] getSubElements();
  void menuSelectionChanged(boolean p0);
}
