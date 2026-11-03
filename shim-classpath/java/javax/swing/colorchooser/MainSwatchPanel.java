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

class MainSwatchPanel extends SwatchPanel {
  protected void initValues() {
    swatchSize = sizeOf("ColorChooser.swatchesSwatchSize");
    numSwatches = new Dimension(SwatchColors.kColumns, SwatchColors.kRows);
    gap = new Dimension(1, 1);
  }

  protected void initColors() {
    int[] rgb = SwatchColors.kMain;
    colors = new Color[rgb.length];
    for (int i = 0; i < rgb.length; i++) {
      colors[i] = new Color(rgb[i]);
    }
  }
}
