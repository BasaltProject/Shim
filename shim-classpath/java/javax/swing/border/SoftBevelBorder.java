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

  @java.beans.ConstructorProperties({"bevelType", "highlightOuterColor", "highlightInnerColor", "shadowOuterColor", "shadowInnerColor"})
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
    Color tlOuter = raised ? getHighlightOuterColor(c) : getShadowOuterColor(c);
    Color tlInner = raised ? getHighlightInnerColor(c) : getShadowInnerColor(c);
    Color brOuter = raised ? getShadowOuterColor(c) : getHighlightOuterColor(c);
    Color brInner = raised ? getShadowInnerColor(c) : getHighlightInnerColor(c);
    int w = width;
    int h = height;
    g.translate(x, y);
    g.setColor(tlOuter);
    g.drawLine(0, 0, w - 2, 0);
    g.drawLine(0, 1, 1, 1);
    g.drawLine(0, 2, 0, h - 3);
    g.setColor(tlInner);
    g.drawLine(w - 1, 0, w - 1, 0);
    g.drawLine(2, 1, w - 2, 1);
    g.drawLine(1, 2, 1, h - 2);
    g.drawLine(2, 2, 2, 2);
    g.drawLine(0, h - 2, 0, h - 1);
    g.setColor(brInner);
    g.drawLine(w - 2, h - 2, w - 2, h - 2);
    g.setColor(brOuter);
    g.drawLine(w - 1, 2, w - 1, h - 1);
    g.drawLine(2, h - 1, w - 1, h - 1);
    g.translate(-x, -y);
    g.setColor(old);
  }
}
