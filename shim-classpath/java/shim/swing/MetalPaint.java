/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package shim.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.util.List;
import javax.swing.JToolBar;
import javax.swing.UIManager;

public final class MetalPaint {
  private MetalPaint() { }

  public static boolean isToolBarButton(Component c) {
    return c.getParent() instanceof JToolBar;
  }

  public static Color lastColor(String key, Color fallback) {
    Object value = UIManager.get(key);
    if (value instanceof List && ((List<?>) value).size() == 5) {
      return (Color) ((List<?>) value).get(4);
    }
    return fallback;
  }

  public static Color gradientColor(String key, int i, int span) {
    List<?> l = (List<?>) UIManager.get(key);
    float ratio = ((Number) l.get(0)).floatValue();
    Color c1 = (Color) l.get(2);
    Color c2 = (Color) l.get(3);
    Color c3 = (Color) l.get(4);
    int mid = (int) (span * ratio);
    int mid2 = ratio < 1f ? mid * 2 : mid;
    Color from = c1;
    Color to = c3;
    int start = mid2;
    int end = span;
    if (ratio >= 1f) {
      to = c2;
      start = 0;
    } else if (i < mid) {
      to = c2;
      start = 0;
      end = mid;
    } else if (i < mid2) {
      from = c2;
      to = c1;
      start = mid;
      end = mid2;
    }
    return lerp(from, to, end == start ? 0 : (double) (i - start) / (end - start));
  }

  public static boolean drawGradient(Component c, Graphics g, String key, int x, int y, int w, int h,
      boolean vertical) {
    Object value = UIManager.get(key);
    if (!(value instanceof List) || ((List<?>) value).size() != 5) {
      return false;
    }
    int span = vertical ? h : w;
    for (int i = 0; i < span; i++) {
      g.setColor(gradientColor(key, i, span));
      if (vertical) {
        g.fillRect(x, y + i, w, 1);
      } else {
        g.fillRect(x + i, y, 1, h);
      }
    }
    return true;
  }

  private static Color lerp(Color a, Color b, double t) {
    int idx = (int) (t * 256);
    return new Color(a.getRed() + ((b.getRed() - a.getRed()) * idx >> 8),
        a.getGreen() + ((b.getGreen() - a.getGreen()) * idx >> 8),
        a.getBlue() + ((b.getBlue() - a.getBlue()) * idx >> 8));
  }
}
