/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt;

import java.awt.geom.Dimension2D;

public class Dimension extends Dimension2D implements java.io.Serializable {
  public int width;
  public int height;
  private static final long serialVersionUID = 4723952579491349524L;

  public Dimension() {
    this(0, 0);
  }

  public Dimension(Dimension d) {
    this(d.width, d.height);
  }

  public Dimension(int width, int height) {
    this.width = width;
    this.height = height;
  }

  @Override
  public double getWidth() {
    return width;
  }

  @Override
  public double getHeight() {
    return height;
  }

  @Override
  public void setSize(double width, double height) {
    this.width = (int) Math.ceil(width);
    this.height = (int) Math.ceil(height);
  }

  public Dimension getSize() {
    return new Dimension(width, height);
  }

  public void setSize(Dimension d) {
    setSize(d.width, d.height);
  }

  public void setSize(int width, int height) {
    this.width = width;
    this.height = height;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Dimension) {
      Dimension d = (Dimension) obj;
      return (width == d.width) && (height == d.height);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int sum = width + height;
    return sum * (sum + 1) / 2 + width;
  }

  @Override
  public String toString() {
    return getClass().getName() + "[width=" + width + ",height=" + height + "]";
  }
}
