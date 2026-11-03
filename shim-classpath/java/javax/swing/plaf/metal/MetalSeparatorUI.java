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
import javax.swing.JSeparator;

public class MetalSeparatorUI extends javax.swing.plaf.basic.BasicSeparatorUI {
  public MetalSeparatorUI() {
  }
  protected void installDefaults(JSeparator c) {
    super.installDefaults(c);
  }
  public static javax.swing.plaf.ComponentUI createUI(JComponent c) {
    return new MetalSeparatorUI();
  }
  public void paint(Graphics g, JComponent c) {
    Dimension s = c.getSize();
    if (((JSeparator) c).getOrientation() == JSeparator.VERTICAL) {
      g.setColor(c.getForeground());
      g.drawLine(0, 0, 0, s.height);
      g.setColor(c.getBackground());
      g.drawLine(1, 0, 1, s.height);
    } else {
      g.setColor(c.getForeground());
      g.drawLine(0, 0, s.width - 1, 0);
      g.setColor(c.getBackground());
      g.drawLine(0, 1, s.width - 1, 1);
    }
  }
  public Dimension getPreferredSize(JComponent c) {
    if (((JSeparator) c).getOrientation() == JSeparator.VERTICAL) {
      return new Dimension(2, 0);
    }
    return new Dimension(0, 2);
  }
}
