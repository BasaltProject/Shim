/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.geom;

import java.util.NoSuchElementException;

class LineIterator implements PathIterator {
  Line2D line;
  AffineTransform affine;
  int index;

  LineIterator(Line2D l, AffineTransform at) {
    this.line = l;
    this.affine = at;
  }

  @Override
  public int getWindingRule() {
    return WIND_NON_ZERO;
  }

  @Override
  public boolean isDone() {
    return (index > 1);
  }

  @Override
  public void next() {
    index++;
  }

  @Override
  public int currentSegment(float[] coords) {
    if (isDone()) {
      throw new NoSuchElementException("line iterator out of bounds");
    }
    int type;
    if (index == 0) {
      coords[0] = (float) line.getX1();
      coords[1] = (float) line.getY1();
      type = SEG_MOVETO;
    } else {
      coords[0] = (float) line.getX2();
      coords[1] = (float) line.getY2();
      type = SEG_LINETO;
    }
    if (affine != null) {
      affine.transform(coords, 0, coords, 0, 1);
    }
    return type;
  }

  @Override
  public int currentSegment(double[] coords) {
    if (isDone()) {
      throw new NoSuchElementException("line iterator out of bounds");
    }
    int type;
    if (index == 0) {
      coords[0] = line.getX1();
      coords[1] = line.getY1();
      type = SEG_MOVETO;
    } else {
      coords[0] = line.getX2();
      coords[1] = line.getY2();
      type = SEG_LINETO;
    }
    if (affine != null) {
      affine.transform(coords, 0, coords, 0, 1);
    }
    return type;
  }
}
