/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */


package javax.swing.plaf.metal;

import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.JComponent;

public class MetalPopupMenuSeparatorUI extends MetalSeparatorUI {
  public MetalPopupMenuSeparatorUI() {
  }

  public static javax.swing.plaf.ComponentUI createUI(JComponent c) {
    return new MetalPopupMenuSeparatorUI();
  }

  public void paint(Graphics g, JComponent c) {
    Dimension s = c.getSize();
    g.setColor(c.getForeground());
    g.drawLine(0, 2, s.width, 2);
    g.setColor(c.getBackground());
    g.drawLine(0, 3, s.width, 3);
  }

  public Dimension getPreferredSize(JComponent c) {
    return new Dimension(0, 4);
  }

  public Dimension getMaximumSize(JComponent c) {
    Dimension size = getPreferredSize(c);
    size.width = Short.MAX_VALUE;
    return size;
  }
}
