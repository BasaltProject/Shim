/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.plaf.basic;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

public class BasicGraphicsUtils {
  public BasicGraphicsUtils() {
  }
  public static void drawBezel(Graphics g, int x, int y, int w, int h, boolean isPressed, boolean isDefault, Color shadow, Color darkShadow, Color highlight, Color lightHighlight) {
    Color oldColor = g.getColor();
    g.translate(x, y);
    if (isPressed && isDefault) {
      g.setColor(darkShadow);
      g.drawRect(0, 0, w - 1, h - 1);
      g.setColor(shadow);
      g.drawRect(1, 1, w - 3, h - 3);
    } else if (isPressed) {
      drawLoweredBezel(g, x, y, w, h, shadow, darkShadow, highlight, lightHighlight);
    } else if (isDefault) {
      g.setColor(darkShadow);
      g.drawRect(0, 0, w - 1, h - 1);
      g.setColor(lightHighlight);
      g.drawLine(1, 1, 1, h - 3);
      g.drawLine(2, 1, w - 3, 1);
      g.setColor(highlight);
      g.drawLine(2, 2, 2, h - 4);
      g.drawLine(3, 2, w - 4, 2);
      g.setColor(shadow);
      g.drawLine(2, h - 3, w - 3, h - 3);
      g.drawLine(w - 3, 2, w - 3, h - 4);
      g.setColor(darkShadow);
      g.drawLine(1, h - 2, w - 2, h - 2);
      g.drawLine(w - 2, h - 2, w - 2, 1);
    } else {
      g.setColor(lightHighlight);
      g.drawLine(0, 0, 0, h - 1);
      g.drawLine(1, 0, w - 2, 0);
      g.setColor(highlight);
      g.drawLine(1, 1, 1, h - 3);
      g.drawLine(2, 1, w - 3, 1);
      g.setColor(shadow);
      g.drawLine(1, h - 2, w - 2, h - 2);
      g.drawLine(w - 2, 1, w - 2, h - 3);
      g.setColor(darkShadow);
      g.drawLine(0, h - 1, w - 1, h - 1);
      g.drawLine(w - 1, h - 1, w - 1, 0);
    }
    g.translate(-x, -y);
    g.setColor(oldColor);
  }
  public static void drawLoweredBezel(Graphics g, int x, int y, int w, int h, Color shadow, Color darkShadow, Color highlight, Color lightHighlight) {
    g.translate(x, y);
    g.setColor(darkShadow);
    g.drawLine(0, 0, 0, h - 1);
    g.drawLine(1, 0, w - 2, 0);
    g.setColor(shadow);
    g.drawLine(1, 1, 1, h - 2);
    g.drawLine(1, 1, w - 3, 1);
    g.setColor(lightHighlight);
    g.drawLine(0, h - 1, w - 1, h - 1);
    g.drawLine(w - 1, h - 1, w - 1, 0);
    g.setColor(highlight);
    g.drawLine(1, h - 2, w - 2, h - 2);
    g.drawLine(w - 2, h - 2, w - 2, 1);
    g.translate(-x, -y);
  }
  public static void drawEtchedRect(Graphics g, int x, int y, int w, int h, Color shadow, Color darkShadow, Color highlight, Color lightHighlight) {
    Color oldColor = g.getColor();
    g.translate(x, y);
    g.setColor(shadow);
    g.drawLine(0, 0, w - 1, 0);
    g.drawLine(0, 1, 0, h - 2);
    g.setColor(darkShadow);
    g.drawLine(1, 1, w - 2, 1);
    g.drawLine(1, 2, 1, h - 2);
    g.setColor(lightHighlight);
    g.drawLine(w - 1, 0, w - 1, h - 1);
    g.drawLine(0, h - 1, w - 1, h - 1);
    g.setColor(highlight);
    g.drawLine(w - 2, 1, w - 2, h - 3);
    g.drawLine(2, h - 2, w - 2, h - 2);
    g.translate(-x, -y);
    g.setColor(oldColor);
  }
  public static void drawStringUnderlineCharAt(Graphics g, String text, int underlinedIndex, int x, int y) {
    g.drawString(text, x, y);
    if (underlinedIndex >= 0 && underlinedIndex < text.length()) {
      FontMetrics fm = g.getFontMetrics();
      int underlineRectX = x + fm.stringWidth(text.substring(0, underlinedIndex));
      int underlineRectY = y;
      int underlineRectWidth = fm.charWidth(text.charAt(underlinedIndex));
      int underlineRectHeight = 1;
      g.fillRect(underlineRectX, underlineRectY + fm.getDescent() - 1, underlineRectWidth, underlineRectHeight);
    }
  }
  public static void drawStringUnderlineCharAt(JComponent c, Graphics2D g, String string, int underlinedIndex, float x, float y) {
    g.drawString(string, x, y);
    if (underlinedIndex >= 0 && underlinedIndex < string.length()) {
      FontMetrics fm = g.getFontMetrics();
      int underlineRectX = (int) (x + fm.stringWidth(string.substring(0, underlinedIndex)));
      int underlineRectY = (int) y;
      int underlineRectWidth = fm.charWidth(string.charAt(underlinedIndex));
      g.fillRect(underlineRectX, underlineRectY + fm.getDescent() - 1, underlineRectWidth, 1);
    }
  }
  public static Dimension getPreferredButtonSize(AbstractButton b, int textIconGap) {
    if (b.getComponentCount() > 0) {
      return null;
    }
    Icon icon = b.getIcon();
    String text = b.getText();
    Font font = b.getFont();
    FontMetrics fm = b.getFontMetrics(font);
    Rectangle iconR = new Rectangle();
    Rectangle textR = new Rectangle();
    Rectangle viewR = new Rectangle(Short.MAX_VALUE, Short.MAX_VALUE);
    SwingUtilities.layoutCompoundLabel(b, fm, text, icon, b.getVerticalAlignment(), b.getHorizontalAlignment(),
        b.getVerticalTextPosition(), b.getHorizontalTextPosition(), viewR, iconR, textR, (text == null ? 0 : textIconGap));
    Rectangle r = iconR.union(textR);
    Insets insets = b.getInsets();
    r.width += insets.left + insets.right;
    r.height += insets.top + insets.bottom;
    return r.getSize();
  }
  public static Insets getEtchedInsets() {
    return new Insets(2, 2, 2, 2);
  }
  public static void drawGroove(Graphics g, int x, int y, int w, int h, Color shadow, Color highlight) {
    Color oldColor = g.getColor();
    g.translate(x, y);
    g.setColor(shadow);
    g.drawRect(0, 0, w - 2, h - 2);
    g.setColor(highlight);
    g.drawLine(1, h - 3, 1, 1);
    g.drawLine(1, 1, w - 3, 1);
    g.drawLine(0, h - 1, w - 1, h - 1);
    g.drawLine(w - 1, h - 1, w - 1, 0);
    g.translate(-x, -y);
    g.setColor(oldColor);
  }
  public static Insets getGrooveInsets() {
    return new Insets(2, 2, 2, 2);
  }
  public static void drawDashedRect(Graphics g, int x, int y, int width, int height) {
    int vx;
    int vy;
    for (vx = x; vx < (x + width); vx += 2) {
      g.fillRect(vx, y, 1, 1);
      g.fillRect(vx, y + height - 1, 1, 1);
    }
    for (vy = y; vy < (y + height); vy += 2) {
      g.fillRect(x, vy, 1, 1);
      g.fillRect(x + width - 1, vy, 1, 1);
    }
  }
  public static String getClippedString(JComponent c, FontMetrics fm, String string, int availTextWidth) {
    if (string == null || string.equals("")) {
      return "";
    }
    int textWidth = fm.stringWidth(string);
    if (textWidth > availTextWidth) {
      String clipString = "...";
      int totalWidth = fm.stringWidth(clipString);
      int nChars;
      for (nChars = 0; nChars < string.length(); nChars++) {
        totalWidth += fm.charWidth(string.charAt(nChars));
        if (totalWidth > availTextWidth) {
          break;
        }
      }
      string = string.substring(0, nChars) + clipString;
    }
    return string;
  }
  public static float getStringWidth(JComponent c, FontMetrics fm, String string) {
    return fm.stringWidth(string);
  }
  public static void drawString(Graphics g, String text, int underlinedChar, int x, int y) {
    int index = -1;
    if (underlinedChar != '\0') {
      char uc = Character.toUpperCase((char) underlinedChar);
      char lc = Character.toLowerCase((char) underlinedChar);
      int uci = text.indexOf(uc);
      int lci = text.indexOf(lc);
      if (uci == -1) {
        index = lci;
      } else if (lci == -1) {
        index = uci;
      } else {
        index = (lci < uci) ? lci : uci;
      }
    }
    drawStringUnderlineCharAt(g, text, index, x, y);
  }
  public static void drawString(JComponent c, Graphics2D g, String string, float x, float y) {
    g.drawString(string, x, y);
  }
}
