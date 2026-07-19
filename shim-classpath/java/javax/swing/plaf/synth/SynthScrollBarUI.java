/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.plaf.synth;

public class SynthScrollBarUI extends javax.swing.plaf.basic.BasicScrollBarUI implements java.beans.PropertyChangeListener, javax.swing.plaf.synth.SynthUI {
  private boolean bp_SupportsAbsolutePositioning;
  public SynthScrollBarUI() {
  }
  public void update(java.awt.Graphics p0, javax.swing.JComponent p1) { javax.swing.plaf.synth.SynthSupport.update(p0, p1, Region.SCROLL_BAR); paint(p0, p1); }
  public javax.swing.plaf.synth.SynthContext getContext(javax.swing.JComponent p0) { return javax.swing.plaf.synth.SynthSupport.context(p0, Region.SCROLL_BAR, javax.swing.plaf.synth.SynthSupport.state(p0)); }
  protected void paintTrack(javax.swing.plaf.synth.SynthContext p0, java.awt.Graphics p1, java.awt.Rectangle p2) {  }
  protected void installDefaults() { super.installDefaults(); }
  protected void installListeners() { super.installListeners(); }
  protected void uninstallListeners() { super.uninstallListeners(); }
  protected void uninstallDefaults() { super.uninstallDefaults(); }
  protected void configureScrollBarColors() { super.configureScrollBarColors(); }
  protected javax.swing.JButton createIncreaseButton(int p0) { return super.createIncreaseButton(p0); }
  protected javax.swing.JButton createDecreaseButton(int p0) { return super.createDecreaseButton(p0); }
  protected void setThumbRollover(boolean p0) { super.setThumbRollover(p0); }
  protected void paintThumb(javax.swing.plaf.synth.SynthContext p0, java.awt.Graphics p1, java.awt.Rectangle p2) {  }
  protected java.awt.Dimension getMinimumThumbSize() { return super.getMinimumThumbSize(); }
  public boolean getSupportsAbsolutePositioning() { return bp_SupportsAbsolutePositioning; }
  public static javax.swing.plaf.ComponentUI createUI(javax.swing.JComponent p0) { return new SynthScrollBarUI(); }
  public void paint(java.awt.Graphics p0, javax.swing.JComponent p1) { super.paint(p0, p1); }
  protected void paint(javax.swing.plaf.synth.SynthContext p0, java.awt.Graphics p1) {  }
  public java.awt.Dimension getPreferredSize(javax.swing.JComponent p0) { return super.getPreferredSize(p0); }
  public void propertyChange(java.beans.PropertyChangeEvent p0) { javax.swing.plaf.synth.SynthSupport.propertyChange(p0, Region.SCROLL_BAR); }
  public void paintBorder(javax.swing.plaf.synth.SynthContext p0, java.awt.Graphics p1, int p2, int p3, int p4, int p5) {  }
  public void installUI(javax.swing.JComponent c) {
    super.installUI(c);
    javax.swing.plaf.synth.SynthSupport.install(c, Region.SCROLL_BAR);
  }
  public void uninstallUI(javax.swing.JComponent c) {
    javax.swing.plaf.synth.SynthSupport.uninstall(c, Region.SCROLL_BAR);
    super.uninstallUI(c);
  }
}
