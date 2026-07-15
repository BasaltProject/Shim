/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.image;

public abstract class LookupTable {
  int numComponents;
  int offset;
  int numEntries;

  protected LookupTable(int offset, int numComponents) {
    if (offset < 0) {
      throw new IllegalArgumentException("Offset must be greater than 0");
    }
    if (numComponents < 1) {
      throw new IllegalArgumentException("Number of components must be at least 1");
    }
    this.numComponents = numComponents;
    this.offset = offset;
  }

  public int getNumComponents() { return numComponents; }

  public int getOffset() { return offset; }

  public abstract int[] lookupPixel(int[] src, int[] dest);
}
