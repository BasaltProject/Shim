/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.print.attribute;

import java.io.Serializable;

public abstract class IntegerSyntax implements Serializable, Cloneable {
  private final int value;

  protected IntegerSyntax(int value) { this.value = value; }

  protected IntegerSyntax(int value, int lowerBound, int upperBound) {
    if (lowerBound > value || value > upperBound) {
      throw new IllegalArgumentException("Value " + value + " not in range " + lowerBound + ":" + upperBound);
    }
    this.value = value;
  }

  public int getValue() { return value; }

  public boolean equals(Object object) {
    return object != null && object instanceof IntegerSyntax && value == ((IntegerSyntax) object).value;
  }

  public int hashCode() { return value; }

  public String toString() { return "" + value; }
}
