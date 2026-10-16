/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.text;

public class Annotation {
  private Object value;

  public Annotation(Object value) {
    this.value = value;
  }

  public Object getValue() {
    return value;
  }

  public String toString() {
    return getClass().getName() + "[value=" + value + "]";
  }
}
