/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.awt;

import java.awt.Font;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphJustificationInfo;
import java.awt.font.GlyphMetrics;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public final class SimpleGlyphVector extends GlyphVector {
  private final Font font;
  private final FontRenderContext frc;
  private final int[] codes;
  private final int[] charIndex;
  private final float[] pos;
  private final AffineTransform[] transforms;

  public SimpleGlyphVector(Font font, FontRenderContext frc, int[] codes, int[] charIndex) {
    this.font = font;
    this.frc = frc;
    this.codes = codes;
    this.charIndex = charIndex;
    this.pos = new float[(codes.length + 1) * 2];
    this.transforms = new AffineTransform[codes.length];
    performDefaultLayout();
  }

  private FontFace face() {
    return Fonts.face(font.getName(), font.getStyle());
  }

  public void performDefaultLayout() {
    FontFace f = face();
    float x = 0;
    boolean fm = frc != null && frc.usesFractionalMetrics();
    AffineTransform ft = font.isTransformed() ? font.getTransform() : null;
    float mx = ft == null ? 1f : (float) ft.getScaleX();
    float my = ft == null ? 0f : (float) ft.getShearY();
    for (int i = 0; i < codes.length; i++) {
      pos[i * 2] = x * mx;
      pos[i * 2 + 1] = x * my;
      x += f.pixelAdvance(codes[i], font.getSize2D(), fm, frc == null || !frc.isAntiAliased())
          + font.tracking() * font.getSize2D();
    }
    pos[codes.length * 2] = x * mx;
    pos[codes.length * 2 + 1] = x * my;
  }

  public void applyKerning() {
    FontFace f = face();
    AffineTransform ft = font.isTransformed() ? font.getTransform() : null;
    float mx = ft == null ? 1f : (float) ft.getScaleX();
    float shift = 0;
    for (int i = 1; i <= codes.length; i++) {
      if (i < codes.length) {
        shift += f.pixels(f.kerning(codes[i - 1], codes[i]), font.getSize2D()) * mx;
      }
      if (i < codes.length) {
        pos[i * 2] += shift;
      }
    }
    pos[codes.length * 2] += shift;
  }

  public Font getFont() { return font; }

  public FontRenderContext getFontRenderContext() { return frc; }

  public int getNumGlyphs() { return codes.length; }

  public int getGlyphCode(int glyphIndex) { return face().glyphCode(codes[glyphIndex]); }

  public int[] getGlyphCodes(int beginGlyphIndex, int numEntries, int[] codeReturn) {
    int[] out = codeReturn != null ? codeReturn : new int[numEntries];
    FontFace f = face();
    for (int i = 0; i < numEntries; i++) {
      out[i] = f.glyphCode(codes[beginGlyphIndex + i]);
    }
    return out;
  }

  public int getGlyphCharIndex(int glyphIndex) { return charIndex[glyphIndex]; }

  public Point2D getGlyphPosition(int glyphIndex) {
    if (glyphIndex < 0 || glyphIndex > codes.length) {
      throw new IndexOutOfBoundsException("glyphIndex " + glyphIndex + " is out of bounds");
    }
    return new Point2D.Float(pos[glyphIndex * 2], pos[glyphIndex * 2 + 1]);
  }

  public void setGlyphPosition(int glyphIndex, Point2D newPos) {
    if (glyphIndex < 0 || glyphIndex > codes.length) {
      throw new IndexOutOfBoundsException("glyphIndex " + glyphIndex + " is out of bounds");
    }
    pos[glyphIndex * 2] = (float) newPos.getX();
    pos[glyphIndex * 2 + 1] = (float) newPos.getY();
  }

  public float[] getGlyphPositions(int beginGlyphIndex, int numEntries, float[] positionReturn) {
    float[] out = positionReturn != null ? positionReturn : new float[numEntries * 2];
    System.arraycopy(pos, beginGlyphIndex * 2, out, 0, numEntries * 2);
    return out;
  }

  public AffineTransform getGlyphTransform(int glyphIndex) {
    return transforms[glyphIndex] == null ? null : new AffineTransform(transforms[glyphIndex]);
  }

  public void setGlyphTransform(int glyphIndex, AffineTransform newTX) {
    transforms[glyphIndex] = newTX == null ? null : new AffineTransform(newTX);
  }

  public Shape getGlyphOutline(int glyphIndex) {
    FontFace f = face();
    boolean synthetic = font.isBold() && !f.styled() || font.isItalic() && !f.styled();
    AffineTransform ft0 = font.isTransformed() ? font.getTransform() : null;
    float k = 1f;
    if (ft0 != null) {
      boolean uniform = ft0.getShearX() == 0 && ft0.getShearY() == 0 && ft0.getScaleX() == ft0.getScaleY();
      k = uniform ? (float) ft0.getScaleX() : 0f;
    }
    Shape raw = synthetic || k == 0f ? null : f.scaledOutline(codes[glyphIndex], font.getSize2D() * k);
    boolean scaled = raw != null;
    if (!scaled) {
      raw = f.outline(codes[glyphIndex], font.isBold() && !f.styled(), font.isItalic() && !f.styled());
    }
    AffineTransform at = new AffineTransform();
    AffineTransform ft = font.isTransformed() ? font.getTransform() : null;
    at.translate(pos[glyphIndex * 2] + (ft == null ? 0 : ft.getTranslateX()),
        pos[glyphIndex * 2 + 1] + (ft == null ? 0 : ft.getTranslateY()));
    if (transforms[glyphIndex] != null) {
      at.concatenate(transforms[glyphIndex]);
    }
    if (!scaled) {
      at.scale(font.getSize2D(), font.getSize2D());
    }
    if (ft != null && !scaled) {
      at.concatenate(new AffineTransform(ft.getScaleX(), ft.getShearY(), ft.getShearX(), ft.getScaleY(), 0, 0));
    }
    return at.createTransformedShape(raw);
  }

  public Shape getOutline() {
    Path2D.Double all = new Path2D.Double();
    for (int i = 0; i < codes.length; i++) {
      all.append(getGlyphOutline(i), false);
    }
    return all;
  }

  public Shape getOutline(float x, float y) {
    return AffineTransform.getTranslateInstance(x, y).createTransformedShape(getOutline());
  }

  public Rectangle2D getLogicalBounds() {
    FontFace f = face();
    float size = font.getSize2D();
    AffineTransform ft = font.isTransformed() ? font.getTransform() : null;
    float sy = ft == null ? 1f : (float) ft.getScaleY();
    float ty = ft == null ? 0f : (float) ft.getTranslateY();
    return new Rectangle2D.Float(0, -f.pixels(f.ascent(), size) * sy + ty, pos[codes.length * 2],
        f.pixels(f.lineHeight(), size) * sy);
  }

  public Rectangle2D getVisualBounds() {
    Rectangle2D b = getOutline().getBounds2D();
    return new Rectangle2D.Float((float) b.getX(), (float) b.getY(), (float) b.getWidth(), (float) b.getHeight());
  }

  public Rectangle getPixelBounds(FontRenderContext renderFRC, float x, float y) {
    return getOutline(x, y).getBounds();
  }

  public Shape getGlyphLogicalBounds(int glyphIndex) {
    FontFace f = face();
    float size = font.getSize2D();
    float x = pos[glyphIndex * 2];
    float next = pos[(glyphIndex + 1) * 2];
    return new Rectangle2D.Float(x, pos[glyphIndex * 2 + 1] - f.pixels(f.ascent(), size), next - x,
        f.pixels(f.lineHeight(), size));
  }

  public Shape getGlyphVisualBounds(int glyphIndex) {
    Rectangle2D b = getGlyphOutline(glyphIndex).getBounds2D();
    return new Rectangle2D.Float((float) b.getX(), (float) b.getY(), (float) b.getWidth(), (float) b.getHeight());
  }

  public GlyphMetrics getGlyphMetrics(int glyphIndex) {
    float adv = pos[(glyphIndex + 1) * 2] - pos[glyphIndex * 2];
    Rectangle2D b = getGlyphOutline(glyphIndex).getBounds2D();
    boolean ws = Character.isWhitespace(codes[glyphIndex]);
    return new GlyphMetrics(adv, new Rectangle2D.Float((float) (b.getX() - pos[glyphIndex * 2]), (float) b.getY(),
        (float) b.getWidth(), (float) b.getHeight()), ws ? GlyphMetrics.WHITESPACE : GlyphMetrics.STANDARD);
  }

  public GlyphJustificationInfo getGlyphJustificationInfo(int glyphIndex) {
    return null;
  }

  public boolean equals(GlyphVector set) {
    if (set == this) {
      return true;
    }
    SimpleGlyphVector o = null;
    if (!(((o = set instanceof SimpleGlyphVector ? (SimpleGlyphVector) set : null) != null)) || o.codes.length != codes.length || !o.font.equals(font)) {
      return false;
    }
    return java.util.Arrays.equals(codes, o.codes) && java.util.Arrays.equals(pos, o.pos);
  }

  public int getLayoutFlags() { return 0; }
}
