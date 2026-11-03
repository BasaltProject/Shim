/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.plaf.basic;

public class BasicToolBarSeparatorUI extends javax.swing.plaf.basic.BasicSeparatorUI {
  public BasicToolBarSeparatorUI() {
  }
  protected void installDefaults(javax.swing.JSeparator s) {
    java.awt.Dimension size = ((javax.swing.JToolBar.Separator) s).getSeparatorSize();
    if (size == null || size instanceof javax.swing.plaf.UIResource) {
      javax.swing.JToolBar.Separator sep = (javax.swing.JToolBar.Separator) s;
      size = (java.awt.Dimension) javax.swing.UIManager.get("ToolBar.separatorSize");
      if (size != null) {
        sep.setSeparatorSize(size);
      }
    }
  }
  public static javax.swing.plaf.ComponentUI createUI(javax.swing.JComponent c) {
    return new BasicToolBarSeparatorUI();
  }
  public void paint(java.awt.Graphics g, javax.swing.JComponent c) { }
  public java.awt.Dimension getPreferredSize(javax.swing.JComponent c) {
    java.awt.Dimension size = ((javax.swing.JToolBar.Separator) c).getSeparatorSize();
    return size != null ? size.getSize() : null;
  }
}
