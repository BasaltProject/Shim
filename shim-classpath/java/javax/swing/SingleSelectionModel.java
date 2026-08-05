/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing;

public interface SingleSelectionModel {
  void clearSelection();
  void removeChangeListener(javax.swing.event.ChangeListener p0);
  void addChangeListener(javax.swing.event.ChangeListener p0);
  int getSelectedIndex();
  void setSelectedIndex(int p0);
  boolean isSelected();
}
