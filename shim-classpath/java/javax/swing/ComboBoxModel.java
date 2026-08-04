/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing;

public interface ComboBoxModel<E> extends javax.swing.ListModel<E> {
  void setSelectedItem(java.lang.Object p0);
  java.lang.Object getSelectedItem();
}
