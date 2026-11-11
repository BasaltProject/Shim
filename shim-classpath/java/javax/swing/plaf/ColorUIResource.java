/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.plaf;

public class ColorUIResource extends java.awt.Color implements javax.swing.plaf.UIResource {
  public ColorUIResource(java.awt.Color p0) {
    super(p0.getRGB(), p0.getAlpha() != 255);
  }
  public ColorUIResource(float p0, float p1, float p2) {
    super(p0, p1, p2);
  }
  public ColorUIResource(int p0) {
    super(p0);
  }
  @java.beans.ConstructorProperties({"red", "green", "blue"})
  public ColorUIResource(int p0, int p1, int p2) {
    super(p0, p1, p2);
  }
}
