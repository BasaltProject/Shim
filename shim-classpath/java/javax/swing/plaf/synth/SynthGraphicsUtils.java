/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package javax.swing.plaf.synth;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicGraphicsUtils;

public class SynthGraphicsUtils {
  private final Rectangle paintIconR = new Rectangle();
  private final Rectangle paintTextR = new Rectangle();
  private final Rectangle paintViewR = new Rectangle();
  private Insets paintInsets = new Insets(0, 0, 0, 0);

  public SynthGraphicsUtils() { }

  public static void paintIcon(Icon icon, SynthContext context, Graphics g, int x, int y, int w, int h) {
    if (icon instanceof SynthIcon) {
      ((SynthIcon) icon).paintIcon(context, g, x, y, w, h);
    } else if (icon != null) {
      icon.paintIcon(context.getComponent(), g, x, y);
    }
  }

  public static int getIconWidth(Icon icon, SynthContext context) {
    if (icon == null) {
      return 0;
    }
    return icon instanceof SynthIcon ? ((SynthIcon) icon).getIconWidth(context) : icon.getIconWidth();
  }

  public static int getIconHeight(Icon icon, SynthContext context) {
    if (icon == null) {
      return 0;
    }
    return icon instanceof SynthIcon ? ((SynthIcon) icon).getIconHeight(context) : icon.getIconHeight();
  }

  public String layoutText(SynthContext ss, FontMetrics fm, String text, Icon icon, int hAlign, int vAlign,
      int hTextPosition, int vTextPosition, Rectangle viewR, Rectangle iconR, Rectangle textR, int iconTextGap) {
    if (icon instanceof SynthIcon) {
      SynthIcon si = (SynthIcon) icon;
      int iconWidth = si.getIconWidth(ss);
      int iconHeight = si.getIconHeight(ss);
      return layoutWithSize(ss, fm, text, iconWidth, iconHeight, hAlign, vAlign, hTextPosition, vTextPosition,
          viewR, iconR, textR, iconTextGap);
    }
    return SwingUtilities.layoutCompoundLabel(ss.getComponent(), fm, text, icon, vAlign, hAlign, vTextPosition,
        hTextPosition, viewR, iconR, textR, iconTextGap);
  }

  private String layoutWithSize(SynthContext ss, FontMetrics fm, String text, final int w, final int h, int hAlign,
      int vAlign, int hTextPosition, int vTextPosition, Rectangle viewR, Rectangle iconR, Rectangle textR,
      int iconTextGap) {
    Icon sized = new Icon() {
      public void paintIcon(java.awt.Component c, Graphics g, int x, int y) { }

      public int getIconWidth() {
        return w;
      }

      public int getIconHeight() {
        return h;
      }
    };
    return SwingUtilities.layoutCompoundLabel(ss.getComponent(), fm, text, sized, vAlign, hAlign, vTextPosition,
        hTextPosition, viewR, iconR, textR, iconTextGap);
  }

  public int computeStringWidth(SynthContext ss, Font font, FontMetrics metrics, String text) {
    return metrics.stringWidth(text);
  }

  public Dimension getMinimumSize(SynthContext ss, Font font, String text, Icon icon, int hAlign, int vAlign,
      int hTextPosition, int vTextPosition, int iconTextGap, int mnemonicIndex) {
    return getPreferredSize(ss, font, text, icon, hAlign, vAlign, hTextPosition, vTextPosition, iconTextGap,
        mnemonicIndex);
  }

  public Dimension getMaximumSize(SynthContext ss, Font font, String text, Icon icon, int hAlign, int vAlign,
      int hTextPosition, int vTextPosition, int iconTextGap, int mnemonicIndex) {
    return getPreferredSize(ss, font, text, icon, hAlign, vAlign, hTextPosition, vTextPosition, iconTextGap,
        mnemonicIndex);
  }

  public int getMaximumCharHeight(SynthContext context) {
    FontMetrics fm = context.getComponent().getFontMetrics(context.getStyle().getFont(context));
    return fm.getAscent() + fm.getDescent();
  }

  public Dimension getPreferredSize(SynthContext ss, Font font, String text, Icon icon, int hAlign, int vAlign,
      int hTextPosition, int vTextPosition, int iconTextGap, int mnemonicIndex) {
    JComponent c = ss.getComponent();
    Insets insets = c.getInsets(paintInsets);
    int dx = insets.left + insets.right;
    int dy = insets.top + insets.bottom;
    if (icon == null && (text == null || font == null)) {
      return new Dimension(dx, dy);
    } else if (text == null || (icon != null && font == null)) {
      return new Dimension(getIconWidth(icon, ss) + dx, getIconHeight(icon, ss) + dy);
    }
    FontMetrics fm = c.getFontMetrics(font);
    paintViewR.x = paintViewR.y = 0;
    paintViewR.width = paintViewR.height = Short.MAX_VALUE;
    paintIconR.x = paintIconR.y = paintIconR.width = paintIconR.height = 0;
    paintTextR.x = paintTextR.y = paintTextR.width = paintTextR.height = 0;
    layoutText(ss, fm, text, icon, hAlign, vAlign, hTextPosition, vTextPosition, paintViewR, paintIconR,
        paintTextR, iconTextGap);
    int x1 = Math.min(paintIconR.x, paintTextR.x);
    int x2 = Math.max(paintIconR.x + paintIconR.width, paintTextR.x + paintTextR.width);
    int y1 = Math.min(paintIconR.y, paintTextR.y);
    int y2 = Math.max(paintIconR.y + paintIconR.height, paintTextR.y + paintTextR.height);
    return new Dimension(x2 - x1 + dx, y2 - y1 + dy);
  }

  public void paintText(SynthContext ss, Graphics g, String text, Rectangle bounds, int mnemonicIndex) {
    paintText(ss, g, text, bounds.x, bounds.y, mnemonicIndex);
  }

  public void paintText(SynthContext ss, Graphics g, String text, int x, int y, int mnemonicIndex) {
    if (text != null) {
      Color color = g.getColor();
      Font font = ss.getStyle().getFont(ss);
      FontMetrics fm = ss.getComponent().getFontMetrics(font);
      g.setColor(ss.getStyle().getColor(ss, ColorType.TEXT_FOREGROUND));
      g.setFont(font);
      y += fm.getAscent();
      BasicGraphicsUtils.drawStringUnderlineCharAt(g, text, mnemonicIndex, x, y);
      g.setColor(color);
    }
  }

  public void paintText(SynthContext ss, Graphics g, String text, Icon icon, int hAlign, int vAlign,
      int hTextPosition, int vTextPosition, int iconTextGap, int mnemonicIndex, int textOffset) {
    if (icon == null && text == null) {
      return;
    }
    JComponent c = ss.getComponent();
    FontMetrics fm = g.getFontMetrics();
    Insets insets = c.getInsets(paintInsets);
    paintViewR.x = insets.left;
    paintViewR.y = insets.top;
    paintViewR.width = c.getWidth() - (insets.left + insets.right);
    paintViewR.height = c.getHeight() - (insets.top + insets.bottom);
    paintIconR.x = paintIconR.y = paintIconR.width = paintIconR.height = 0;
    paintTextR.x = paintTextR.y = paintTextR.width = paintTextR.height = 0;
    String clippedText = layoutText(ss, fm, text, icon, hAlign, vAlign, hTextPosition, vTextPosition, paintViewR,
        paintIconR, paintTextR, iconTextGap);
    if (icon != null) {
      Color color = g.getColor();
      paintIcon(icon, ss, g, paintIconR.x + textOffset, paintIconR.y + textOffset, paintIconR.width,
          paintIconR.height);
      g.setColor(color);
    }
    if (text != null) {
      paintText(ss, g, clippedText, paintTextR.x + textOffset, paintTextR.y + textOffset, mnemonicIndex);
    }
  }

  public void drawLine(SynthContext context, Object paintKey, Graphics g, int x1, int y1, int x2, int y2) {
    drawLine(context, paintKey, g, x1, y1, x2, y2, null);
  }

  public void drawLine(SynthContext context, Object paintKey, Graphics g, int x1, int y1, int x2, int y2,
      Object styleKey) {
    g.setColor(context.getStyle().getColor(context, ColorType.FOREGROUND));
    g.drawLine(x1, y1, x2, y2);
  }
}
