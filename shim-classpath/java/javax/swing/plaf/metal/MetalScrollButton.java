/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package javax.swing.plaf.metal;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicArrowButton;

public class MetalScrollButton extends BasicArrowButton {
  private static Color shadowColor;
  private static Color highlightColor;
  private boolean isFreeStanding = false;
  private int buttonWidth;

  public MetalScrollButton(int direction, int width, boolean freeStanding) {
    super(direction);
    shadowColor = UIManager.getColor("ScrollBar.darkShadow");
    highlightColor = UIManager.getColor("ScrollBar.highlight");
    buttonWidth = width;
    isFreeStanding = freeStanding;
  }

  public void setFreeStanding(boolean freeStanding) {
    isFreeStanding = freeStanding;
  }

  public void paint(Graphics g) {
    boolean leftToRight = getComponentOrientation().isLeftToRight();
    boolean isEnabled = getParent().isEnabled();
    Color arrowColor = isEnabled ? MetalLookAndFeel.getControlInfo() : MetalLookAndFeel.getControlDisabled();
    boolean isPressed = getModel().isPressed();
    int width = getWidth();
    int height = getHeight();
    int w = width;
    int h = height;
    int arrowHeight = (height + 1) / 4;
    int arrowWidth = (height + 1) / 2;
    if (isPressed) {
      g.setColor(MetalLookAndFeel.getControlShadow());
    } else {
      g.setColor(getBackground());
    }
    g.fillRect(0, 0, width, height);
    g.setColor(shadowColor);
    boolean leading = direction == NORTH || direction == WEST;
    if (direction == NORTH || direction == SOUTH) {
      g.drawLine(0, leading ? 0 : 0, w - 1, 0);
      g.drawLine(0, 0, 0, h - 1);
      g.drawLine(w - 1, 0, w - 1, h - 1);
      if (!leading) {
        g.drawLine(0, h - 1, w - 1, h - 1);
      }
    } else {
      g.drawLine(0, 0, 0, h - 1);
      g.drawLine(0, 0, w - 1, 0);
      g.drawLine(0, h - 1, w - 1, h - 1);
      if (!leading) {
        g.drawLine(w - 1, 0, w - 1, h - 1);
      }
    }
    if (!isPressed) {
      g.setColor(highlightColor);
      g.drawLine(1, 1, w - 2, 1);
      g.drawLine(1, 1, 1, h - 2);
    }
    g.setColor(arrowColor);
    int cx = w / 2;
    int cy = h / 2;
    int half = Math.max(2, w / 4);
    switch (direction) {
      case NORTH:
        for (int i = 0; i <= half; i++) {
          g.drawLine(cx - i, cy - 1 + i, cx + i, cy - 1 + i);
        }
        break;
      case SOUTH:
        for (int i = 0; i <= half; i++) {
          g.drawLine(cx - i, cy + 1 - i, cx + i, cy + 1 - i);
        }
        break;
      case WEST:
        for (int i = 0; i <= half; i++) {
          g.drawLine(cx - 1 + i, cy - i, cx - 1 + i, cy + i);
        }
        break;
      case EAST:
      default:
        for (int i = 0; i <= half; i++) {
          g.drawLine(cx + 1 - i, cy - i, cx + 1 - i, cy + i);
        }
    }
  }

  public Dimension getPreferredSize() {
    int lessen = direction == NORTH || direction == WEST ? 2 : 1;
    if (direction == NORTH || direction == SOUTH) {
      return new Dimension(buttonWidth, buttonWidth - lessen);
    }
    return new Dimension(buttonWidth - lessen, buttonWidth);
  }

  public Dimension getMinimumSize() {
    return getPreferredSize();
  }

  public Dimension getMaximumSize() {
    return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
  }

  public int getButtonWidth() {
    return buttonWidth;
  }
}
