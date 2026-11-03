/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package javax.swing.colorchooser;

import java.awt.Color;
import java.awt.Dimension;
import javax.swing.UIManager;

class RecentSwatchPanel extends SwatchPanel {
  protected void initValues() {
    swatchSize = sizeOf("ColorChooser.swatchesRecentSwatchSize");
    numSwatches = new Dimension(5, 7);
    gap = new Dimension(1, 1);
  }

  protected void initColors() {
    Color defaultRecent = UIManager.getColor("ColorChooser.swatchesDefaultRecentColor");
    colors = new Color[numSwatches.width * numSwatches.height];
    for (int i = 0; i < colors.length; i++) {
      colors[i] = defaultRecent == null ? Color.white : defaultRecent;
    }
  }

  void setMostRecentColor(Color c) {
    System.arraycopy(colors, 0, colors, 1, colors.length - 1);
    colors[0] = c;
    repaint();
  }
}
