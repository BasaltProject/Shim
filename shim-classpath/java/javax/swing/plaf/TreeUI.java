/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.plaf;

public abstract class TreeUI extends javax.swing.plaf.ComponentUI {
  protected TreeUI() {
  }
  public abstract javax.swing.tree.TreePath getPathForRow(javax.swing.JTree p0, int p1);
  public abstract int getRowCount(javax.swing.JTree p0);
  public abstract java.awt.Rectangle getPathBounds(javax.swing.JTree p0, javax.swing.tree.TreePath p1);
  public abstract int getRowForPath(javax.swing.JTree p0, javax.swing.tree.TreePath p1);
  public abstract javax.swing.tree.TreePath getClosestPathForLocation(javax.swing.JTree p0, int p1, int p2);
  public abstract boolean isEditing(javax.swing.JTree p0);
  public abstract boolean stopEditing(javax.swing.JTree p0);
  public abstract void cancelEditing(javax.swing.JTree p0);
  public abstract void startEditingAtPath(javax.swing.JTree p0, javax.swing.tree.TreePath p1);
  public abstract javax.swing.tree.TreePath getEditingPath(javax.swing.JTree p0);
}
