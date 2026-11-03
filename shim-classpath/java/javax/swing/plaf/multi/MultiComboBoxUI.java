/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.plaf.multi;

public class MultiComboBoxUI extends javax.swing.plaf.ComboBoxUI {
  protected java.util.Vector<javax.swing.plaf.ComponentUI> uis = new java.util.Vector<javax.swing.plaf.ComponentUI>();

  public MultiComboBoxUI() {
  }

  public void update(java.awt.Graphics p0, javax.swing.JComponent p1) {
    for (int i = 0; i < uis.size(); i++) {
      ((javax.swing.plaf.ComboBoxUI) uis.elementAt(i)).update(p0, p1);
    }
  }

  public boolean contains(javax.swing.JComponent p0, int p1, int p2) {
    boolean returnValue = ((javax.swing.plaf.ComboBoxUI) uis.elementAt(0)).contains(p0, p1, p2);
    for (int i = 1; i < uis.size(); i++) {
      ((javax.swing.plaf.ComboBoxUI) uis.elementAt(i)).contains(p0, p1, p2);
    }
    return returnValue;
  }

  public void setPopupVisible(javax.swing.JComboBox<?> p0, boolean p1) {
    for (int i = 0; i < uis.size(); i++) {
      ((javax.swing.plaf.ComboBoxUI) uis.elementAt(i)).setPopupVisible(p0, p1);
    }
  }

  public boolean isPopupVisible(javax.swing.JComboBox<?> p0) {
    boolean returnValue = ((javax.swing.plaf.ComboBoxUI) uis.elementAt(0)).isPopupVisible(p0);
    for (int i = 1; i < uis.size(); i++) {
      ((javax.swing.plaf.ComboBoxUI) uis.elementAt(i)).isPopupVisible(p0);
    }
    return returnValue;
  }

  public javax.swing.plaf.ComponentUI[] getUIs() {
    return MultiLookAndFeel.uisToArray(uis);
  }

  public static javax.swing.plaf.ComponentUI createUI(javax.swing.JComponent p0) {
    MultiComboBoxUI mui = new MultiComboBoxUI();
    return MultiLookAndFeel.createUIs(mui, mui.uis, p0);
  }

  public java.awt.Dimension getMaximumSize(javax.swing.JComponent p0) {
    java.awt.Dimension returnValue = ((javax.swing.plaf.ComboBoxUI) uis.elementAt(0)).getMaximumSize(p0);
    for (int i = 1; i < uis.size(); i++) {
      ((javax.swing.plaf.ComboBoxUI) uis.elementAt(i)).getMaximumSize(p0);
    }
    return returnValue;
  }

  public void paint(java.awt.Graphics p0, javax.swing.JComponent p1) {
    for (int i = 0; i < uis.size(); i++) {
      ((javax.swing.plaf.ComboBoxUI) uis.elementAt(i)).paint(p0, p1);
    }
  }

  public int getAccessibleChildrenCount(javax.swing.JComponent p0) {
    int returnValue = ((javax.swing.plaf.ComboBoxUI) uis.elementAt(0)).getAccessibleChildrenCount(p0);
    for (int i = 1; i < uis.size(); i++) {
      ((javax.swing.plaf.ComboBoxUI) uis.elementAt(i)).getAccessibleChildrenCount(p0);
    }
    return returnValue;
  }

  public javax.accessibility.Accessible getAccessibleChild(javax.swing.JComponent p0, int p1) {
    javax.accessibility.Accessible returnValue = ((javax.swing.plaf.ComboBoxUI) uis.elementAt(0)).getAccessibleChild(p0, p1);
    for (int i = 1; i < uis.size(); i++) {
      ((javax.swing.plaf.ComboBoxUI) uis.elementAt(i)).getAccessibleChild(p0, p1);
    }
    return returnValue;
  }

  public java.awt.Dimension getPreferredSize(javax.swing.JComponent p0) {
    java.awt.Dimension returnValue = ((javax.swing.plaf.ComboBoxUI) uis.elementAt(0)).getPreferredSize(p0);
    for (int i = 1; i < uis.size(); i++) {
      ((javax.swing.plaf.ComboBoxUI) uis.elementAt(i)).getPreferredSize(p0);
    }
    return returnValue;
  }

  public java.awt.Dimension getMinimumSize(javax.swing.JComponent p0) {
    java.awt.Dimension returnValue = ((javax.swing.plaf.ComboBoxUI) uis.elementAt(0)).getMinimumSize(p0);
    for (int i = 1; i < uis.size(); i++) {
      ((javax.swing.plaf.ComboBoxUI) uis.elementAt(i)).getMinimumSize(p0);
    }
    return returnValue;
  }

  public boolean isFocusTraversable(javax.swing.JComboBox<?> p0) {
    boolean returnValue = ((javax.swing.plaf.ComboBoxUI) uis.elementAt(0)).isFocusTraversable(p0);
    for (int i = 1; i < uis.size(); i++) {
      ((javax.swing.plaf.ComboBoxUI) uis.elementAt(i)).isFocusTraversable(p0);
    }
    return returnValue;
  }

  public void installUI(javax.swing.JComponent p0) {
    for (int i = 0; i < uis.size(); i++) {
      ((javax.swing.plaf.ComboBoxUI) uis.elementAt(i)).installUI(p0);
    }
  }

  public void uninstallUI(javax.swing.JComponent p0) {
    for (int i = 0; i < uis.size(); i++) {
      ((javax.swing.plaf.ComboBoxUI) uis.elementAt(i)).uninstallUI(p0);
    }
  }

}
