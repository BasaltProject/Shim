/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.plaf.synth;

public class SynthComboBoxUI extends javax.swing.plaf.basic.BasicComboBoxUI implements java.beans.PropertyChangeListener, javax.swing.plaf.synth.SynthUI {
  public SynthComboBoxUI() {
  }
  public void update(java.awt.Graphics p0, javax.swing.JComponent p1) { javax.swing.plaf.synth.SynthSupport.update(p0, p1, Region.COMBO_BOX); paint(p0, p1); }
  public javax.swing.plaf.synth.SynthContext getContext(javax.swing.JComponent p0) { return javax.swing.plaf.synth.SynthSupport.context(p0, Region.COMBO_BOX, javax.swing.plaf.synth.SynthSupport.state(p0)); }
  protected javax.swing.ComboBoxEditor createEditor() { return super.createEditor(); }
  protected javax.swing.plaf.basic.ComboPopup createPopup() { return super.createPopup(); }
  protected javax.swing.ListCellRenderer<java.lang.Object> createRenderer() { return super.createRenderer(); }
  protected javax.swing.JButton createArrowButton() { return super.createArrowButton(); }
  public void paintCurrentValue(java.awt.Graphics p0, java.awt.Rectangle p1, boolean p2) { super.paintCurrentValue(p0, p1, p2); }
  protected java.awt.Dimension getDefaultSize() { return super.getDefaultSize(); }
  protected void installDefaults() { super.installDefaults(); }
  protected void installListeners() { super.installListeners(); }
  protected void uninstallListeners() { super.uninstallListeners(); }
  protected void uninstallDefaults() { super.uninstallDefaults(); }
  public static javax.swing.plaf.ComponentUI createUI(javax.swing.JComponent p0) { return new SynthComboBoxUI(); }
  public void paint(java.awt.Graphics p0, javax.swing.JComponent p1) { super.paint(p0, p1); }
  protected void paint(javax.swing.plaf.synth.SynthContext p0, java.awt.Graphics p1) {  }
  public java.awt.Dimension getMinimumSize(javax.swing.JComponent p0) { return super.getMinimumSize(p0); }
  public void propertyChange(java.beans.PropertyChangeEvent p0) { javax.swing.plaf.synth.SynthSupport.propertyChange(p0, Region.COMBO_BOX); }
  public void paintBorder(javax.swing.plaf.synth.SynthContext p0, java.awt.Graphics p1, int p2, int p3, int p4, int p5) {  }
  public void installUI(javax.swing.JComponent p0) { super.installUI(p0); javax.swing.plaf.synth.SynthSupport.install(p0, Region.COMBO_BOX); }
  public void uninstallUI(javax.swing.JComponent p0) { javax.swing.plaf.synth.SynthSupport.uninstall(p0, Region.COMBO_BOX); super.uninstallUI(p0); }
}
