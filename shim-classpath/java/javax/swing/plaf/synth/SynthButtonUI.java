/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.plaf.synth;

public class SynthButtonUI extends javax.swing.plaf.basic.BasicButtonUI implements java.beans.PropertyChangeListener, javax.swing.plaf.synth.SynthUI {
  public SynthButtonUI() {
  }
  public void update(java.awt.Graphics p0, javax.swing.JComponent p1) { javax.swing.plaf.synth.SynthSupport.update(p0, p1, Region.BUTTON); paint(p0, p1); }
  public javax.swing.plaf.synth.SynthContext getContext(javax.swing.JComponent p0) { return javax.swing.plaf.synth.SynthSupport.context(p0, Region.BUTTON, javax.swing.plaf.synth.SynthSupport.state(p0)); }
  protected javax.swing.Icon getIcon(javax.swing.AbstractButton p0) { return null; }
  protected void installDefaults(javax.swing.AbstractButton p0) { super.installDefaults(p0); }
  protected void installListeners(javax.swing.AbstractButton p0) { super.installListeners(p0); }
  protected void uninstallListeners(javax.swing.AbstractButton p0) { super.uninstallListeners(p0); }
  protected void uninstallDefaults(javax.swing.AbstractButton p0) { super.uninstallDefaults(p0); }
  protected javax.swing.Icon getDefaultIcon(javax.swing.AbstractButton p0) { return null; }
  protected javax.swing.Icon getSizingIcon(javax.swing.AbstractButton p0) { return null; }
  public static javax.swing.plaf.ComponentUI createUI(javax.swing.JComponent p0) { return new SynthButtonUI(); }
  public java.awt.Dimension getMaximumSize(javax.swing.JComponent p0) { return super.getMaximumSize(p0); }
  protected void paint(javax.swing.plaf.synth.SynthContext p0, java.awt.Graphics p1) {  }
  public void paint(java.awt.Graphics p0, javax.swing.JComponent p1) { super.paint(p0, p1); }
  public java.awt.Dimension getPreferredSize(javax.swing.JComponent p0) { return super.getPreferredSize(p0); }
  public java.awt.Dimension getMinimumSize(javax.swing.JComponent p0) { return super.getMinimumSize(p0); }
  public int getBaseline(javax.swing.JComponent p0, int p1, int p2) { return super.getBaseline(p0, p1, p2); }
  public void propertyChange(java.beans.PropertyChangeEvent p0) { javax.swing.plaf.synth.SynthSupport.propertyChange(p0, Region.BUTTON); }
  public void paintBorder(javax.swing.plaf.synth.SynthContext p0, java.awt.Graphics p1, int p2, int p3, int p4, int p5) {  }
  public void installUI(javax.swing.JComponent c) {
    super.installUI(c);
    javax.swing.plaf.synth.SynthSupport.install(c, Region.BUTTON);
  }
  public void uninstallUI(javax.swing.JComponent c) {
    javax.swing.plaf.synth.SynthSupport.uninstall(c, Region.BUTTON);
    super.uninstallUI(c);
  }
}
