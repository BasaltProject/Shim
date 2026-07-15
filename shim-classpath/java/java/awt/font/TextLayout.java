/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.font;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.AttributedCharacterIterator;
import java.util.Map;

public final class TextLayout implements Cloneable {
  public static final CaretPolicy DEFAULT_CARET_POLICY = new CaretPolicy();

  private String text;
  private Font font;
  private FontRenderContext frc;
  private GlyphVector gv;
  private LineMetrics lm;

  public static class CaretPolicy {
    public CaretPolicy() { }

    public TextHitInfo getStrongCaret(TextHitInfo hit1, TextHitInfo hit2, TextLayout layout) {
      return hit1;
    }
  }

  public TextLayout(String string, Font font, FontRenderContext frc) {
    if (font == null) {
      throw new IllegalArgumentException("Null font passed to TextLayout constructor.");
    }
    if (string == null) {
      throw new IllegalArgumentException("Null string passed to TextLayout constructor.");
    }
    if (string.length() == 0) {
      throw new IllegalArgumentException("Zero length string passed to TextLayout constructor.");
    }
    init(string, font, frc);
  }

  public TextLayout(String string, Map<? extends AttributedCharacterIterator.Attribute, ?> attributes,
      FontRenderContext frc) {
    if (string == null) {
      throw new IllegalArgumentException("Null string passed to TextLayout constructor.");
    }
    if (attributes == null) {
      throw new IllegalArgumentException("Null map passed to TextLayout constructor.");
    }
    if (string.length() == 0) {
      throw new IllegalArgumentException("Zero length string passed to TextLayout constructor.");
    }
    init(string, Font.getFont(attributes) != null ? Font.getFont(attributes) : new Font(attributes), frc);
  }

  public TextLayout(AttributedCharacterIterator text, FontRenderContext frc) {
    if (text == null) {
      throw new IllegalArgumentException("Null iterator passed to TextLayout constructor.");
    }
    int start = text.getBeginIndex();
    int limit = text.getEndIndex();
    if (start == limit) {
      throw new IllegalArgumentException("Zero length iterator passed to TextLayout constructor.");
    }
    StringBuilder sb = new StringBuilder();
    for (char c = text.first(); c != java.text.CharacterIterator.DONE; c = text.next()) {
      sb.append(c);
    }
    Object f = text.getAttribute(TextAttribute.FONT);
    init(sb.toString(), f instanceof Font ? (Font) f : new Font(text.getAttributes()), frc);
  }

  private void init(String string, Font font, FontRenderContext frc) {
    this.text = string;
    this.font = font;
    this.frc = frc;
    this.gv = font.createGlyphVector(frc, string);
    this.lm = font.getLineMetrics(string, frc);
  }

  protected Object clone() {
    try {
      return super.clone();
    } catch (CloneNotSupportedException e) {
      throw new InternalError(e);
    }
  }

  public byte getBaseline() { return 0; }

  public float[] getBaselineOffsets() { return lm.getBaselineOffsets(); }

  public float getAdvance() { return (float) gv.getLogicalBounds().getWidth(); }

  public float getVisibleAdvance() {
    int end = text.length();
    while (end > 0 && Character.isWhitespace(text.charAt(end - 1))) {
      end--;
    }
    return end == 0 ? 0 : gv.getGlyphPosition(end).getX() == 0 ? getAdvance() : (float) gv.getGlyphPosition(end).getX();
  }

  public float getAscent() { return lm.getAscent(); }

  public float getDescent() { return lm.getDescent(); }

  public float getLeading() { return lm.getLeading(); }

  public Rectangle2D getBounds() { return gv.getVisualBounds(); }

  public Rectangle getPixelBounds(FontRenderContext frc, float x, float y) {
    return gv.getPixelBounds(frc, x, y);
  }

  public boolean isLeftToRight() { return true; }

  public boolean isVertical() { return false; }

  public int getCharacterCount() { return text.length(); }

  public float[] getCaretInfo(TextHitInfo hit, Rectangle2D bounds) {
    float x = caretX(hit);
    return new float[] {x, 0f};
  }

  public float[] getCaretInfo(TextHitInfo hit) {
    return getCaretInfo(hit, getNaturalBounds());
  }

  private float caretX(TextHitInfo hit) {
    int idx = hit.getInsertionIndex();
    idx = Math.max(0, Math.min(text.length(), idx));
    return (float) gv.getGlyphPosition(idx).getX();
  }

  private Rectangle2D getNaturalBounds() {
    return gv.getLogicalBounds();
  }

  public TextHitInfo getNextRightHit(TextHitInfo hit) {
    int i = hit.getInsertionIndex();
    return i >= text.length() ? null : TextHitInfo.afterOffset(i + 1 > text.length() ? i : i + 1 - 1 + 1 - 1 + 0 + (hit.isLeadingEdge() ? 0 : 1));
  }

  public TextHitInfo getNextRightHit(int offset) {
    if (offset < 0 || offset > text.length()) {
      throw new IllegalArgumentException("Offset out of bounds in TextLayout.getNextRightHit()");
    }
    return offset == text.length() ? null : TextHitInfo.afterOffset(offset + 1);
  }

  public TextHitInfo getNextRightHit(int offset, CaretPolicy policy) {
    return getNextRightHit(offset);
  }

  public TextHitInfo getNextLeftHit(TextHitInfo hit) {
    int i = hit.getInsertionIndex();
    return i <= 0 ? null : TextHitInfo.beforeOffset(i - 1);
  }

  public TextHitInfo getNextLeftHit(int offset) {
    if (offset < 0 || offset > text.length()) {
      throw new IllegalArgumentException("Offset out of bounds in TextLayout.getNextLeftHit()");
    }
    return offset == 0 ? null : TextHitInfo.beforeOffset(offset - 1);
  }

  public TextHitInfo getNextLeftHit(int offset, CaretPolicy policy) {
    return getNextLeftHit(offset);
  }

  public TextHitInfo getVisualOtherHit(TextHitInfo hit) {
    return hit.getOtherHit();
  }

  public Shape getCaretShape(TextHitInfo hit, Rectangle2D bounds) {
    float x = caretX(hit);
    return new java.awt.geom.Line2D.Float(x, -getAscent(), x, getDescent());
  }

  public Shape getCaretShape(TextHitInfo hit) {
    return getCaretShape(hit, getNaturalBounds());
  }

  public Shape[] getCaretShapes(int offset, Rectangle2D bounds, CaretPolicy policy) {
    return new Shape[] {getCaretShape(TextHitInfo.afterOffset(offset), bounds), null};
  }

  public Shape[] getCaretShapes(int offset, Rectangle2D bounds) {
    return getCaretShapes(offset, bounds, DEFAULT_CARET_POLICY);
  }

  public Shape[] getCaretShapes(int offset) {
    return getCaretShapes(offset, getNaturalBounds(), DEFAULT_CARET_POLICY);
  }

  public int[] getLogicalRangesForVisualSelection(TextHitInfo firstEndpoint, TextHitInfo secondEndpoint) {
    int a = firstEndpoint.getInsertionIndex();
    int b = secondEndpoint.getInsertionIndex();
    return new int[] {Math.min(a, b), Math.max(a, b)};
  }

  public Shape getVisualHighlightShape(TextHitInfo firstEndpoint, TextHitInfo secondEndpoint, Rectangle2D bounds) {
    float a = caretX(firstEndpoint);
    float b = caretX(secondEndpoint);
    return new Rectangle2D.Float(Math.min(a, b), -getAscent(), Math.abs(b - a), getAscent() + getDescent());
  }

  public Shape getVisualHighlightShape(TextHitInfo firstEndpoint, TextHitInfo secondEndpoint) {
    return getVisualHighlightShape(firstEndpoint, secondEndpoint, getNaturalBounds());
  }

  public Shape getLogicalHighlightShape(int firstEndpoint, int secondEndpoint, Rectangle2D bounds) {
    return getVisualHighlightShape(TextHitInfo.leading(firstEndpoint), TextHitInfo.leading(secondEndpoint), bounds);
  }

  public Shape getLogicalHighlightShape(int firstEndpoint, int secondEndpoint) {
    return getLogicalHighlightShape(firstEndpoint, secondEndpoint, getNaturalBounds());
  }

  public Shape getBlackBoxBounds(int firstEndpoint, int secondEndpoint) {
    return getLogicalHighlightShape(firstEndpoint, secondEndpoint);
  }

  public TextHitInfo hitTestChar(float x, float y, Rectangle2D bounds) {
    for (int i = 0; i < text.length(); i++) {
      double left = gv.getGlyphPosition(i).getX();
      double right = gv.getGlyphPosition(i + 1).getX();
      if (x < right || i == text.length() - 1) {
        if (x < left) {
          return TextHitInfo.leading(i);
        }
        return x - left < right - x ? TextHitInfo.leading(i) : TextHitInfo.trailing(i);
      }
    }
    return TextHitInfo.trailing(text.length() - 1);
  }

  public TextHitInfo hitTestChar(float x, float y) {
    return hitTestChar(x, y, getNaturalBounds());
  }

  public boolean equals(Object obj) {
    return obj instanceof TextLayout && equals((TextLayout) obj);
  }

  public boolean equals(TextLayout rhs) {
    return rhs != null && rhs.text.equals(text) && rhs.font.equals(font);
  }

  public int hashCode() { return text.hashCode(); }

  public String toString() { return gv.toString(); }

  public void draw(Graphics2D g2, float x, float y) {
    g2.drawGlyphVector(gv, x, y);
  }

  public Shape getOutline(AffineTransform tx) {
    Shape s = gv.getOutline();
    if (tx != null) {
      return tx.createTransformedShape(s);
    }
    return s;
  }

  public LayoutPath getLayoutPath() { return null; }

  public void hitToPoint(TextHitInfo hit, Point2D point) {
    point.setLocation(caretX(hit), 0);
  }

  public byte getCharacterLevel(int index) {
    if (index < -1 || index > text.length()) {
      throw new IllegalArgumentException("Index is out of range in getCharacterLevel.");
    }
    return 0;
  }

  public TextLayout getJustifiedLayout(float justificationWidth) {
    return this;
  }

  protected void handleJustify(float justificationWidth) { }
}
