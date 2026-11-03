/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.event;

public interface TreeModelListener extends java.util.EventListener {
  void treeNodesChanged(javax.swing.event.TreeModelEvent p0);
  void treeNodesInserted(javax.swing.event.TreeModelEvent p0);
  void treeNodesRemoved(javax.swing.event.TreeModelEvent p0);
  void treeStructureChanged(javax.swing.event.TreeModelEvent p0);
}
