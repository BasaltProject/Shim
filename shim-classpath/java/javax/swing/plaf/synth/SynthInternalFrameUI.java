/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.plaf.synth;

public class SynthInternalFrameUI extends javax.swing.plaf.basic.BasicInternalFrameUI implements javax.swing.plaf.synth.SynthUI, java.beans.PropertyChangeListener {
  protected SynthInternalFrameUI(javax.swing.JInternalFrame p0) {
    super(p0);
  }
  public void update(java.awt.Graphics p0, javax.swing.JComponent p1) { javax.swing.plaf.synth.SynthSupport.update(p0, p1, Region.INTERNAL_FRAME); paint(p0, p1); }
  public javax.swing.plaf.synth.SynthContext getContext(javax.swing.JComponent p0) { return javax.swing.plaf.synth.SynthSupport.context(p0, Region.INTERNAL_FRAME, javax.swing.plaf.synth.SynthSupport.state(p0)); }
  protected javax.swing.JComponent createNorthPane(javax.swing.JInternalFrame p0) { return super.createNorthPane(p0); }
  protected java.awt.event.ComponentListener createComponentListener() { return super.createComponentListener(); }
  public void installDefaults() { super.installDefaults(); }
  protected void installListeners() { super.installListeners(); }
  protected void uninstallListeners() { super.uninstallListeners(); }
  protected void uninstallDefaults() { super.uninstallDefaults(); }
  protected void uninstallComponents() { super.uninstallComponents(); }
  public static javax.swing.plaf.ComponentUI createUI(javax.swing.JComponent p0) { return new SynthInternalFrameUI((javax.swing.JInternalFrame) p0); }
  public void paint(java.awt.Graphics p0, javax.swing.JComponent p1) { super.paint(p0, p1); }
  protected void paint(javax.swing.plaf.synth.SynthContext p0, java.awt.Graphics p1) {  }
  public void propertyChange(java.beans.PropertyChangeEvent p0) { javax.swing.plaf.synth.SynthSupport.propertyChange(p0, Region.INTERNAL_FRAME); }
  public void paintBorder(javax.swing.plaf.synth.SynthContext p0, java.awt.Graphics p1, int p2, int p3, int p4, int p5) {  }
  public void installUI(javax.swing.JComponent c) {
    super.installUI(c);
    javax.swing.plaf.synth.SynthSupport.install(c, Region.INTERNAL_FRAME);
  }
  public void uninstallUI(javax.swing.JComponent c) {
    javax.swing.plaf.synth.SynthSupport.uninstall(c, Region.INTERNAL_FRAME);
    super.uninstallUI(c);
  }
}
