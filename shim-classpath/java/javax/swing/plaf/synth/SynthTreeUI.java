/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.plaf.synth;

public class SynthTreeUI extends javax.swing.plaf.basic.BasicTreeUI implements java.beans.PropertyChangeListener, javax.swing.plaf.synth.SynthUI {
  private javax.swing.Icon bp_ExpandedIcon;
  private javax.swing.Icon bp_CollapsedIcon;
  public SynthTreeUI() {
  }
  public void update(java.awt.Graphics p0, javax.swing.JComponent p1) { javax.swing.plaf.synth.SynthSupport.update(p0, p1, Region.TREE); paint(p0, p1); }
  public javax.swing.plaf.synth.SynthContext getContext(javax.swing.JComponent p0) { return javax.swing.plaf.synth.SynthSupport.context(p0, Region.TREE, javax.swing.plaf.synth.SynthSupport.state(p0)); }
  protected void paintDropLine(java.awt.Graphics p0) { super.paintDropLine(p0); }
  protected void installDefaults() { super.installDefaults(); }
  protected void installListeners() { super.installListeners(); }
  protected void uninstallListeners() { super.uninstallListeners(); }
  protected void uninstallDefaults() { super.uninstallDefaults(); }
  protected javax.swing.tree.TreeCellRenderer createDefaultCellRenderer() { return super.createDefaultCellRenderer(); }
  protected void paintVerticalPartOfLeg(java.awt.Graphics p0, java.awt.Rectangle p1, java.awt.Insets p2, javax.swing.tree.TreePath p3) { super.paintVerticalPartOfLeg(p0, p1, p2, p3); }
  protected void paintHorizontalPartOfLeg(java.awt.Graphics p0, java.awt.Rectangle p1, java.awt.Insets p2, java.awt.Rectangle p3, javax.swing.tree.TreePath p4, int p5, boolean p6, boolean p7, boolean p8) { super.paintHorizontalPartOfLeg(p0, p1, p2, p3, p4, p5, p6, p7, p8); }
  protected void paintExpandControl(java.awt.Graphics p0, java.awt.Rectangle p1, java.awt.Insets p2, java.awt.Rectangle p3, javax.swing.tree.TreePath p4, int p5, boolean p6, boolean p7, boolean p8) { super.paintExpandControl(p0, p1, p2, p3, p4, p5, p6, p7, p8); }
  protected void paintHorizontalLine(java.awt.Graphics p0, javax.swing.JComponent p1, int p2, int p3, int p4) { super.paintHorizontalLine(p0, p1, p2, p3, p4); }
  protected int getRowX(int p0, int p1) { return super.getRowX(p0, p1); }
  protected void paintVerticalLine(java.awt.Graphics p0, javax.swing.JComponent p1, int p2, int p3, int p4) { super.paintVerticalLine(p0, p1, p2, p3, p4); }
  public javax.swing.Icon getExpandedIcon() { return bp_ExpandedIcon; }
  protected void drawCentered(java.awt.Component p0, java.awt.Graphics p1, javax.swing.Icon p2, int p3, int p4) { super.drawCentered(p0, p1, p2, p3, p4); }
  public javax.swing.Icon getCollapsedIcon() { return bp_CollapsedIcon; }
  protected javax.swing.tree.TreeCellEditor createDefaultCellEditor() { return super.createDefaultCellEditor(); }
  public static javax.swing.plaf.ComponentUI createUI(javax.swing.JComponent p0) { return new SynthTreeUI(); }
  public void paint(java.awt.Graphics p0, javax.swing.JComponent p1) { super.paint(p0, p1); }
  protected void paint(javax.swing.plaf.synth.SynthContext p0, java.awt.Graphics p1) {  }
  public void propertyChange(java.beans.PropertyChangeEvent p0) { javax.swing.plaf.synth.SynthSupport.propertyChange(p0, Region.TREE); }
  public void paintBorder(javax.swing.plaf.synth.SynthContext p0, java.awt.Graphics p1, int p2, int p3, int p4, int p5) {  }
  public void installUI(javax.swing.JComponent c) {
    super.installUI(c);
    javax.swing.plaf.synth.SynthSupport.install(c, Region.TREE);
  }
  public void uninstallUI(javax.swing.JComponent c) {
    javax.swing.plaf.synth.SynthSupport.uninstall(c, Region.TREE);
    super.uninstallUI(c);
  }
}
