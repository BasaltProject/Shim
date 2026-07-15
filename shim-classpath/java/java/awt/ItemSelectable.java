/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt;

public interface ItemSelectable {
  void addItemListener(java.awt.event.ItemListener p0);
  java.lang.Object[] getSelectedObjects();
  void removeItemListener(java.awt.event.ItemListener p0);
}
