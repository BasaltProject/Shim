/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.image;

public interface RasterOp {
  java.awt.image.WritableRaster filter(java.awt.image.Raster p0, java.awt.image.WritableRaster p1);
  java.awt.RenderingHints getRenderingHints();
  java.awt.image.WritableRaster createCompatibleDestRaster(java.awt.image.Raster p0);
  java.awt.geom.Point2D getPoint2D(java.awt.geom.Point2D p0, java.awt.geom.Point2D p1);
  java.awt.geom.Rectangle2D getBounds2D(java.awt.image.Raster p0);
}
