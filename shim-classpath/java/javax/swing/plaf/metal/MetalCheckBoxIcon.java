/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.plaf.metal;

public class MetalCheckBoxIcon implements javax.swing.Icon, javax.swing.plaf.UIResource, java.io.Serializable {
  public MetalCheckBoxIcon() {
  }
  public void paintIcon(java.awt.Component c, java.awt.Graphics g, int x, int y) {
    MetalIconFactory.getCheckBoxIcon().paintIcon(c, g, x, y);
  }
  public int getIconWidth() { return getControlSize(); }
  public int getIconHeight() { return getControlSize(); }
  protected int getControlSize() { return 13; }
  protected void drawCheck(java.awt.Component c, java.awt.Graphics g, int x, int y) {
    int controlSize = getControlSize();
    g.fillRect(x + 3, y + 5, 2, controlSize - 8);
    g.drawLine(x + (controlSize - 4), y + 3, x + 5, y + (controlSize - 6));
    g.drawLine(x + (controlSize - 4), y + 4, x + 5, y + (controlSize - 5));
  }
}
