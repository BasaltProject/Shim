/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */


package javax.swing.plaf.metal;

import java.awt.Graphics;
import javax.swing.JComponent;
import javax.swing.plaf.basic.BasicMenuBarUI;

public class MetalMenuBarUI extends BasicMenuBarUI {
  public MetalMenuBarUI() {
  }

  public static javax.swing.plaf.ComponentUI createUI(JComponent x) {
    return new MetalMenuBarUI();
  }

  public void installUI(JComponent c) {
    super.installUI(c);
  }

  public void uninstallUI(JComponent c) {
    super.uninstallUI(c);
  }

  public void update(Graphics g, JComponent c) {
    if (c.isOpaque()) {
      g.setColor(c.getBackground());
      g.fillRect(0, 0, c.getWidth(), c.getHeight());
    }
    paint(g, c);
  }
}
