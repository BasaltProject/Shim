/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.font;

public abstract class GlyphVector implements java.lang.Cloneable {
  public static final int FLAG_HAS_TRANSFORMS = 1;
  public static final int FLAG_HAS_POSITION_ADJUSTMENTS = 2;
  public static final int FLAG_RUN_RTL = 4;
  public static final int FLAG_COMPLEX_GLYPHS = 8;
  public static final int FLAG_MASK = 15;
  protected GlyphVector() {
  }
  public abstract boolean equals(java.awt.font.GlyphVector p0);
  public int getGlyphCharIndex(int glyphIndex) { return glyphIndex; }
  public abstract java.awt.geom.Rectangle2D getVisualBounds();
  public abstract java.awt.Shape getGlyphOutline(int p0);
  public java.awt.Shape getGlyphOutline(int glyphIndex, float x, float y) {
    java.awt.Shape s = getGlyphOutline(glyphIndex);
    java.awt.geom.AffineTransform at = java.awt.geom.AffineTransform.getTranslateInstance(x, y);
    return at.createTransformedShape(s);
  }
  public abstract java.awt.Shape getGlyphVisualBounds(int p0);
  public abstract void performDefaultLayout();
  public abstract int getGlyphCode(int p0);
  public abstract int[] getGlyphCodes(int p0, int p1, int[] p2);
  public int[] getGlyphCharIndices(int beginGlyphIndex, int numEntries, int[] codeReturn) {
    if (codeReturn == null) {
      codeReturn = new int[numEntries];
    }
    for (int i = 0, j = beginGlyphIndex; i < numEntries; ++i, ++j) {
      codeReturn[i] = getGlyphCharIndex(j);
    }
    return codeReturn;
  }
  public abstract java.awt.geom.Rectangle2D getLogicalBounds();
  public java.awt.Rectangle getPixelBounds(java.awt.font.FontRenderContext renderFRC, float x, float y) {
    java.awt.geom.Rectangle2D rect = getVisualBounds();
    int l = (int) Math.floor(rect.getX() + x);
    int t = (int) Math.floor(rect.getY() + y);
    int r = (int) Math.ceil(rect.getMaxX() + x);
    int b = (int) Math.ceil(rect.getMaxY() + y);
    return new java.awt.Rectangle(l, t, r - l, b - t);
  }
  public abstract java.awt.geom.Point2D getGlyphPosition(int p0);
  public abstract void setGlyphPosition(int p0, java.awt.geom.Point2D p1);
  public abstract java.awt.geom.AffineTransform getGlyphTransform(int p0);
  public abstract void setGlyphTransform(int p0, java.awt.geom.AffineTransform p1);
  public int getLayoutFlags() { return 0; }
  public abstract float[] getGlyphPositions(int p0, int p1, float[] p2);
  public abstract java.awt.Shape getGlyphLogicalBounds(int p0);
  public java.awt.Rectangle getGlyphPixelBounds(int index, java.awt.font.FontRenderContext renderFRC, float x, float y) {
    java.awt.geom.Rectangle2D rect = getGlyphVisualBounds(index).getBounds2D();
    int l = (int) Math.floor(rect.getX() + x);
    int t = (int) Math.floor(rect.getY() + y);
    int r = (int) Math.ceil(rect.getMaxX() + x);
    int b = (int) Math.ceil(rect.getMaxY() + y);
    return new java.awt.Rectangle(l, t, r - l, b - t);
  }
  public abstract java.awt.font.GlyphMetrics getGlyphMetrics(int p0);
  public abstract java.awt.font.GlyphJustificationInfo getGlyphJustificationInfo(int p0);
  public abstract java.awt.Shape getOutline(float p0, float p1);
  public abstract java.awt.Shape getOutline();
  public abstract java.awt.Font getFont();
  public abstract int getNumGlyphs();
  public abstract java.awt.font.FontRenderContext getFontRenderContext();
}
