/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.plaf.multi;

public class MultiFileChooserUI extends javax.swing.plaf.FileChooserUI {
  protected java.util.Vector<javax.swing.plaf.ComponentUI> uis = new java.util.Vector<javax.swing.plaf.ComponentUI>();

  public MultiFileChooserUI() {
  }

  public void update(java.awt.Graphics p0, javax.swing.JComponent p1) {
    for (int i = 0; i < uis.size(); i++) {
      ((javax.swing.plaf.FileChooserUI) uis.elementAt(i)).update(p0, p1);
    }
  }

  public boolean contains(javax.swing.JComponent p0, int p1, int p2) {
    boolean returnValue = ((javax.swing.plaf.FileChooserUI) uis.elementAt(0)).contains(p0, p1, p2);
    for (int i = 1; i < uis.size(); i++) {
      ((javax.swing.plaf.FileChooserUI) uis.elementAt(i)).contains(p0, p1, p2);
    }
    return returnValue;
  }

  public javax.swing.filechooser.FileFilter getAcceptAllFileFilter(javax.swing.JFileChooser p0) {
    javax.swing.filechooser.FileFilter returnValue = ((javax.swing.plaf.FileChooserUI) uis.elementAt(0)).getAcceptAllFileFilter(p0);
    for (int i = 1; i < uis.size(); i++) {
      ((javax.swing.plaf.FileChooserUI) uis.elementAt(i)).getAcceptAllFileFilter(p0);
    }
    return returnValue;
  }

  public void ensureFileIsVisible(javax.swing.JFileChooser p0, java.io.File p1) {
    for (int i = 0; i < uis.size(); i++) {
      ((javax.swing.plaf.FileChooserUI) uis.elementAt(i)).ensureFileIsVisible(p0, p1);
    }
  }

  public void rescanCurrentDirectory(javax.swing.JFileChooser p0) {
    for (int i = 0; i < uis.size(); i++) {
      ((javax.swing.plaf.FileChooserUI) uis.elementAt(i)).rescanCurrentDirectory(p0);
    }
  }

  public java.lang.String getDialogTitle(javax.swing.JFileChooser p0) {
    java.lang.String returnValue = ((javax.swing.plaf.FileChooserUI) uis.elementAt(0)).getDialogTitle(p0);
    for (int i = 1; i < uis.size(); i++) {
      ((javax.swing.plaf.FileChooserUI) uis.elementAt(i)).getDialogTitle(p0);
    }
    return returnValue;
  }

  public javax.swing.filechooser.FileView getFileView(javax.swing.JFileChooser p0) {
    javax.swing.filechooser.FileView returnValue = ((javax.swing.plaf.FileChooserUI) uis.elementAt(0)).getFileView(p0);
    for (int i = 1; i < uis.size(); i++) {
      ((javax.swing.plaf.FileChooserUI) uis.elementAt(i)).getFileView(p0);
    }
    return returnValue;
  }

  public java.lang.String getApproveButtonText(javax.swing.JFileChooser p0) {
    java.lang.String returnValue = ((javax.swing.plaf.FileChooserUI) uis.elementAt(0)).getApproveButtonText(p0);
    for (int i = 1; i < uis.size(); i++) {
      ((javax.swing.plaf.FileChooserUI) uis.elementAt(i)).getApproveButtonText(p0);
    }
    return returnValue;
  }

  public javax.swing.plaf.ComponentUI[] getUIs() {
    return MultiLookAndFeel.uisToArray(uis);
  }

  public static javax.swing.plaf.ComponentUI createUI(javax.swing.JComponent p0) {
    MultiFileChooserUI mui = new MultiFileChooserUI();
    return MultiLookAndFeel.createUIs(mui, mui.uis, p0);
  }

  public java.awt.Dimension getMaximumSize(javax.swing.JComponent p0) {
    java.awt.Dimension returnValue = ((javax.swing.plaf.FileChooserUI) uis.elementAt(0)).getMaximumSize(p0);
    for (int i = 1; i < uis.size(); i++) {
      ((javax.swing.plaf.FileChooserUI) uis.elementAt(i)).getMaximumSize(p0);
    }
    return returnValue;
  }

  public void paint(java.awt.Graphics p0, javax.swing.JComponent p1) {
    for (int i = 0; i < uis.size(); i++) {
      ((javax.swing.plaf.FileChooserUI) uis.elementAt(i)).paint(p0, p1);
    }
  }

  public int getAccessibleChildrenCount(javax.swing.JComponent p0) {
    int returnValue = ((javax.swing.plaf.FileChooserUI) uis.elementAt(0)).getAccessibleChildrenCount(p0);
    for (int i = 1; i < uis.size(); i++) {
      ((javax.swing.plaf.FileChooserUI) uis.elementAt(i)).getAccessibleChildrenCount(p0);
    }
    return returnValue;
  }

  public javax.accessibility.Accessible getAccessibleChild(javax.swing.JComponent p0, int p1) {
    javax.accessibility.Accessible returnValue = ((javax.swing.plaf.FileChooserUI) uis.elementAt(0)).getAccessibleChild(p0, p1);
    for (int i = 1; i < uis.size(); i++) {
      ((javax.swing.plaf.FileChooserUI) uis.elementAt(i)).getAccessibleChild(p0, p1);
    }
    return returnValue;
  }

  public java.awt.Dimension getPreferredSize(javax.swing.JComponent p0) {
    java.awt.Dimension returnValue = ((javax.swing.plaf.FileChooserUI) uis.elementAt(0)).getPreferredSize(p0);
    for (int i = 1; i < uis.size(); i++) {
      ((javax.swing.plaf.FileChooserUI) uis.elementAt(i)).getPreferredSize(p0);
    }
    return returnValue;
  }

  public java.awt.Dimension getMinimumSize(javax.swing.JComponent p0) {
    java.awt.Dimension returnValue = ((javax.swing.plaf.FileChooserUI) uis.elementAt(0)).getMinimumSize(p0);
    for (int i = 1; i < uis.size(); i++) {
      ((javax.swing.plaf.FileChooserUI) uis.elementAt(i)).getMinimumSize(p0);
    }
    return returnValue;
  }

  public void installUI(javax.swing.JComponent p0) {
    for (int i = 0; i < uis.size(); i++) {
      ((javax.swing.plaf.FileChooserUI) uis.elementAt(i)).installUI(p0);
    }
  }

  public void uninstallUI(javax.swing.JComponent p0) {
    for (int i = 0; i < uis.size(); i++) {
      ((javax.swing.plaf.FileChooserUI) uis.elementAt(i)).uninstallUI(p0);
    }
  }

}
