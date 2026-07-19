/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.plaf.synth;

public class SynthSpinnerUI extends javax.swing.plaf.basic.BasicSpinnerUI implements java.beans.PropertyChangeListener, javax.swing.plaf.synth.SynthUI {
  public SynthSpinnerUI() {
  }
  public void update(java.awt.Graphics p0, javax.swing.JComponent p1) { javax.swing.plaf.synth.SynthSupport.update(p0, p1, Region.SPINNER); paint(p0, p1); }
  public javax.swing.plaf.synth.SynthContext getContext(javax.swing.JComponent p0) { return javax.swing.plaf.synth.SynthSupport.context(p0, Region.SPINNER, javax.swing.plaf.synth.SynthSupport.state(p0)); }
  protected javax.swing.JComponent createEditor() { return super.createEditor(); }
  protected java.awt.LayoutManager createLayout() { return super.createLayout(); }
  protected void installDefaults() { super.installDefaults(); }
  protected void installListeners() { super.installListeners(); }
  protected void uninstallListeners() { super.uninstallListeners(); }
  protected void uninstallDefaults() { super.uninstallDefaults(); }
  protected java.awt.Component createNextButton() { return super.createNextButton(); }
  protected java.awt.Component createPreviousButton() { return super.createPreviousButton(); }
  protected void replaceEditor(javax.swing.JComponent p0, javax.swing.JComponent p1) { super.replaceEditor(p0, p1); }
  public static javax.swing.plaf.ComponentUI createUI(javax.swing.JComponent p0) { return new SynthSpinnerUI(); }
  protected void paint(javax.swing.plaf.synth.SynthContext p0, java.awt.Graphics p1) {  }
  public void paint(java.awt.Graphics p0, javax.swing.JComponent p1) { super.paint(p0, p1); }
  public void propertyChange(java.beans.PropertyChangeEvent p0) { javax.swing.plaf.synth.SynthSupport.propertyChange(p0, Region.SPINNER); }
  public void paintBorder(javax.swing.plaf.synth.SynthContext p0, java.awt.Graphics p1, int p2, int p3, int p4, int p5) {  }
  public void installUI(javax.swing.JComponent c) {
    super.installUI(c);
    javax.swing.plaf.synth.SynthSupport.install(c, Region.SPINNER);
  }
  public void uninstallUI(javax.swing.JComponent c) {
    javax.swing.plaf.synth.SynthSupport.uninstall(c, Region.SPINNER);
    super.uninstallUI(c);
  }
}
