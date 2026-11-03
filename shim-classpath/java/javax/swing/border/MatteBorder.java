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
import javax.swing.Icon;

public class MatteBorder extends EmptyBorder {
  protected Color color;
  protected Icon tileIcon;

  public MatteBorder(int top, int left, int bottom, int right, Color matteColor) {
    super(top, left, bottom, right);
    this.color = matteColor;
  }

  public MatteBorder(Insets borderInsets, Color matteColor) {
    super(borderInsets);
    this.color = matteColor;
  }

  public MatteBorder(int top, int left, int bottom, int right, Icon tileIcon) {
    super(top, left, bottom, right);
    this.tileIcon = tileIcon;
  }

  public MatteBorder(Insets borderInsets, Icon tileIcon) {
    super(borderInsets);
    this.tileIcon = tileIcon;
  }

  public MatteBorder(Icon tileIcon) {
    this(-1, -1, -1, -1, tileIcon);
  }

  public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
    Insets insets = getBorderInsets(c);
    Color oldColor = g.getColor();
    g.translate(x, y);
    if (tileIcon != null) {
      color = (tileIcon.getIconWidth() == -1) ? Color.gray : null;
    }
    if (color != null) {
      g.setColor(color);
      g.fillRect(0, 0, width - insets.right, insets.top);
      g.fillRect(0, insets.top, insets.left, height - insets.top);
      g.fillRect(insets.left, height - insets.bottom, width - insets.left, insets.bottom);
      g.fillRect(width - insets.right, 0, insets.right, height - insets.bottom);
    } else if (tileIcon != null) {
      int tileW = tileIcon.getIconWidth();
      int tileH = tileIcon.getIconHeight();
      for (int ty = 0; ty < height; ty += tileH) {
        for (int tx = 0; tx < width; tx += tileW) {
          boolean inside = tx >= insets.left && tx < width - insets.right && ty >= insets.top && ty < height - insets.bottom;
          if (!inside) {
            tileIcon.paintIcon(c, g, tx, ty);
          }
        }
      }
    }
    g.translate(-x, -y);
    g.setColor(oldColor);
  }

  public Insets getBorderInsets(Component c, Insets insets) { return computeInsets(insets); }

  public Insets getBorderInsets() { return computeInsets(new Insets(0, 0, 0, 0)); }

  private Insets computeInsets(Insets insets) {
    if (tileIcon != null && top == -1 && bottom == -1 && left == -1 && right == -1) {
      int w = tileIcon.getIconWidth();
      int h = tileIcon.getIconHeight();
      insets.top = h;
      insets.right = w;
      insets.bottom = h;
      insets.left = w;
    } else {
      insets.left = left;
      insets.top = top;
      insets.right = right;
      insets.bottom = bottom;
    }
    return insets;
  }

  public Color getMatteColor() { return color; }

  public Icon getTileIcon() { return tileIcon; }

  public boolean isBorderOpaque() { return color != null; }
}
