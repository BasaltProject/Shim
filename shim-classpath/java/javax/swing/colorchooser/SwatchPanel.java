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
import java.awt.Graphics;
import javax.swing.JPanel;
import javax.swing.UIManager;

abstract class SwatchPanel extends JPanel {
  protected Color[] colors;
  protected Dimension swatchSize;
  protected Dimension numSwatches;
  protected Dimension gap;

  SwatchPanel() {
    initValues();
    initColors();
    setToolTipText("");
    setOpaque(true);
    setBackground(Color.white);
    setRequestFocusEnabled(false);
  }

  protected abstract void initValues();

  protected abstract void initColors();

  public void paintComponent(Graphics g) {
    g.setColor(getBackground());
    g.fillRect(0, 0, getWidth(), getHeight());
    for (int row = 0; row < numSwatches.height; row++) {
      int y = row * (swatchSize.height + gap.height);
      for (int column = 0; column < numSwatches.width; column++) {
        g.setColor(getColorForCell(column, row));
        int x = column * (swatchSize.width + gap.width);
        g.fillRect(x, y, swatchSize.width - 1, swatchSize.height - 1);
        g.setColor(Color.black);
        g.drawLine(x + swatchSize.width - 1, y, x + swatchSize.width - 1, y + swatchSize.height - 1);
        g.drawLine(x, y + swatchSize.height - 1, x + swatchSize.width - 1, y + swatchSize.height - 1);
      }
    }
  }

  public Dimension getPreferredSize() {
    int x = numSwatches.width * (swatchSize.width + gap.width) - 1;
    int y = numSwatches.height * (swatchSize.height + gap.height) - 1;
    return new Dimension(x, y);
  }

  Color getColorForCell(int column, int row) {
    return colors[row * numSwatches.width + column];
  }

  Color getColorForLocation(int x, int y) {
    int column = x / (swatchSize.width + gap.width);
    int row = y / (swatchSize.height + gap.height);
    if (column < 0 || column >= numSwatches.width || row < 0 || row >= numSwatches.height) {
      return null;
    }
    return getColorForCell(column, row);
  }

  public String getToolTipText(java.awt.event.MouseEvent e) {
    Color c = getColorForLocation(e.getX(), e.getY());
    return c == null ? null : c.getRed() + ", " + c.getGreen() + ", " + c.getBlue();
  }

  static Dimension sizeOf(String key) {
    Dimension d = UIManager.getDimension(key);
    return d == null ? new Dimension(10, 10) : new Dimension(d);
  }
}
