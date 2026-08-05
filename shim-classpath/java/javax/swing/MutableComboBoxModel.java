/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing;

public interface MutableComboBoxModel<E> extends javax.swing.ComboBoxModel<E> {
  void addElement(E p0);
  void removeElementAt(int p0);
  void insertElementAt(E p0, int p1);
  void removeElement(java.lang.Object p0);
}
