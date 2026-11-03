/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.accessibility;

public interface AccessibleSelection {
  javax.accessibility.Accessible getAccessibleSelection(int p0);
  boolean isAccessibleChildSelected(int p0);
  int getAccessibleSelectionCount();
  void addAccessibleSelection(int p0);
  void removeAccessibleSelection(int p0);
  void clearAccessibleSelection();
  void selectAllAccessibleSelection();
}
