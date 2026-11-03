/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing;

public interface ComboBoxEditor {
  java.awt.Component getEditorComponent();
  void setItem(java.lang.Object p0);
  void selectAll();
  java.lang.Object getItem();
  void addActionListener(java.awt.event.ActionListener p0);
  void removeActionListener(java.awt.event.ActionListener p0);
}
