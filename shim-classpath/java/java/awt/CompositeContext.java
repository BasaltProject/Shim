/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt;

public interface CompositeContext {
  void compose(java.awt.image.Raster p0, java.awt.image.Raster p1, java.awt.image.WritableRaster p2);
  void dispose();
}
