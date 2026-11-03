/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.plaf.synth;

public class SynthProgressBarUI extends javax.swing.plaf.basic.BasicProgressBarUI implements javax.swing.plaf.synth.SynthUI, java.beans.PropertyChangeListener {
  public SynthProgressBarUI() {
  }
  public void update(java.awt.Graphics p0, javax.swing.JComponent p1) { javax.swing.plaf.synth.SynthSupport.update(p0, p1, Region.PROGRESS_BAR); paint(p0, p1); }
  public javax.swing.plaf.synth.SynthContext getContext(javax.swing.JComponent p0) { return javax.swing.plaf.synth.SynthSupport.context(p0, Region.PROGRESS_BAR, javax.swing.plaf.synth.SynthSupport.state(p0)); }
  protected void installDefaults() { super.installDefaults(); }
  protected void installListeners() { super.installListeners(); }
  protected void uninstallListeners() { super.uninstallListeners(); }
  protected void uninstallDefaults() { super.uninstallDefaults(); }
  protected java.awt.Rectangle getBox(java.awt.Rectangle p0) { return super.getBox(p0); }
  protected void setAnimationIndex(int p0) { super.setAnimationIndex(p0); }
  protected void paintText(javax.swing.plaf.synth.SynthContext p0, java.awt.Graphics p1, java.lang.String p2) {  }
  public static javax.swing.plaf.ComponentUI createUI(javax.swing.JComponent p0) { return new SynthProgressBarUI(); }
  protected void paint(javax.swing.plaf.synth.SynthContext p0, java.awt.Graphics p1) {  }
  public void paint(java.awt.Graphics p0, javax.swing.JComponent p1) { super.paint(p0, p1); }
  public java.awt.Dimension getPreferredSize(javax.swing.JComponent p0) { return super.getPreferredSize(p0); }
  public int getBaseline(javax.swing.JComponent p0, int p1, int p2) { return super.getBaseline(p0, p1, p2); }
  public void propertyChange(java.beans.PropertyChangeEvent p0) { javax.swing.plaf.synth.SynthSupport.propertyChange(p0, Region.PROGRESS_BAR); }
  public void paintBorder(javax.swing.plaf.synth.SynthContext p0, java.awt.Graphics p1, int p2, int p3, int p4, int p5) {  }
  public void installUI(javax.swing.JComponent c) {
    super.installUI(c);
    javax.swing.plaf.synth.SynthSupport.install(c, Region.PROGRESS_BAR);
  }
  public void uninstallUI(javax.swing.JComponent c) {
    javax.swing.plaf.synth.SynthSupport.uninstall(c, Region.PROGRESS_BAR);
    super.uninstallUI(c);
  }
}
