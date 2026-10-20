/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.geom;

public abstract class Dimension2D implements Cloneable {
  protected Dimension2D() { }

  public abstract double getWidth();

  public abstract double getHeight();

  public abstract void setSize(double width, double height);

  public void setSize(Dimension2D d) {
    setSize(d.getWidth(), d.getHeight());
  }

  @Override
  public Object clone() {
    try {
      return super.clone();
    } catch (CloneNotSupportedException e) {
      throw new InternalError(e);
    }
  }
}
