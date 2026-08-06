/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.table;

public abstract class TableStringConverter {
  protected TableStringConverter() {
  }
  public abstract java.lang.String toString(javax.swing.table.TableModel p0, int p1, int p2);
}
