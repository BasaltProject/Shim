/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.plaf.synth;

public class SynthSeparatorUI extends javax.swing.plaf.SeparatorUI implements java.beans.PropertyChangeListener, javax.swing.plaf.synth.SynthUI {
  public SynthSeparatorUI() {
  }
  public void update(java.awt.Graphics p0, javax.swing.JComponent p1) { javax.swing.plaf.synth.SynthSupport.update(p0, p1, Region.SEPARATOR); paint(p0, p1); }
  public javax.swing.plaf.synth.SynthContext getContext(javax.swing.JComponent p0) { return javax.swing.plaf.synth.SynthSupport.context(p0, Region.SEPARATOR, javax.swing.plaf.synth.SynthSupport.state(p0)); }
  public void installDefaults(javax.swing.JSeparator p0) {  }
  public void installListeners(javax.swing.JSeparator p0) {  }
  public void uninstallListeners(javax.swing.JSeparator p0) {  }
  public void uninstallDefaults(javax.swing.JSeparator p0) {  }
  public static javax.swing.plaf.ComponentUI createUI(javax.swing.JComponent p0) { return new SynthSeparatorUI(); }
  public java.awt.Dimension getMaximumSize(javax.swing.JComponent p0) { return super.getMaximumSize(p0); }
  protected void paint(javax.swing.plaf.synth.SynthContext p0, java.awt.Graphics p1) {  }
  public void paint(java.awt.Graphics p0, javax.swing.JComponent p1) { super.paint(p0, p1); }
  public java.awt.Dimension getPreferredSize(javax.swing.JComponent p0) { return super.getPreferredSize(p0); }
  public java.awt.Dimension getMinimumSize(javax.swing.JComponent p0) { return super.getMinimumSize(p0); }
  public void propertyChange(java.beans.PropertyChangeEvent p0) { javax.swing.plaf.synth.SynthSupport.propertyChange(p0, Region.SEPARATOR); }
  public void paintBorder(javax.swing.plaf.synth.SynthContext p0, java.awt.Graphics p1, int p2, int p3, int p4, int p5) {  }
  public void installUI(javax.swing.JComponent p0) { super.installUI(p0); javax.swing.plaf.synth.SynthSupport.install(p0, Region.SEPARATOR); }
  public void uninstallUI(javax.swing.JComponent p0) { javax.swing.plaf.synth.SynthSupport.uninstall(p0, Region.SEPARATOR); super.uninstallUI(p0); }
}
