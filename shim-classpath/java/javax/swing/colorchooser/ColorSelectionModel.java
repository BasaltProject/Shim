/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.colorchooser;

public interface ColorSelectionModel {
  void removeChangeListener(javax.swing.event.ChangeListener p0);
  void addChangeListener(javax.swing.event.ChangeListener p0);
  java.awt.Color getSelectedColor();
  void setSelectedColor(java.awt.Color p0);
}
