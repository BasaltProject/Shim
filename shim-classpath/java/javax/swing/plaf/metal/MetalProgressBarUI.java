/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.plaf.metal;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import javax.swing.JComponent;
import javax.swing.JProgressBar;

public class MetalProgressBarUI extends javax.swing.plaf.basic.BasicProgressBarUI {
  private static final Color kEdgeFill = new Color(0x6382BF);
  private static final Color kEdgeEmpty = new Color(0xB8CFE5);

  public MetalProgressBarUI() {
  }

  private void edges(Graphics g, Rectangle fill) {
    Insets b = progressBar.getInsets();
    int w = progressBar.getWidth() - b.left - b.right;
    int h = progressBar.getHeight() - b.top - b.bottom;
    if (w <= 0 || h <= 0) {
      return;
    }
    Color old = g.getColor();
    for (int x = 0; x < w; x++) {
      g.setColor(fill != null && fill.contains(b.left + x, b.top) ? kEdgeFill : kEdgeEmpty);
      g.fillRect(b.left + x, b.top, 1, 1);
    }
    for (int y = 1; y < h; y++) {
      g.setColor(fill != null && fill.contains(b.left, b.top + y) ? kEdgeFill : kEdgeEmpty);
      g.fillRect(b.left, b.top + y, 1, 1);
    }
    g.setColor(old);
  }

  public void paintIndeterminate(Graphics g, JComponent c) {
    super.paintIndeterminate(g, c);
    edges(g, null);
    if (boxRect != null) {
      Insets b = progressBar.getInsets();
      Color old = g.getColor();
      g.setColor(kEdgeFill);
      g.fillRect(boxRect.x - 1, b.top, boxRect.width + 1, 1);
      g.setColor(old);
    }
  }

  public void paintDeterminate(Graphics g, JComponent c) {
    super.paintDeterminate(g, c);
    Insets b = progressBar.getInsets();
    int w = progressBar.getWidth() - b.left - b.right;
    int h = progressBar.getHeight() - b.top - b.bottom;
    int full = getAmountFull(b, w, h);
    Rectangle fill;
    if (progressBar.getOrientation() == JProgressBar.HORIZONTAL) {
      fill = new Rectangle(b.left, b.top, full, h);
    } else {
      fill = new Rectangle(b.left, b.top + h - full, w, full);
    }
    edges(g, full > 0 ? fill : null);
  }

  public static javax.swing.plaf.ComponentUI createUI(JComponent c) {
    return new MetalProgressBarUI();
  }
}
