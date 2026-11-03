/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.font;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

public abstract class GraphicAttribute {
  private int fAlignment;

  public static final int TOP_ALIGNMENT = -1;
  public static final int BOTTOM_ALIGNMENT = -2;
  public static final int ROMAN_BASELINE = 0;
  public static final int CENTER_BASELINE = 1;
  public static final int HANGING_BASELINE = 2;

  protected GraphicAttribute(int alignment) {
    if (alignment < BOTTOM_ALIGNMENT || alignment > HANGING_BASELINE) {
      throw new IllegalArgumentException("bad alignment");
    }
    fAlignment = alignment;
  }

  public abstract float getAscent();

  public abstract float getDescent();

  public abstract float getAdvance();

  public Rectangle2D getBounds() {
    float ascent = getAscent();
    return new Rectangle2D.Float(0, -ascent, getAdvance(), ascent + getDescent());
  }

  public Shape getOutline(AffineTransform tx) {
    Shape b = getBounds();
    if (tx != null) {
      b = tx.createTransformedShape(b);
    }
    return b;
  }

  public abstract void draw(Graphics2D graphics, float x, float y);

  public final int getAlignment() { return fAlignment; }

  public GlyphJustificationInfo getJustificationInfo() {
    float advance = getAdvance();
    return new GlyphJustificationInfo(advance, false, GlyphJustificationInfo.PRIORITY_NONE, 0, 0, false,
        GlyphJustificationInfo.PRIORITY_NONE, 0, 0);
  }
}
