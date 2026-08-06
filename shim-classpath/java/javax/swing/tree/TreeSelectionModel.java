/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.tree;

public interface TreeSelectionModel {
  int SINGLE_TREE_SELECTION = 1;
  int CONTIGUOUS_TREE_SELECTION = 2;
  int DISCONTIGUOUS_TREE_SELECTION = 4;
  void clearSelection();
  int getSelectionMode();
  void setRowMapper(javax.swing.tree.RowMapper p0);
  javax.swing.tree.RowMapper getRowMapper();
  void resetRowSelection();
  void setSelectionPaths(javax.swing.tree.TreePath[] p0);
  javax.swing.tree.TreePath[] getSelectionPaths();
  javax.swing.tree.TreePath getLeadSelectionPath();
  void setSelectionPath(javax.swing.tree.TreePath p0);
  boolean isRowSelected(int p0);
  void addSelectionPath(javax.swing.tree.TreePath p0);
  void addSelectionPaths(javax.swing.tree.TreePath[] p0);
  javax.swing.tree.TreePath getSelectionPath();
  int[] getSelectionRows();
  int getSelectionCount();
  int getMinSelectionRow();
  int getMaxSelectionRow();
  boolean isPathSelected(javax.swing.tree.TreePath p0);
  void removeTreeSelectionListener(javax.swing.event.TreeSelectionListener p0);
  void addTreeSelectionListener(javax.swing.event.TreeSelectionListener p0);
  void removeSelectionPaths(javax.swing.tree.TreePath[] p0);
  void removeSelectionPath(javax.swing.tree.TreePath p0);
  boolean isSelectionEmpty();
  int getLeadSelectionRow();
  void setSelectionMode(int p0);
  void addPropertyChangeListener(java.beans.PropertyChangeListener p0);
  void removePropertyChangeListener(java.beans.PropertyChangeListener p0);
}
