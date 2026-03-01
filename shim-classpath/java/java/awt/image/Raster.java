/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.image;

public class Raster {
  protected final int width;
  protected final int height;
  protected final int numBands;

  protected Raster(int width, int height, int numBands) {
    this.width = width;
    this.height = height;
    this.numBands = numBands;
  }

  public final int getMinX() {
    return 0;
  }

  public final int getMinY() {
    return 0;
  }

  public final int getWidth() {
    return width;
  }

  public final int getHeight() {
    return height;
  }

  public final int getNumBands() {
    return numBands;
  }

  public int getSample(int x, int y, int b) {
    throw new UnsupportedOperationException();
  }
}
