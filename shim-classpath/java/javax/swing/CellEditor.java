/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing;

public interface CellEditor {
  boolean isCellEditable(java.util.EventObject p0);
  boolean shouldSelectCell(java.util.EventObject p0);
  boolean stopCellEditing();
  void cancelCellEditing();
  void addCellEditorListener(javax.swing.event.CellEditorListener p0);
  void removeCellEditorListener(javax.swing.event.CellEditorListener p0);
  java.lang.Object getCellEditorValue();
}
