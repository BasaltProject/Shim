/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.tree;

public interface TreeModel {
  java.lang.Object getRoot();
  boolean isLeaf(java.lang.Object p0);
  int getChildCount(java.lang.Object p0);
  java.lang.Object getChild(java.lang.Object p0, int p1);
  void valueForPathChanged(javax.swing.tree.TreePath p0, java.lang.Object p1);
  void removeTreeModelListener(javax.swing.event.TreeModelListener p0);
  void addTreeModelListener(javax.swing.event.TreeModelListener p0);
  int getIndexOfChild(java.lang.Object p0, java.lang.Object p1);
}
