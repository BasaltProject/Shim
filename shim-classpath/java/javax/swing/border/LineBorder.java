/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.border;

import java.awt.*;
import java.io.Serializable;

public class LineBorder extends AbstractBorder {
  private static Border blackLine;
  private static Border grayLine;

  protected int thickness;
  protected Color lineColor;
  protected boolean roundedCorners;

  public static Border createBlackLineBorder() {
    if (blackLine == null) {
      blackLine = new LineBorder(Color.black, 1);
    }
    return blackLine;
  }

  public static Border createGrayLineBorder() {
    if (grayLine == null) {
      grayLine = new LineBorder(Color.gray, 1);
    }
    return grayLine;
  }

  public LineBorder(Color color) { this(color, 1, false); }

  public LineBorder(Color color, int thickness) { this(color, thickness, false); }

  public LineBorder(Color color, int thickness, boolean roundedCorners) {
    lineColor = color;
    this.thickness = thickness;
    this.roundedCorners = roundedCorners;
  }

  public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
    if (thickness <= 0 || g == null) {
      return;
    }
    Color oldColor = g.getColor();
    g.setColor(lineColor);
    for (int i = 0; i < thickness; i++) {
      if (!roundedCorners) {
        g.drawRect(x + i, y + i, width - i - i - 1, height - i - i - 1);
      } else {
        g.drawRoundRect(x + i, y + i, width - i - i - 1, height - i - i - 1, thickness, thickness);
      }
    }
    g.setColor(oldColor);
  }

  public Insets getBorderInsets(Component c, Insets insets) {
    insets.set(thickness, thickness, thickness, thickness);
    return insets;
  }

  public Color getLineColor() { return lineColor; }

  public int getThickness() { return thickness; }

  public boolean getRoundedCorners() { return roundedCorners; }

  public boolean isBorderOpaque() { return !roundedCorners; }
}
