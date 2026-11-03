/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.table;

public interface TableModel {
  boolean isCellEditable(int p0, int p1);
  int getRowCount();
  int getColumnCount();
  java.lang.String getColumnName(int p0);
  java.lang.Class<?> getColumnClass(int p0);
  java.lang.Object getValueAt(int p0, int p1);
  void setValueAt(java.lang.Object p0, int p1, int p2);
  void removeTableModelListener(javax.swing.event.TableModelListener p0);
  void addTableModelListener(javax.swing.event.TableModelListener p0);
}
