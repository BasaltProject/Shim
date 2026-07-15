/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.accessibility;

public interface AccessibleExtendedTable extends javax.accessibility.AccessibleTable {
  int getAccessibleRow(int p0);
  int getAccessibleColumn(int p0);
  int getAccessibleIndex(int p0, int p1);
}
