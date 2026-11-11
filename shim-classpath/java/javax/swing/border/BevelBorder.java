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

public class BevelBorder extends AbstractBorder {
  public static final int RAISED = 0;
  public static final int LOWERED = 1;

  protected int bevelType;
  protected Color highlightOuter;
  protected Color highlightInner;
  protected Color shadowInner;
  protected Color shadowOuter;

  public BevelBorder(int bevelType) { this.bevelType = bevelType; }

  public BevelBorder(int bevelType, Color highlight, Color shadow) {
    this(bevelType, highlight.brighter(), highlight, shadow, shadow.brighter());
  }

  @java.beans.ConstructorProperties({"bevelType", "highlightOuterColor", "highlightInnerColor", "shadowOuterColor", "shadowInnerColor"})
  public BevelBorder(int bevelType, Color highlightOuterColor, Color highlightInnerColor, Color shadowOuterColor,
      Color shadowInnerColor) {
    this(bevelType);
    this.highlightOuter = highlightOuterColor;
    this.highlightInner = highlightInnerColor;
    this.shadowOuter = shadowOuterColor;
    this.shadowInner = shadowInnerColor;
  }

  public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
    if (bevelType == RAISED) {
      paintRaisedBevel(c, g, x, y, width, height);
    } else if (bevelType == LOWERED) {
      paintLoweredBevel(c, g, x, y, width, height);
    }
  }

  public Insets getBorderInsets(Component c, Insets insets) {
    insets.set(2, 2, 2, 2);
    return insets;
  }

  public Color getHighlightOuterColor(Component c) {
    Color highlight = getHighlightOuterColor();
    return highlight != null ? highlight : c.getBackground().brighter().brighter();
  }

  public Color getHighlightInnerColor(Component c) {
    Color highlight = getHighlightInnerColor();
    return highlight != null ? highlight : c.getBackground().brighter();
  }

  public Color getShadowInnerColor(Component c) {
    Color shadow = getShadowInnerColor();
    return shadow != null ? shadow : c.getBackground().darker();
  }

  public Color getShadowOuterColor(Component c) {
    Color shadow = getShadowOuterColor();
    return shadow != null ? shadow : c.getBackground().darker().darker();
  }

  public Color getHighlightOuterColor() { return highlightOuter; }

  public Color getHighlightInnerColor() { return highlightInner; }

  public Color getShadowInnerColor() { return shadowInner; }

  public Color getShadowOuterColor() { return shadowOuter; }

  public int getBevelType() { return bevelType; }

  public boolean isBorderOpaque() { return true; }

  protected void paintRaisedBevel(Component c, Graphics g, int x, int y, int width, int height) {
    bevel(g, x, y, width, height, getHighlightOuterColor(c), getHighlightInnerColor(c), getShadowOuterColor(c),
        getShadowInnerColor(c));
  }

  protected void paintLoweredBevel(Component c, Graphics g, int x, int y, int width, int height) {
    Color old = g.getColor();
    g.translate(x, y);
    g.setColor(getShadowInnerColor(c));
    g.drawLine(0, 0, 0, height - 1);
    g.drawLine(1, 0, width - 1, 0);
    g.setColor(getShadowOuterColor(c));
    g.drawLine(1, 1, 1, height - 2);
    g.drawLine(2, 1, width - 2, 1);
    g.setColor(getHighlightOuterColor(c));
    g.drawLine(1, height - 1, width - 1, height - 1);
    g.drawLine(width - 1, 1, width - 1, height - 2);
    g.setColor(getHighlightInnerColor(c));
    g.drawLine(2, height - 2, width - 2, height - 2);
    g.drawLine(width - 2, 2, width - 2, height - 3);
    g.translate(-x, -y);
    g.setColor(old);
  }

  private void bevel(Graphics g, int x, int y, int w, int h, Color tlOuter, Color tlInner, Color brOuter, Color brInner) {
    Color old = g.getColor();
    int h1 = h - 1;
    int w1 = w - 1;
    g.setColor(tlOuter);
    g.drawLine(x, y, x, y + h1 - 1);
    g.drawLine(x + 1, y, x + w1 - 1, y);
    g.setColor(tlInner);
    g.drawLine(x + 1, y + 1, x + 1, y + h1 - 2);
    g.drawLine(x + 2, y + 1, x + w1 - 2, y + 1);
    g.setColor(brOuter);
    g.drawLine(x, y + h1, x + w1, y + h1);
    g.drawLine(x + w1, y, x + w1, y + h1 - 1);
    g.setColor(brInner);
    g.drawLine(x + 1, y + h1 - 1, x + w1 - 1, y + h1 - 1);
    g.drawLine(x + w1 - 1, y + 1, x + w1 - 1, y + h1 - 2);
    g.setColor(old);
  }
}
