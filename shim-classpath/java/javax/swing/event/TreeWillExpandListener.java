/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.event;

public interface TreeWillExpandListener extends java.util.EventListener {
  void treeWillExpand(javax.swing.event.TreeExpansionEvent p0) throws javax.swing.tree.ExpandVetoException;
  void treeWillCollapse(javax.swing.event.TreeExpansionEvent p0) throws javax.swing.tree.ExpandVetoException;
}
