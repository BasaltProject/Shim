/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.table;

public interface TableColumnModel {
  void setColumnMargin(int p0);
  int getColumnMargin();
  int getColumnCount();
  javax.swing.table.TableColumn getColumn(int p0);
  void removeColumn(javax.swing.table.TableColumn p0);
  void addColumn(javax.swing.table.TableColumn p0);
  int[] getSelectedColumns();
  boolean getColumnSelectionAllowed();
  void setColumnSelectionAllowed(boolean p0);
  int getSelectedColumnCount();
  int getColumnIndex(java.lang.Object p0);
  void moveColumn(int p0, int p1);
  int getColumnIndexAtX(int p0);
  int getTotalColumnWidth();
  void removeColumnModelListener(javax.swing.event.TableColumnModelListener p0);
  void addColumnModelListener(javax.swing.event.TableColumnModelListener p0);
  java.util.Enumeration<javax.swing.table.TableColumn> getColumns();
  void setSelectionModel(javax.swing.ListSelectionModel p0);
  javax.swing.ListSelectionModel getSelectionModel();
}
