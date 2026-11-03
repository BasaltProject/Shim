/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */


package javax.swing.plaf.metal;

import java.awt.Component;
import java.awt.Graphics;
import javax.swing.Icon;

public class MetalComboBoxIcon implements Icon, java.io.Serializable {
  public MetalComboBoxIcon() {
  }

  public void paintIcon(Component c, Graphics g, int x, int y) {
    javax.swing.JComponent component = (javax.swing.JComponent) c;
    int iconWidth = getIconWidth();
    g.translate(x, y);
    g.setColor(component.isEnabled() ? MetalLookAndFeel.getControlInfo() : MetalLookAndFeel.getControlShadow());
    g.drawLine(0, 0, iconWidth - 1, 0);
    g.drawLine(1, 1, 1 + (iconWidth - 3), 1);
    g.drawLine(2, 2, 2 + (iconWidth - 5), 2);
    g.drawLine(3, 3, 3 + (iconWidth - 7), 3);
    g.drawLine(4, 4, 4 + (iconWidth - 9), 4);
    g.translate(-x, -y);
  }

  public int getIconWidth() {
    return 10;
  }

  public int getIconHeight() {
    return 5;
  }
}
