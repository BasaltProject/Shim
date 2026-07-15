/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.image;

public interface WritableRenderedImage extends java.awt.image.RenderedImage {
  void setData(java.awt.image.Raster p0);
  void addTileObserver(java.awt.image.TileObserver p0);
  void removeTileObserver(java.awt.image.TileObserver p0);
  boolean isTileWritable(int p0, int p1);
  java.awt.Point[] getWritableTileIndices();
  boolean hasTileWriters();
  java.awt.image.WritableRaster getWritableTile(int p0, int p1);
  void releaseWritableTile(int p0, int p1);
}
