/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.awt;

import java.awt.Shape;

public abstract class FontFace {
  public abstract String family();

  public abstract boolean canDisplay(int cp);

  public abstract float advance(int cp);

  public abstract Shape outline(int cp, boolean bold, boolean italic);

  public abstract float ascent();

  public abstract float descent();

  public abstract float leading();

  public String fullName() {
    return null;
  }

  public String postScriptName() {
    return null;
  }

  public int numGlyphs() {
    return 95;
  }

  public int missingGlyph() {
    return 0;
  }

  public int hintedAdvance(int cp, float size) {
    return -1;
  }

  public int maxAdvancePixels(float size) {
    return Math.round(size);
  }

  private static boolean zeroWidth(int cp) {
    return cp == 0 || (cp >= 9 && cp <= 13) || cp == 0x85 || cp == 0xAD || (cp >= 0x200B && cp <= 0x200F)
        || (cp >= 0x2028 && cp <= 0x202E) || (cp >= 0x2060 && cp <= 0x206F) || cp == 0xFEFF;
  }

  private static boolean wide(int cp) {
    return (cp >= 0x3000 && cp <= 0x30FF) || (cp >= 0x3400 && cp <= 0x4DBF) || (cp >= 0x4E00 && cp <= 0x9FFF)
        || (cp >= 0xAC00 && cp <= 0xD7AF) || (cp >= 0xF900 && cp <= 0xFAFF) || (cp >= 0xFF01 && cp <= 0xFF60)
        || (cp >= 0xFFE0 && cp <= 0xFFE6);
  }

  public float pixelAdvance(int cp, float size, boolean fractional, boolean hinted) {
    if (zeroWidth(cp)) {
      return 0f;
    }
    if (wide(cp) && !canDisplay(cp)) {
      return fractional ? size : Math.round(size);
    }
    if (fractional) {
      return advancePixels(advance(cp), size);
    }
    if (hinted) {
      int h = hintedAdvance(cp, size);
      if (h >= 0) {
        return h;
      }
    }
    return Math.round(advance(cp) * size);
  }

  public long ligature(String s, int index) {
    return 0;
  }

  public float glyphAdvance(int glyph) {
    return 0;
  }

  public boolean styled() {
    return false;
  }

  public int glyphCode(int cp) {
    return cp;
  }

  public int codePointOf(int glyph) {
    return glyph;
  }

  public float pixels(float emFraction, float size) {
    return emFraction * size;
  }

  public float advancePixels(float emFraction, float size) {
    return pixels(emFraction, size);
  }

  public float[] styleMetrics(float size, float ascentPixels) {
    float thickness = size / 12f;
    return new float[] {-ascentPixels / 2.5f, thickness, thickness / 1.5f, thickness};
  }

  public int unitsPerEm() {
    return 0;
  }

  public java.awt.Shape scaledOutline(int cp, float size) {
    int upem = unitsPerEm();
    if (upem <= 0) {
      return null;
    }
    java.awt.Shape em = outline(cp, false, false);
    long scale = TrueTypeHinter.divFix(Math.round(size * 64f), upem);
    java.awt.geom.Path2D.Float out = new java.awt.geom.Path2D.Float(java.awt.geom.Path2D.WIND_NON_ZERO);
    java.awt.geom.PathIterator it = em.getPathIterator(null);
    float[] c = new float[6];
    float sx = 0;
    float sy = 0;
    float cx = 0;
    float cy = 0;
    while (!it.isDone()) {
      int type = it.currentSegment(c);
      int n = type == java.awt.geom.PathIterator.SEG_CLOSE ? 0
          : type == java.awt.geom.PathIterator.SEG_QUADTO ? 4
          : type == java.awt.geom.PathIterator.SEG_CUBICTO ? 6 : 2;
      for (int i = 0; i < n; i++) {
        c[i] = TrueTypeHinter.mulFix(Math.round(c[i] * upem), scale) / 64f;
      }
      switch (type) {
      case java.awt.geom.PathIterator.SEG_MOVETO:
        out.moveTo(c[0], c[1]);
        sx = cx = c[0];
        sy = cy = c[1];
        break;
      case java.awt.geom.PathIterator.SEG_LINETO:
        out.lineTo(c[0], c[1]);
        cx = c[0];
        cy = c[1];
        break;
      case java.awt.geom.PathIterator.SEG_QUADTO:
        out.quadTo(c[0], c[1], c[2], c[3]);
        cx = c[2];
        cy = c[3];
        break;
      case java.awt.geom.PathIterator.SEG_CUBICTO:
        out.curveTo(c[0], c[1], c[2], c[3], c[4], c[5]);
        cx = c[4];
        cy = c[5];
        break;
      default:
        if (cx != sx || cy != sy) {
          out.lineTo(sx, sy);
        }
        out.closePath();
      }
      it.next();
    }
    return out;
  }

  public java.awt.Shape hintedGlyph(int cp, float size) {
    return null;
  }

  public float lineHeight() {
    return ascent() + descent() + leading();
  }

  public boolean leadingFromHeight() {
    return false;
  }

  public float kerning(int a, int b) {
    return 0f;
  }
}
