/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.plaf.multi;

public class MultiTextUI extends javax.swing.plaf.TextUI {
  protected java.util.Vector<javax.swing.plaf.ComponentUI> uis = new java.util.Vector<javax.swing.plaf.ComponentUI>();

  public MultiTextUI() {
  }

  public void update(java.awt.Graphics p0, javax.swing.JComponent p1) {
    for (int i = 0; i < uis.size(); i++) {
      ((javax.swing.plaf.TextUI) uis.elementAt(i)).update(p0, p1);
    }
  }

  public boolean contains(javax.swing.JComponent p0, int p1, int p2) {
    boolean returnValue = ((javax.swing.plaf.TextUI) uis.elementAt(0)).contains(p0, p1, p2);
    for (int i = 1; i < uis.size(); i++) {
      ((javax.swing.plaf.TextUI) uis.elementAt(i)).contains(p0, p1, p2);
    }
    return returnValue;
  }

  public javax.swing.text.EditorKit getEditorKit(javax.swing.text.JTextComponent p0) {
    javax.swing.text.EditorKit returnValue = ((javax.swing.plaf.TextUI) uis.elementAt(0)).getEditorKit(p0);
    for (int i = 1; i < uis.size(); i++) {
      ((javax.swing.plaf.TextUI) uis.elementAt(i)).getEditorKit(p0);
    }
    return returnValue;
  }

  public int viewToModel(javax.swing.text.JTextComponent p0, java.awt.Point p1, javax.swing.text.Position.Bias[] p2) {
    int returnValue = ((javax.swing.plaf.TextUI) uis.elementAt(0)).viewToModel(p0, p1, p2);
    for (int i = 1; i < uis.size(); i++) {
      ((javax.swing.plaf.TextUI) uis.elementAt(i)).viewToModel(p0, p1, p2);
    }
    return returnValue;
  }

  public int viewToModel(javax.swing.text.JTextComponent p0, java.awt.Point p1) {
    int returnValue = ((javax.swing.plaf.TextUI) uis.elementAt(0)).viewToModel(p0, p1);
    for (int i = 1; i < uis.size(); i++) {
      ((javax.swing.plaf.TextUI) uis.elementAt(i)).viewToModel(p0, p1);
    }
    return returnValue;
  }

  public int viewToModel2D(javax.swing.text.JTextComponent p0, java.awt.geom.Point2D p1, javax.swing.text.Position.Bias[] p2) {
    int returnValue = ((javax.swing.plaf.TextUI) uis.elementAt(0)).viewToModel2D(p0, p1, p2);
    for (int i = 1; i < uis.size(); i++) {
      ((javax.swing.plaf.TextUI) uis.elementAt(i)).viewToModel2D(p0, p1, p2);
    }
    return returnValue;
  }

  public javax.swing.plaf.ComponentUI[] getUIs() {
    return MultiLookAndFeel.uisToArray(uis);
  }

  public void damageRange(javax.swing.text.JTextComponent p0, int p1, int p2) {
    for (int i = 0; i < uis.size(); i++) {
      ((javax.swing.plaf.TextUI) uis.elementAt(i)).damageRange(p0, p1, p2);
    }
  }

  public void damageRange(javax.swing.text.JTextComponent p0, int p1, int p2, javax.swing.text.Position.Bias p3, javax.swing.text.Position.Bias p4) {
    for (int i = 0; i < uis.size(); i++) {
      ((javax.swing.plaf.TextUI) uis.elementAt(i)).damageRange(p0, p1, p2, p3, p4);
    }
  }

  public javax.swing.text.View getRootView(javax.swing.text.JTextComponent p0) {
    javax.swing.text.View returnValue = ((javax.swing.plaf.TextUI) uis.elementAt(0)).getRootView(p0);
    for (int i = 1; i < uis.size(); i++) {
      ((javax.swing.plaf.TextUI) uis.elementAt(i)).getRootView(p0);
    }
    return returnValue;
  }

  public static javax.swing.plaf.ComponentUI createUI(javax.swing.JComponent p0) {
    MultiTextUI mui = new MultiTextUI();
    return MultiLookAndFeel.createUIs(mui, mui.uis, p0);
  }

  public java.awt.Dimension getMaximumSize(javax.swing.JComponent p0) {
    java.awt.Dimension returnValue = ((javax.swing.plaf.TextUI) uis.elementAt(0)).getMaximumSize(p0);
    for (int i = 1; i < uis.size(); i++) {
      ((javax.swing.plaf.TextUI) uis.elementAt(i)).getMaximumSize(p0);
    }
    return returnValue;
  }

  public void paint(java.awt.Graphics p0, javax.swing.JComponent p1) {
    for (int i = 0; i < uis.size(); i++) {
      ((javax.swing.plaf.TextUI) uis.elementAt(i)).paint(p0, p1);
    }
  }

  public int getAccessibleChildrenCount(javax.swing.JComponent p0) {
    int returnValue = ((javax.swing.plaf.TextUI) uis.elementAt(0)).getAccessibleChildrenCount(p0);
    for (int i = 1; i < uis.size(); i++) {
      ((javax.swing.plaf.TextUI) uis.elementAt(i)).getAccessibleChildrenCount(p0);
    }
    return returnValue;
  }

  public javax.accessibility.Accessible getAccessibleChild(javax.swing.JComponent p0, int p1) {
    javax.accessibility.Accessible returnValue = ((javax.swing.plaf.TextUI) uis.elementAt(0)).getAccessibleChild(p0, p1);
    for (int i = 1; i < uis.size(); i++) {
      ((javax.swing.plaf.TextUI) uis.elementAt(i)).getAccessibleChild(p0, p1);
    }
    return returnValue;
  }

  public java.awt.Dimension getPreferredSize(javax.swing.JComponent p0) {
    java.awt.Dimension returnValue = ((javax.swing.plaf.TextUI) uis.elementAt(0)).getPreferredSize(p0);
    for (int i = 1; i < uis.size(); i++) {
      ((javax.swing.plaf.TextUI) uis.elementAt(i)).getPreferredSize(p0);
    }
    return returnValue;
  }

  public java.awt.Dimension getMinimumSize(javax.swing.JComponent p0) {
    java.awt.Dimension returnValue = ((javax.swing.plaf.TextUI) uis.elementAt(0)).getMinimumSize(p0);
    for (int i = 1; i < uis.size(); i++) {
      ((javax.swing.plaf.TextUI) uis.elementAt(i)).getMinimumSize(p0);
    }
    return returnValue;
  }

  public void installUI(javax.swing.JComponent p0) {
    for (int i = 0; i < uis.size(); i++) {
      ((javax.swing.plaf.TextUI) uis.elementAt(i)).installUI(p0);
    }
  }

  public void uninstallUI(javax.swing.JComponent p0) {
    for (int i = 0; i < uis.size(); i++) {
      ((javax.swing.plaf.TextUI) uis.elementAt(i)).uninstallUI(p0);
    }
  }

  public java.lang.String getToolTipText(javax.swing.text.JTextComponent p0, java.awt.Point p1) {
    java.lang.String returnValue = ((javax.swing.plaf.TextUI) uis.elementAt(0)).getToolTipText(p0, p1);
    for (int i = 1; i < uis.size(); i++) {
      ((javax.swing.plaf.TextUI) uis.elementAt(i)).getToolTipText(p0, p1);
    }
    return returnValue;
  }


  public int getNextVisualPositionFrom(javax.swing.text.JTextComponent p0, int p1, javax.swing.text.Position.Bias p2,
      int p3, javax.swing.text.Position.Bias[] p4) throws javax.swing.text.BadLocationException {
    int returnValue = ((javax.swing.plaf.TextUI) uis.elementAt(0)).getNextVisualPositionFrom(p0, p1, p2, p3, p4);
    for (int i = 1; i < uis.size(); i++) {
      ((javax.swing.plaf.TextUI) uis.elementAt(i)).getNextVisualPositionFrom(p0, p1, p2, p3, p4);
    }
    return returnValue;
  }

  public java.awt.Rectangle modelToView(javax.swing.text.JTextComponent p0, int p1) throws javax.swing.text.BadLocationException {
    java.awt.Rectangle returnValue = ((javax.swing.plaf.TextUI) uis.elementAt(0)).modelToView(p0, p1);
    for (int i = 1; i < uis.size(); i++) {
      ((javax.swing.plaf.TextUI) uis.elementAt(i)).modelToView(p0, p1);
    }
    return returnValue;
  }

  public java.awt.Rectangle modelToView(javax.swing.text.JTextComponent p0, int p1, javax.swing.text.Position.Bias p2) throws javax.swing.text.BadLocationException {
    java.awt.Rectangle returnValue = ((javax.swing.plaf.TextUI) uis.elementAt(0)).modelToView(p0, p1, p2);
    for (int i = 1; i < uis.size(); i++) {
      ((javax.swing.plaf.TextUI) uis.elementAt(i)).modelToView(p0, p1, p2);
    }
    return returnValue;
  }

  public java.awt.geom.Rectangle2D modelToView2D(javax.swing.text.JTextComponent p0, int p1, javax.swing.text.Position.Bias p2) throws javax.swing.text.BadLocationException {
    java.awt.geom.Rectangle2D returnValue = ((javax.swing.plaf.TextUI) uis.elementAt(0)).modelToView2D(p0, p1, p2);
    for (int i = 1; i < uis.size(); i++) {
      ((javax.swing.plaf.TextUI) uis.elementAt(i)).modelToView2D(p0, p1, p2);
    }
    return returnValue;
  }
}
