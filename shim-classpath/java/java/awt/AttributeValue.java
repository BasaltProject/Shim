/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt;

abstract class AttributeValue {
  private final int value;
  private final String[] names;

  protected AttributeValue(int value, String[] names) {
    this.value = value;
    this.names = names;
  }

  public int hashCode() { return value; }

  public String toString() { return names[value]; }
}
