/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.font;

public abstract class LayoutPath {
  protected LayoutPath() {
  }
  public abstract boolean pointToPath(java.awt.geom.Point2D p0, java.awt.geom.Point2D p1);
  public abstract void pathToPoint(java.awt.geom.Point2D p0, boolean p1, java.awt.geom.Point2D p2);
}
