/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.text;

import java.io.Serializable;

abstract class GapVector implements Serializable {
  private Object array;
  private int g0;
  private int g1;

  public GapVector() { this(10); }

  public GapVector(int initialLength) {
    array = allocateArray(initialLength);
    g0 = 0;
    g1 = initialLength;
  }

  protected abstract Object allocateArray(int len);

  protected abstract int getArrayLength();

  protected final Object getArray() { return array; }

  protected final int getGapStart() { return g0; }

  protected final int getGapEnd() { return g1; }

  protected void replaceUpdate(int position, int rmSize, int addSize) { }
}
