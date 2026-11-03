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

public class EmptyBorder extends AbstractBorder implements Serializable {
  protected int left;
  protected int right;
  protected int top;
  protected int bottom;

  public EmptyBorder(int top, int left, int bottom, int right) {
    this.top = top;
    this.right = right;
    this.bottom = bottom;
    this.left = left;
  }

  public EmptyBorder(Insets borderInsets) {
    this(borderInsets.top, borderInsets.left, borderInsets.bottom, borderInsets.right);
  }

  public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) { }

  public Insets getBorderInsets(Component c, Insets insets) {
    insets.left = left;
    insets.top = top;
    insets.right = right;
    insets.bottom = bottom;
    return insets;
  }

  public Insets getBorderInsets() { return new Insets(top, left, bottom, right); }

  public boolean isBorderOpaque() { return true; }
}
