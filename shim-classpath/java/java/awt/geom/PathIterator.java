/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.geom;

public interface PathIterator {
  int WIND_EVEN_ODD = 0;
  int WIND_NON_ZERO = 1;
  int SEG_MOVETO = 0;
  int SEG_LINETO = 1;
  int SEG_QUADTO = 2;
  int SEG_CUBICTO = 3;
  int SEG_CLOSE = 4;

  int getWindingRule();

  boolean isDone();

  void next();

  int currentSegment(float[] coords);

  int currentSegment(double[] coords);
}
