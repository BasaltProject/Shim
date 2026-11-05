/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.awt;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.font.FontRenderContext;

public final class SimpleFontMetrics extends FontMetrics {
  private final FontRenderContext frc;
  private final FontFace face;
  private final float size;

  public SimpleFontMetrics(Font font, FontRenderContext frc) {
    super(font);
    this.frc = frc;
    this.face = Fonts.face(font.getName(), font.getStyle());
    this.size = font.getSize2D();
  }

  public FontRenderContext getFontRenderContext() { return frc; }

  public int getAscent() { return (int) Math.ceil(face.ascent() * size - 0.05); }

  public int getDescent() { return (int) Math.ceil(face.descent() * size - 0.05); }

  public int getLeading() { return (int) Math.ceil(face.leading() * size - 0.05); }

  public int getMaxAscent() { return getAscent(); }

  public int getMaxDescent() { return getDescent(); }

  public int getMaxAdvance() { return face.maxAdvancePixels(size); }

  public int charWidth(char ch) { return charWidth((int) ch); }

  public int charWidth(int codePoint) {
    boolean fm = frc != null && frc.usesFractionalMetrics();
    boolean hinted = frc == null || !frc.isAntiAliased();
    return Math.round(face.pixelAdvance(codePoint, size, fm, hinted));
  }

  public int stringWidth(String str) {
    if (frc != null && frc.usesFractionalMetrics()) {
      float total = 0f;
      for (int i = 0; i < str.length();) {
        int cp = str.codePointAt(i);
        total += face.pixelAdvance(cp, size, true, false);
        i += Character.charCount(cp);
      }
      return Math.round(total);
    }
    int w = 0;
    for (int i = 0; i < str.length();) {
      int cp = str.codePointAt(i);
      w += charWidth(cp);
      i += Character.charCount(cp);
    }
    return w;
  }

  public int charsWidth(char[] data, int off, int len) {
    return stringWidth(new String(data, off, len));
  }

  public int bytesWidth(byte[] data, int off, int len) {
    int w = 0;
    for (int i = off; i < off + len; i++) {
      w += charWidth(data[i] & 0xff);
    }
    return w;
  }

  public int[] getWidths() {
    int[] widths = new int[256];
    for (int ch = 0; ch < 256; ch++) {
      widths[ch] = charWidth(ch);
    }
    return widths;
  }
}
