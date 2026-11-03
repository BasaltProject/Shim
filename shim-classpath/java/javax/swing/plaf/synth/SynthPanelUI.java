/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.plaf.synth;

public class SynthPanelUI extends javax.swing.plaf.basic.BasicPanelUI implements java.beans.PropertyChangeListener, javax.swing.plaf.synth.SynthUI {
  public SynthPanelUI() {
  }
  public void update(java.awt.Graphics p0, javax.swing.JComponent p1) { javax.swing.plaf.synth.SynthSupport.update(p0, p1, Region.PANEL); paint(p0, p1); }
  public javax.swing.plaf.synth.SynthContext getContext(javax.swing.JComponent p0) { return javax.swing.plaf.synth.SynthSupport.context(p0, Region.PANEL, javax.swing.plaf.synth.SynthSupport.state(p0)); }
  protected void installDefaults(javax.swing.JPanel p0) { super.installDefaults(p0); }
  protected void installListeners(javax.swing.JPanel p0) {  }
  protected void uninstallListeners(javax.swing.JPanel p0) {  }
  protected void uninstallDefaults(javax.swing.JPanel p0) { super.uninstallDefaults(p0); }
  public static javax.swing.plaf.ComponentUI createUI(javax.swing.JComponent p0) { return new SynthPanelUI(); }
  public void paint(java.awt.Graphics p0, javax.swing.JComponent p1) { super.paint(p0, p1); }
  protected void paint(javax.swing.plaf.synth.SynthContext p0, java.awt.Graphics p1) {  }
  public void propertyChange(java.beans.PropertyChangeEvent p0) { javax.swing.plaf.synth.SynthSupport.propertyChange(p0, Region.PANEL); }
  public void paintBorder(javax.swing.plaf.synth.SynthContext p0, java.awt.Graphics p1, int p2, int p3, int p4, int p5) {  }
  public void installUI(javax.swing.JComponent p0) { super.installUI(p0); javax.swing.plaf.synth.SynthSupport.install(p0, Region.PANEL); }
  public void uninstallUI(javax.swing.JComponent p0) { javax.swing.plaf.synth.SynthSupport.uninstall(p0, Region.PANEL); super.uninstallUI(p0); }
}
