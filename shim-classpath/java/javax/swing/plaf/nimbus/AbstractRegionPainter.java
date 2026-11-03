/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package javax.swing.plaf.nimbus;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import javax.swing.JComponent;
import javax.swing.Painter;
import javax.swing.UIManager;
import javax.swing.plaf.UIResource;

public abstract class AbstractRegionPainter implements Painter<JComponent> {
  private int width;
  private int height;

  protected AbstractRegionPainter() { }

  public final void paint(Graphics2D g, JComponent c, int w, int h) {
    if (w <= 0 || h <= 0) {
      return;
    }
    width = w;
    height = h;
    Graphics2D g2 = (Graphics2D) g.create();
    try {
      configureGraphics(g2);
      doPaint(g2, c, w, h, getExtendedCacheKeys(c));
    } finally {
      g2.dispose();
    }
  }

  protected abstract void doPaint(Graphics2D g, JComponent c, int width, int height, Object[] extendedCacheKeys);

  protected Object[] getExtendedCacheKeys(JComponent c) {
    return null;
  }

  protected void configureGraphics(Graphics2D g) {
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
  }

  protected final float decodeX(float x) {
    if (x >= 0 && x <= 1) {
      return 0;
    } else if (x > 1 && x < 2) {
      return (x - 1) * width;
    }
    return width + (x - 2) * 0;
  }

  protected final float decodeY(float y) {
    if (y >= 0 && y <= 1) {
      return 0;
    } else if (y > 1 && y < 2) {
      return (y - 1) * height;
    }
    return height + (y - 2) * 0;
  }

  protected final float decodeAnchorX(float x, float dx) {
    return decodeX(x) + dx;
  }

  protected final float decodeAnchorY(float y, float dy) {
    return decodeY(y) + dy;
  }

  protected final Color decodeColor(String key, float hOffset, float sOffset, float bOffset, int aOffset) {
    if (UIManager.getLookAndFeel() instanceof NimbusLookAndFeel) {
      return ((NimbusLookAndFeel) UIManager.getLookAndFeel()).getDerivedColor(key, hOffset, sOffset, bOffset,
          aOffset, true);
    }
    return Color.getHSBColor(hOffset, sOffset, bOffset);
  }

  protected final Color decodeColor(Color color1, Color color2, float midPoint) {
    return new Color(NimbusLookAndFeel.deriveARGB(color1, color2, midPoint), true);
  }

  protected final LinearGradientPaint decodeGradient(float x1, float y1, float x2, float y2, float[] midpoints,
      Color[] colors) {
    if (x1 == x2 && y1 == y2) {
      y2 += .00001f;
    }
    return new LinearGradientPaint(decodeX(x1), decodeY(y1), decodeX(x2), decodeY(y2), midpoints, colors);
  }

  protected final RadialGradientPaint decodeRadialGradient(float x, float y, float r, float[] midpoints,
      Color[] colors) {
    if (r == 0f) {
      r = .00001f;
    }
    return new RadialGradientPaint(decodeX(x), decodeY(y), r, midpoints, colors);
  }

  protected final Color getComponentColor(JComponent c, String property, Color defaultColor, float saturationOffset,
      float brightnessOffset, int alphaOffset) {
    Color color = null;
    if (c != null) {
      if ("background".equals(property)) {
        color = c.getBackground();
      } else if ("foreground".equals(property)) {
        color = c.getForeground();
      }
    }
    if (color == null || color instanceof UIResource) {
      return defaultColor;
    }
    float[] tmp = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
    tmp[1] = Math.max(0f, Math.min(1f, tmp[1] + saturationOffset));
    tmp[2] = Math.max(0f, Math.min(1f, tmp[2] + brightnessOffset));
    int alpha = Math.max(0, Math.min(255, color.getAlpha() + alphaOffset));
    return new Color((Color.HSBtoRGB(tmp[0], tmp[1], tmp[2]) & 0xFFFFFF) | (alpha << 24), true);
  }
}
