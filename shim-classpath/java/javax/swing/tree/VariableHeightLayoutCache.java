/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.tree;

import java.awt.Rectangle;
import java.util.Enumeration;
import javax.swing.event.TreeModelEvent;

public class VariableHeightLayoutCache extends AbstractLayoutCache {
  private final TreeRows rows = new TreeRows(this, false);

  public VariableHeightLayoutCache() { }

  public void setModel(TreeModel newModel) {
    super.setModel(newModel);
    rows.reset();
  }

  public void setRootVisible(boolean rootVisible) {
    if (isRootVisible() != rootVisible) {
      super.setRootVisible(rootVisible);
      rows.invalidate();
    }
  }

  public void setRowHeight(int rowHeight) {
    if (rowHeight != getRowHeight()) {
      super.setRowHeight(rowHeight);
      rows.invalidate();
    }
  }

  public void setNodeDimensions(NodeDimensions nd) {
    super.setNodeDimensions(nd);
    rows.invalidate();
  }

  public Rectangle getBounds(TreePath path, Rectangle placeIn) { return rows.getBounds(path, placeIn); }

  public TreePath getPathForRow(int row) { return rows.pathForRow(row); }

  public int getRowForPath(TreePath path) { return rows.rowForPath(path); }

  public int getRowCount() { return rows.rowCount(); }

  public void setExpandedState(TreePath path, boolean isExpanded) { rows.setExpanded(path, isExpanded); }

  public boolean getExpandedState(TreePath path) { return rows.isExpanded(path) && rows.rowForPath(path) >= 0; }

  public boolean isExpanded(TreePath path) { return rows.isExpanded(path); }

  public TreePath getPathClosestTo(int x, int y) { return rows.closestTo(x, y); }

  public Enumeration<TreePath> getVisiblePathsFrom(TreePath path) { return rows.visiblePathsFrom(path); }

  public int getVisibleChildCount(TreePath path) { return rows.visibleChildCount(path); }

  public void invalidateSizes() { rows.invalidate(); }

  public void invalidatePathBounds(TreePath path) { rows.invalidate(); }

  public void treeNodesChanged(TreeModelEvent e) { rows.invalidate(); }

  public void treeNodesInserted(TreeModelEvent e) { rows.invalidate(); }

  public void treeNodesRemoved(TreeModelEvent e) { rows.nodesRemoved(e); }

  public void treeStructureChanged(TreeModelEvent e) { rows.structureChanged(e); }
}
