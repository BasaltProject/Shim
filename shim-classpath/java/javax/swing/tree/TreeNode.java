/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.tree;

public interface TreeNode {
  javax.swing.tree.TreeNode getParent();
  int getIndex(javax.swing.tree.TreeNode p0);
  java.util.Enumeration<? extends javax.swing.tree.TreeNode> children();
  boolean isLeaf();
  int getChildCount();
  boolean getAllowsChildren();
  javax.swing.tree.TreeNode getChildAt(int p0);
}
