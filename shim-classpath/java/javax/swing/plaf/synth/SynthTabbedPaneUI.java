/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.plaf.synth;

public class SynthTabbedPaneUI extends javax.swing.plaf.basic.BasicTabbedPaneUI implements java.beans.PropertyChangeListener, javax.swing.plaf.synth.SynthUI {
  public SynthTabbedPaneUI() {
  }
  public void update(java.awt.Graphics p0, javax.swing.JComponent p1) { javax.swing.plaf.synth.SynthSupport.update(p0, p1, Region.TABBED_PANE); paint(p0, p1); }
  public javax.swing.plaf.synth.SynthContext getContext(javax.swing.JComponent p0) { return javax.swing.plaf.synth.SynthSupport.context(p0, Region.TABBED_PANE, javax.swing.plaf.synth.SynthSupport.state(p0)); }
  protected java.awt.LayoutManager createLayoutManager() { return super.createLayoutManager(); }
  protected java.awt.event.MouseListener createMouseListener() { return super.createMouseListener(); }
  protected void installDefaults() { super.installDefaults(); }
  protected void installListeners() { super.installListeners(); }
  protected void uninstallListeners() { super.uninstallListeners(); }
  protected void uninstallDefaults() { super.uninstallDefaults(); }
  protected void setRolloverTab(int p0) { super.setRolloverTab(p0); }
  protected java.awt.Insets getTabInsets(int p0, int p1) { return super.getTabInsets(p0, p1); }
  protected int calculateMaxTabHeight(int p0) { return super.calculateMaxTabHeight(p0); }
  protected void paintTabArea(java.awt.Graphics p0, int p1, int p2) { super.paintTabArea(p0, p1, p2); }
  protected int getTabLabelShiftX(int p0, int p1, boolean p2) { return super.getTabLabelShiftX(p0, p1, p2); }
  protected int getTabLabelShiftY(int p0, int p1, boolean p2) { return super.getTabLabelShiftY(p0, p1, p2); }
  protected int calculateTabWidth(int p0, int p1, java.awt.FontMetrics p2) { return super.calculateTabWidth(p0, p1, p2); }
  protected javax.swing.JButton createScrollButton(int p0) { return super.createScrollButton(p0); }
  protected int calculateMaxTabWidth(int p0) { return super.calculateMaxTabWidth(p0); }
  public static javax.swing.plaf.ComponentUI createUI(javax.swing.JComponent p0) { return new SynthTabbedPaneUI(); }
  protected void paint(javax.swing.plaf.synth.SynthContext p0, java.awt.Graphics p1) {  }
  public void paint(java.awt.Graphics p0, javax.swing.JComponent p1) { super.paint(p0, p1); }
  protected int getBaseline(int p0) { return super.getBaseline(p0); }
  protected java.awt.FontMetrics getFontMetrics() { return super.getFontMetrics(); }
  public void propertyChange(java.beans.PropertyChangeEvent p0) { javax.swing.plaf.synth.SynthSupport.propertyChange(p0, Region.TABBED_PANE); }
  public void paintBorder(javax.swing.plaf.synth.SynthContext p0, java.awt.Graphics p1, int p2, int p3, int p4, int p5) {  }
  public void installUI(javax.swing.JComponent c) {
    super.installUI(c);
    javax.swing.plaf.synth.SynthSupport.install(c, Region.TABBED_PANE);
  }
  public void uninstallUI(javax.swing.JComponent c) {
    javax.swing.plaf.synth.SynthSupport.uninstall(c, Region.TABBED_PANE);
    super.uninstallUI(c);
  }
}
