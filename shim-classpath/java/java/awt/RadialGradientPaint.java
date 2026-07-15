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

public final class RadialGradientPaint extends MultipleGradientPaint {
  private final Point2D focus;
  private final Point2D center;
  private final float radius;

  public RadialGradientPaint(float cx, float cy, float radius, float[] fractions, Color[] colors) {
    this(cx, cy, radius, cx, cy, fractions, colors, CycleMethod.NO_CYCLE);
  }

  public RadialGradientPaint(float cx, float cy, float radius, float[] fractions, Color[] colors,
      CycleMethod cycleMethod) {
    this(cx, cy, radius, cx, cy, fractions, colors, cycleMethod);
  }

  public RadialGradientPaint(float cx, float cy, float radius, float fx, float fy, float[] fractions, Color[] colors,
      CycleMethod cycleMethod) {
    this(new Point2D.Float(cx, cy), radius, new Point2D.Float(fx, fy), fractions, colors, cycleMethod,
        ColorSpaceType.SRGB, new AffineTransform());
  }

  public RadialGradientPaint(Point2D center, float radius, float[] fractions, Color[] colors) {
    this(center, radius, center, fractions, colors, CycleMethod.NO_CYCLE);
  }

  public RadialGradientPaint(Point2D center, float radius, float[] fractions, Color[] colors,
      CycleMethod cycleMethod) {
    this(center, radius, center, fractions, colors, cycleMethod);
  }

  public RadialGradientPaint(Point2D center, float radius, Point2D focus, float[] fractions, Color[] colors,
      CycleMethod cycleMethod) {
    this(center, radius, focus, fractions, colors, cycleMethod, ColorSpaceType.SRGB, new AffineTransform());
  }

  public RadialGradientPaint(Point2D center, float radius, Point2D focus, float[] fractions, Color[] colors,
      CycleMethod cycleMethod, ColorSpaceType colorSpace, AffineTransform gradientTransform) {
    super(fractions, colors, cycleMethod, colorSpace, gradientTransform);
    if (center == null) {
      throw new NullPointerException("Center point must be non-null");
    }
    if (focus == null) {
      throw new NullPointerException("Focus point must be non-null");
    }
    if (radius <= 0) {
      throw new IllegalArgumentException("Radius must be greater than zero");
    }
    this.center = new Point2D.Double(center.getX(), center.getY());
    this.focus = new Point2D.Double(focus.getX(), focus.getY());
    this.radius = radius;
  }

  public RadialGradientPaint(Rectangle2D gradientBounds, float[] fractions, Color[] colors, CycleMethod cycleMethod) {
    this(new Point2D.Double(gradientBounds.getCenterX(), gradientBounds.getCenterY()),
        1.0f, new Point2D.Double(gradientBounds.getCenterX(), gradientBounds.getCenterY()), fractions, colors,
        cycleMethod, ColorSpaceType.SRGB, boundsTransform(gradientBounds));
  }

  private static AffineTransform boundsTransform(Rectangle2D b) {
    if (b.isEmpty()) {
      throw new IllegalArgumentException("Gradient bounds must have non-zero width and height");
    }
    AffineTransform t = new AffineTransform();
    t.translate(b.getCenterX(), b.getCenterY());
    t.scale(b.getWidth() / 2, b.getHeight() / 2);
    t.translate(-b.getCenterX(), -b.getCenterY());
    return t;
  }

  public PaintContext createContext(ColorModel cm, Rectangle deviceBounds, Rectangle2D userBounds,
      AffineTransform transform, RenderingHints hints) {
    return new shim.awt.MultiGradientContext(this, true, center.getX(), center.getY(), focus.getX(), focus.getY(),
        radius, transform);
  }

  public Point2D getCenterPoint() { return new Point2D.Double(center.getX(), center.getY()); }

  public Point2D getFocusPoint() { return new Point2D.Double(focus.getX(), focus.getY()); }

  public float getRadius() { return radius; }
}
