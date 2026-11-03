/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.plaf.synth;

public class SynthColorChooserUI extends javax.swing.plaf.basic.BasicColorChooserUI implements java.beans.PropertyChangeListener, javax.swing.plaf.synth.SynthUI {
  public SynthColorChooserUI() {
  }
  public void update(java.awt.Graphics p0, javax.swing.JComponent p1) { javax.swing.plaf.synth.SynthSupport.update(p0, p1, Region.COLOR_CHOOSER); paint(p0, p1); }
  public javax.swing.plaf.synth.SynthContext getContext(javax.swing.JComponent p0) { return javax.swing.plaf.synth.SynthSupport.context(p0, Region.COLOR_CHOOSER, javax.swing.plaf.synth.SynthSupport.state(p0)); }
  protected void installDefaults() { super.installDefaults(); }
  protected void installListeners() { super.installListeners(); }
  protected void uninstallListeners() { super.uninstallListeners(); }
  protected void uninstallDefaults() { super.uninstallDefaults(); }
  protected javax.swing.colorchooser.AbstractColorChooserPanel[] createDefaultChoosers() { return super.createDefaultChoosers(); }
  public static javax.swing.plaf.ComponentUI createUI(javax.swing.JComponent p0) { return new SynthColorChooserUI(); }
  protected void paint(javax.swing.plaf.synth.SynthContext p0, java.awt.Graphics p1) {  }
  public void paint(java.awt.Graphics p0, javax.swing.JComponent p1) { super.paint(p0, p1); }
  public void propertyChange(java.beans.PropertyChangeEvent p0) { javax.swing.plaf.synth.SynthSupport.propertyChange(p0, Region.COLOR_CHOOSER); }
  public void paintBorder(javax.swing.plaf.synth.SynthContext p0, java.awt.Graphics p1, int p2, int p3, int p4, int p5) {  }
  public void installUI(javax.swing.JComponent c) {
    super.installUI(c);
    javax.swing.plaf.synth.SynthSupport.install(c, Region.COLOR_CHOOSER);
  }
  public void uninstallUI(javax.swing.JComponent c) {
    javax.swing.plaf.synth.SynthSupport.uninstall(c, Region.COLOR_CHOOSER);
    super.uninstallUI(c);
  }
}
