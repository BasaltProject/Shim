/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt;

public final class GridBagLayoutInfo implements java.io.Serializable {
  int width;
  int height;
  int startx;
  int starty;
  int[] minWidth;
  int[] minHeight;
  double[] weightX;
  double[] weightY;

  GridBagLayoutInfo(int width, int height) {
    this.width = width;
    this.height = height;
  }

  GridBagLayoutInfo() { }
}
