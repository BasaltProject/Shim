/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.plaf.multi;

public class MultiTreeUI extends javax.swing.plaf.TreeUI {
  protected java.util.Vector<javax.swing.plaf.ComponentUI> uis = new java.util.Vector<javax.swing.plaf.ComponentUI>();

  public MultiTreeUI() {
  }

  public void update(java.awt.Graphics p0, javax.swing.JComponent p1) {
    for (int i = 0; i < uis.size(); i++) {
      ((javax.swing.plaf.TreeUI) uis.elementAt(i)).update(p0, p1);
    }
  }

  public boolean contains(javax.swing.JComponent p0, int p1, int p2) {
    boolean returnValue = ((javax.swing.plaf.TreeUI) uis.elementAt(0)).contains(p0, p1, p2);
    for (int i = 1; i < uis.size(); i++) {
      ((javax.swing.plaf.TreeUI) uis.elementAt(i)).contains(p0, p1, p2);
    }
    return returnValue;
  }

  public javax.swing.tree.TreePath getPathForRow(javax.swing.JTree p0, int p1) {
    javax.swing.tree.TreePath returnValue = ((javax.swing.plaf.TreeUI) uis.elementAt(0)).getPathForRow(p0, p1);
    for (int i = 1; i < uis.size(); i++) {
      ((javax.swing.plaf.TreeUI) uis.elementAt(i)).getPathForRow(p0, p1);
    }
    return returnValue;
  }

  public int getRowCount(javax.swing.JTree p0) {
    int returnValue = ((javax.swing.plaf.TreeUI) uis.elementAt(0)).getRowCount(p0);
    for (int i = 1; i < uis.size(); i++) {
      ((javax.swing.plaf.TreeUI) uis.elementAt(i)).getRowCount(p0);
    }
    return returnValue;
  }

  public java.awt.Rectangle getPathBounds(javax.swing.JTree p0, javax.swing.tree.TreePath p1) {
    java.awt.Rectangle returnValue = ((javax.swing.plaf.TreeUI) uis.elementAt(0)).getPathBounds(p0, p1);
    for (int i = 1; i < uis.size(); i++) {
      ((javax.swing.plaf.TreeUI) uis.elementAt(i)).getPathBounds(p0, p1);
    }
    return returnValue;
  }

  public int getRowForPath(javax.swing.JTree p0, javax.swing.tree.TreePath p1) {
    int returnValue = ((javax.swing.plaf.TreeUI) uis.elementAt(0)).getRowForPath(p0, p1);
    for (int i = 1; i < uis.size(); i++) {
      ((javax.swing.plaf.TreeUI) uis.elementAt(i)).getRowForPath(p0, p1);
    }
    return returnValue;
  }

  public javax.swing.tree.TreePath getClosestPathForLocation(javax.swing.JTree p0, int p1, int p2) {
    javax.swing.tree.TreePath returnValue = ((javax.swing.plaf.TreeUI) uis.elementAt(0)).getClosestPathForLocation(p0, p1, p2);
    for (int i = 1; i < uis.size(); i++) {
      ((javax.swing.plaf.TreeUI) uis.elementAt(i)).getClosestPathForLocation(p0, p1, p2);
    }
    return returnValue;
  }

  public boolean isEditing(javax.swing.JTree p0) {
    boolean returnValue = ((javax.swing.plaf.TreeUI) uis.elementAt(0)).isEditing(p0);
    for (int i = 1; i < uis.size(); i++) {
      ((javax.swing.plaf.TreeUI) uis.elementAt(i)).isEditing(p0);
    }
    return returnValue;
  }

  public boolean stopEditing(javax.swing.JTree p0) {
    boolean returnValue = ((javax.swing.plaf.TreeUI) uis.elementAt(0)).stopEditing(p0);
    for (int i = 1; i < uis.size(); i++) {
      ((javax.swing.plaf.TreeUI) uis.elementAt(i)).stopEditing(p0);
    }
    return returnValue;
  }

  public void cancelEditing(javax.swing.JTree p0) {
    for (int i = 0; i < uis.size(); i++) {
      ((javax.swing.plaf.TreeUI) uis.elementAt(i)).cancelEditing(p0);
    }
  }

  public void startEditingAtPath(javax.swing.JTree p0, javax.swing.tree.TreePath p1) {
    for (int i = 0; i < uis.size(); i++) {
      ((javax.swing.plaf.TreeUI) uis.elementAt(i)).startEditingAtPath(p0, p1);
    }
  }

  public javax.swing.tree.TreePath getEditingPath(javax.swing.JTree p0) {
    javax.swing.tree.TreePath returnValue = ((javax.swing.plaf.TreeUI) uis.elementAt(0)).getEditingPath(p0);
    for (int i = 1; i < uis.size(); i++) {
      ((javax.swing.plaf.TreeUI) uis.elementAt(i)).getEditingPath(p0);
    }
    return returnValue;
  }

  public javax.swing.plaf.ComponentUI[] getUIs() {
    return MultiLookAndFeel.uisToArray(uis);
  }

  public static javax.swing.plaf.ComponentUI createUI(javax.swing.JComponent p0) {
    MultiTreeUI mui = new MultiTreeUI();
    return MultiLookAndFeel.createUIs(mui, mui.uis, p0);
  }

  public java.awt.Dimension getMaximumSize(javax.swing.JComponent p0) {
    java.awt.Dimension returnValue = ((javax.swing.plaf.TreeUI) uis.elementAt(0)).getMaximumSize(p0);
    for (int i = 1; i < uis.size(); i++) {
      ((javax.swing.plaf.TreeUI) uis.elementAt(i)).getMaximumSize(p0);
    }
    return returnValue;
  }

  public void paint(java.awt.Graphics p0, javax.swing.JComponent p1) {
    for (int i = 0; i < uis.size(); i++) {
      ((javax.swing.plaf.TreeUI) uis.elementAt(i)).paint(p0, p1);
    }
  }

  public int getAccessibleChildrenCount(javax.swing.JComponent p0) {
    int returnValue = ((javax.swing.plaf.TreeUI) uis.elementAt(0)).getAccessibleChildrenCount(p0);
    for (int i = 1; i < uis.size(); i++) {
      ((javax.swing.plaf.TreeUI) uis.elementAt(i)).getAccessibleChildrenCount(p0);
    }
    return returnValue;
  }

  public javax.accessibility.Accessible getAccessibleChild(javax.swing.JComponent p0, int p1) {
    javax.accessibility.Accessible returnValue = ((javax.swing.plaf.TreeUI) uis.elementAt(0)).getAccessibleChild(p0, p1);
    for (int i = 1; i < uis.size(); i++) {
      ((javax.swing.plaf.TreeUI) uis.elementAt(i)).getAccessibleChild(p0, p1);
    }
    return returnValue;
  }

  public java.awt.Dimension getPreferredSize(javax.swing.JComponent p0) {
    java.awt.Dimension returnValue = ((javax.swing.plaf.TreeUI) uis.elementAt(0)).getPreferredSize(p0);
    for (int i = 1; i < uis.size(); i++) {
      ((javax.swing.plaf.TreeUI) uis.elementAt(i)).getPreferredSize(p0);
    }
    return returnValue;
  }

  public java.awt.Dimension getMinimumSize(javax.swing.JComponent p0) {
    java.awt.Dimension returnValue = ((javax.swing.plaf.TreeUI) uis.elementAt(0)).getMinimumSize(p0);
    for (int i = 1; i < uis.size(); i++) {
      ((javax.swing.plaf.TreeUI) uis.elementAt(i)).getMinimumSize(p0);
    }
    return returnValue;
  }

  public void installUI(javax.swing.JComponent p0) {
    for (int i = 0; i < uis.size(); i++) {
      ((javax.swing.plaf.TreeUI) uis.elementAt(i)).installUI(p0);
    }
  }

  public void uninstallUI(javax.swing.JComponent p0) {
    for (int i = 0; i < uis.size(); i++) {
      ((javax.swing.plaf.TreeUI) uis.elementAt(i)).uninstallUI(p0);
    }
  }

}
