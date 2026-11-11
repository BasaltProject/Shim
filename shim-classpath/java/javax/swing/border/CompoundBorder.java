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

public class CompoundBorder extends AbstractBorder {
  protected Border outsideBorder;
  protected Border insideBorder;

  public CompoundBorder() { }

  @java.beans.ConstructorProperties({"outsideBorder", "insideBorder"})
  public CompoundBorder(Border outsideBorder, Border insideBorder) {
    this.outsideBorder = outsideBorder;
    this.insideBorder = insideBorder;
  }

  public boolean isBorderOpaque() {
    return (outsideBorder == null || outsideBorder.isBorderOpaque())
        && (insideBorder == null || insideBorder.isBorderOpaque());
  }

  public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
    int px = x;
    int py = y;
    int pw = width;
    int ph = height;
    if (outsideBorder != null) {
      outsideBorder.paintBorder(c, g, px, py, pw, ph);
      Insets nextInsets = outsideBorder.getBorderInsets(c);
      px += nextInsets.left;
      py += nextInsets.top;
      pw = pw - nextInsets.right - nextInsets.left;
      ph = ph - nextInsets.bottom - nextInsets.top;
    }
    if (insideBorder != null) {
      insideBorder.paintBorder(c, g, px, py, pw, ph);
    }
  }

  public Insets getBorderInsets(Component c, Insets insets) {
    insets.top = insets.left = insets.right = insets.bottom = 0;
    if (outsideBorder != null) {
      Insets nextInsets = outsideBorder.getBorderInsets(c);
      insets.top += nextInsets.top;
      insets.left += nextInsets.left;
      insets.right += nextInsets.right;
      insets.bottom += nextInsets.bottom;
    }
    if (insideBorder != null) {
      Insets nextInsets = insideBorder.getBorderInsets(c);
      insets.top += nextInsets.top;
      insets.left += nextInsets.left;
      insets.right += nextInsets.right;
      insets.bottom += nextInsets.bottom;
    }
    return insets;
  }

  public Border getOutsideBorder() { return outsideBorder; }

  public Border getInsideBorder() { return insideBorder; }
}
