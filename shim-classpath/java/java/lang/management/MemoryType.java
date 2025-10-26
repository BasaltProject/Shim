/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.management;

public enum MemoryType {
  HEAP("Heap memory"),
  NON_HEAP("Non-heap memory");

  private final String description;

  MemoryType(String s) {
    this.description = s;
  }

  @Override
  public String toString() {
    return description;
  }

  private static final long serialVersionUID = 6992337162326171013L;
}
