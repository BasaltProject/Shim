/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing;

public class JToolTip extends javax.swing.JComponent implements javax.accessibility.Accessible {
  private javax.swing.JComponent bp_Component;
  private java.lang.String bp_TipText;
  private javax.accessibility.AccessibleContext bp_AccessibleContext;
  public JToolTip() {
    setOpaque(true);
    updateUI();
  }
  protected java.lang.String paramString() {
    String tipTextString = (getTipText() != null ? getTipText() : "");
    return super.paramString() + ",tipText=" + tipTextString;
  }
  public javax.swing.JComponent getComponent() { return bp_Component; }
  public void setTipText(java.lang.String p0) { java.lang.String old = bp_TipText; bp_TipText = p0; firePropertyChange("tipText", old, p0); }
  public java.lang.String getTipText() { return bp_TipText; }
  public javax.accessibility.AccessibleContext getAccessibleContext() { return bp_AccessibleContext; }
  public void setComponent(javax.swing.JComponent p0) { javax.swing.JComponent old = bp_Component; bp_Component = p0; firePropertyChange("component", old, p0); }
  public void updateUI() {
    setUI((javax.swing.plaf.ComponentUI) UIManager.getUI(this));
  }
  public javax.swing.plaf.ToolTipUI getUI() { return (javax.swing.plaf.ToolTipUI) ui; }
  public java.lang.String getUIClassID() { return "ToolTipUI"; }
}
