/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.image;

public class WritableRaster extends Raster {
  protected WritableRaster(int width, int height, int numBands) {
    super(width, height, numBands);
  }

  public void setSample(int x, int y, int b, int s) {
    throw new UnsupportedOperationException();
  }
}
