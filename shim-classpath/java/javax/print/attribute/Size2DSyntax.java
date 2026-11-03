/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.print.attribute;

import java.io.Serializable;

public abstract class Size2DSyntax implements Serializable, Cloneable {
  public static final int INCH = 25400;
  public static final int MM = 1000;

  private final int x;
  private final int y;

  protected Size2DSyntax(float x, float y, int units) {
    if (x < 0.0f) {
      throw new IllegalArgumentException("x < 0");
    }
    if (y < 0.0f) {
      throw new IllegalArgumentException("y < 0");
    }
    if (units < 1) {
      throw new IllegalArgumentException("units is < 1");
    }
    this.x = (int) (x * units + 0.5f);
    this.y = (int) (y * units + 0.5f);
  }

  protected Size2DSyntax(int x, int y, int units) {
    if (x < 0) {
      throw new IllegalArgumentException("x < 0");
    }
    if (y < 0) {
      throw new IllegalArgumentException("y < 0");
    }
    if (units < 1) {
      throw new IllegalArgumentException("units is < 1");
    }
    this.x = x * units;
    this.y = y * units;
  }

  private static float convertFromMicrometers(int x, int units) {
    if (units < 1) {
      throw new IllegalArgumentException(": units is < 1");
    }
    return ((float) x) / ((float) units);
  }

  public float[] getSize(int units) { return new float[] {getX(units), getY(units)}; }

  public float getX(int units) { return convertFromMicrometers(x, units); }

  public float getY(int units) { return convertFromMicrometers(y, units); }

  public String toString(int units, String unitsName) {
    StringBuilder result = new StringBuilder();
    result.append(getX(units));
    result.append('x');
    result.append(getY(units));
    if (unitsName != null) {
      result.append(' ');
      result.append(unitsName);
    }
    return result.toString();
  }

  public boolean equals(Object object) {
    return (object != null && object instanceof Size2DSyntax && this.x == ((Size2DSyntax) object).x
        && this.y == ((Size2DSyntax) object).y);
  }

  public int hashCode() { return (((x & 0x0000FFFF)) | ((y & 0x0000FFFF) << 16)); }

  public String toString() {
    StringBuilder result = new StringBuilder();
    result.append(x);
    result.append('x');
    result.append(y);
    result.append(" um");
    return result.toString();
  }

  protected int getXMicrometers() { return x; }

  protected int getYMicrometers() { return y; }
}
