/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt;

public interface PaintContext {
  void dispose();
  java.awt.image.ColorModel getColorModel();
  java.awt.image.Raster getRaster(int p0, int p1, int p2, int p3);
}
