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

  private void px(Graphics g, boolean horizontal, int l1, int t1, int l2, int t2) {
    if (horizontal) {
      g.fillRect(l1, t1, l2 - l1 + 1, t2 - t1 + 1);
    } else {
      g.fillRect(t1, l1, t2 - t1 + 1, l2 - l1 + 1);
    }
  }

  public void paint(Graphics g) {
    boolean isEnabled = getParent().isEnabled();
    Color arrowColor = isEnabled ? MetalLookAndFeel.getControlInfo() : MetalLookAndFeel.getControlDisabled();
    boolean isPressed = getModel().isPressed();
    boolean horizontal = direction == WEST || direction == EAST;
    boolean leading = direction == NORTH || direction == WEST;
    int len = horizontal ? getWidth() : getHeight();
    int thick = horizontal ? getHeight() : getWidth();
    g.setColor(isPressed ? MetalLookAndFeel.getControlShadow() : getBackground());
    g.fillRect(0, 0, getWidth(), getHeight());
    g.setColor(shadowColor);
    int last = thick - 2;
    if (leading) {
      px(g, horizontal, 0, 0, len - 1, 0);
      px(g, horizontal, 0, 0, 0, last);
      px(g, horizontal, 2, last, len - 1, last);
    } else {
      px(g, horizontal, 0, 0, len - 2, 0);
      px(g, horizontal, len - 2, 2, len - 2, last);
      px(g, horizontal, 0, last, len - 2, last);
    }
    if (!isPressed) {
      g.setColor(highlightColor);
      if (leading) {
        px(g, horizontal, 1, 1, len - 1, 1);
        px(g, horizontal, 1, 1, 1, last - 1);
        px(g, horizontal, 1, last + 1, len - 1, last + 1);
      } else {
        px(g, horizontal, 0, 1, len - 3, 1);
        px(g, horizontal, 0, 1, 0, last - 1);
        px(g, horizontal, len - 1, 1, len - 1, last + 1);
        px(g, horizontal, 0, last + 1, len - 1, last + 1);
      }
    }
    g.setColor(arrowColor);
    int size = (thick + 1) / 4;
    if (horizontal) {
      int tip = leading ? (len - size + 1) / 2 : (len - 1 - size) / 2 + size - 1;
      int top = (thick - 2 * size + 2) / 2;
      for (int k = 0; k < size; k++) {
        int l = leading ? tip + k : tip - k;
        px(g, true, l, top + size - 1 - k, l, top + size + k);
      }
    } else {
      int tip = leading ? (len - size + 1) / 2 : len / 2;
      int center = thick / 2;
      for (int k = 0; k < size; k++) {
        int l = leading ? tip + k : tip - k;
        int half = leading ? k : k + 1;
        px(g, false, l, center - half, l, center + half);
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
