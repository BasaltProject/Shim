/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package javax.swing.colorchooser;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import javax.swing.JComponent;
import javax.swing.JColorChooser;
import javax.swing.UIManager;

class DefaultPreviewPanel extends JComponent {
  private static final int kSquare = 30;
  private static final int kTextGap = 5;

  public Dimension getPreferredSize() {
    String text = sampleText();
    Font font = getFont() == null ? UIManager.getFont("ColorChooser.font") : getFont();
    int w = kSquare * 3 + 2 * kTextGap;
    if (font != null) {
      FontMetrics fm = getFontMetrics(font);
      w = Math.max(w, fm.stringWidth(text) + 2 * kTextGap);
    }
    return new Dimension(Math.max(w, 310), kSquare * 2);
  }

  private String sampleText() {
    String s = UIManager.getString("ColorChooser.sampleText", getLocale());
    return s == null ? "Sample Text  Sample Text" : s;
  }

  private Color currentColor() {
    JColorChooser c = (JColorChooser) javax.swing.SwingUtilities.getAncestorOfClass(JColorChooser.class, this);
    return c == null ? getForeground() : c.getColor();
  }

  public void paintComponent(Graphics g) {
    Color color = currentColor();
    int w = getWidth();
    int h = getHeight();
    g.setColor(getBackground());
    g.fillRect(0, 0, w, h);
    int swatchW = kSquare * 2;
    g.setColor(color);
    g.fillRect(w - swatchW, 0, swatchW, h);
    Font font = getFont();
    if (font != null) {
      g.setFont(font);
    }
    FontMetrics fm = g.getFontMetrics();
    String text = sampleText();
    int baseline = (kSquare - fm.getHeight()) / 2 + fm.getAscent();
    g.setColor(color);
    g.drawString(text, kTextGap, baseline);
    g.fillRect(0, kSquare, w - swatchW, kSquare);
    g.setColor(getBackground());
    g.drawString(text, kTextGap, kSquare + baseline);
  }
}
