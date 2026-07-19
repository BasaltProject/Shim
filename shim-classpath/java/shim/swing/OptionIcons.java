/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package shim.swing;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.Icon;
import javax.swing.plaf.UIResource;

public final class OptionIcons implements Icon, UIResource {
  public static final int kError = 0;
  public static final int kInformation = 1;
  public static final int kWarning = 2;
  public static final int kQuestion = 3;
  private final int kind;

  public OptionIcons(int kind) {
    this.kind = kind;
  }

  public int getIconWidth() { return 32; }

  public int getIconHeight() { return 32; }

  public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    try {
      g2.translate(x, y);
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      if (kind == kWarning) {
        g2.setColor(new Color(0xFFCC33));
        g2.fillPolygon(new int[] {16, 31, 1}, new int[] {2, 29, 29}, 3);
        g2.setColor(new Color(0x333333));
        g2.fillRect(15, 10, 3, 10);
        g2.fillRect(15, 22, 3, 3);
      } else {
        g2.setColor(kind == kError ? new Color(0xCC3333) : new Color(0x6382BF));
        g2.fillOval(1, 1, 30, 30);
        g2.setColor(Color.WHITE);
        if (kind == kError) {
          g2.setStroke(new BasicStroke(4f));
          g2.drawLine(10, 10, 22, 22);
          g2.drawLine(22, 10, 10, 22);
        } else {
          g2.setFont(new Font("Dialog", Font.BOLD, 22));
          String s = kind == kInformation ? "i" : "?";
          int w = g2.getFontMetrics().stringWidth(s);
          g2.drawString(s, 16 - w / 2, 24);
        }
      }
    } finally {
      g2.dispose();
    }
  }
}
