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

public class SoftBevelBorder extends BevelBorder {
  public SoftBevelBorder(int bevelType) { super(bevelType); }

  public SoftBevelBorder(int bevelType, Color highlight, Color shadow) { super(bevelType, highlight, shadow); }

  public SoftBevelBorder(int bevelType, Color highlightOuterColor, Color highlightInnerColor, Color shadowOuterColor,
      Color shadowInnerColor) {
    super(bevelType, highlightOuterColor, highlightInnerColor, shadowOuterColor, shadowInnerColor);
  }

  public Insets getBorderInsets(Component c, Insets insets) {
    insets.set(3, 3, 3, 3);
    return insets;
  }

  public boolean isBorderOpaque() { return false; }

  public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
    Color old = g.getColor();
    boolean raised = bevelType == RAISED;
    g.setColor(raised ? getHighlightOuterColor(c) : getShadowOuterColor(c));
    g.drawLine(x, y, x + width - 2, y);
    g.drawLine(x, y, x, y + height - 2);
    g.setColor(raised ? getShadowOuterColor(c) : getHighlightOuterColor(c));
    g.drawLine(x + 1, y + height - 1, x + width - 1, y + height - 1);
    g.drawLine(x + width - 1, y + 1, x + width - 1, y + height - 1);
    g.setColor(old);
  }
}
