/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.tree;

public interface MutableTreeNode extends javax.swing.tree.TreeNode {
  void remove(javax.swing.tree.MutableTreeNode p0);
  void remove(int p0);
  void insert(javax.swing.tree.MutableTreeNode p0, int p1);
  void setParent(javax.swing.tree.MutableTreeNode p0);
  void removeFromParent();
  void setUserObject(java.lang.Object p0);
}
