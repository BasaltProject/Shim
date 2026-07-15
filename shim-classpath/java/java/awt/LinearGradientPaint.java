/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.ColorModel;

public final class LinearGradientPaint extends MultipleGradientPaint {
  private final Point2D start;
  private final Point2D end;

  public LinearGradientPaint(float startX, float startY, float endX, float endY, float[] fractions, Color[] colors) {
    this(new Point2D.Float(startX, startY), new Point2D.Float(endX, endY), fractions, colors, CycleMethod.NO_CYCLE);
  }

  public LinearGradientPaint(float startX, float startY, float endX, float endY, float[] fractions, Color[] colors,
      CycleMethod cycleMethod) {
    this(new Point2D.Float(startX, startY), new Point2D.Float(endX, endY), fractions, colors, cycleMethod);
  }

  public LinearGradientPaint(Point2D start, Point2D end, float[] fractions, Color[] colors) {
    this(start, end, fractions, colors, CycleMethod.NO_CYCLE);
  }

  public LinearGradientPaint(Point2D start, Point2D end, float[] fractions, Color[] colors,
      CycleMethod cycleMethod) {
    this(start, end, fractions, colors, cycleMethod, ColorSpaceType.SRGB, new AffineTransform());
  }

  public LinearGradientPaint(Point2D start, Point2D end, float[] fractions, Color[] colors, CycleMethod cycleMethod,
      ColorSpaceType colorSpace, AffineTransform gradientTransform) {
    super(fractions, colors, cycleMethod, colorSpace, gradientTransform);
    if (start == null || end == null) {
      throw new NullPointerException("Start and end points must be non-null");
    }
    if (start.equals(end)) {
      throw new IllegalArgumentException("Start point cannot equalendpoint");
    }
    this.start = new Point2D.Double(start.getX(), start.getY());
    this.end = new Point2D.Double(end.getX(), end.getY());
  }

  public PaintContext createContext(ColorModel cm, Rectangle deviceBounds, Rectangle2D userBounds,
      AffineTransform transform, RenderingHints hints) {
    return new shim.awt.MultiGradientContext(this, false, start.getX(), start.getY(), end.getX(), end.getY(), 0,
        transform);
  }

  public Point2D getStartPoint() { return new Point2D.Double(start.getX(), start.getY()); }

  public Point2D getEndPoint() { return new Point2D.Double(end.getX(), end.getY()); }
}
