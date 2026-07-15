/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.accessibility;

public interface AccessibleTableModelChange {
  int INSERT = 1;
  int UPDATE = 0;
  int DELETE = -1;
  int getType();
  int getFirstRow();
  int getLastRow();
  int getFirstColumn();
  int getLastColumn();
}
