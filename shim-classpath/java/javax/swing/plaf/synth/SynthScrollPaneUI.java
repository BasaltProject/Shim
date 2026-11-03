/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.plaf.synth;

public class SynthScrollPaneUI extends javax.swing.plaf.basic.BasicScrollPaneUI implements java.beans.PropertyChangeListener, javax.swing.plaf.synth.SynthUI {
  public SynthScrollPaneUI() {
  }
  public void update(java.awt.Graphics p0, javax.swing.JComponent p1) { javax.swing.plaf.synth.SynthSupport.update(p0, p1, Region.SCROLL_PANE); paint(p0, p1); }
  public javax.swing.plaf.synth.SynthContext getContext(javax.swing.JComponent p0) { return javax.swing.plaf.synth.SynthSupport.context(p0, Region.SCROLL_PANE, javax.swing.plaf.synth.SynthSupport.state(p0)); }
  protected void installDefaults(javax.swing.JScrollPane p0) { super.installDefaults(p0); }
  protected void installListeners(javax.swing.JScrollPane p0) { super.installListeners(p0); }
  protected void uninstallListeners(javax.swing.JComponent p0) { super.uninstallListeners(p0); }
  protected void uninstallDefaults(javax.swing.JScrollPane p0) { super.uninstallDefaults(p0); }
  public static javax.swing.plaf.ComponentUI createUI(javax.swing.JComponent p0) { return new SynthScrollPaneUI(); }
  public void paint(java.awt.Graphics p0, javax.swing.JComponent p1) { super.paint(p0, p1); }
  protected void paint(javax.swing.plaf.synth.SynthContext p0, java.awt.Graphics p1) {  }
  public void propertyChange(java.beans.PropertyChangeEvent p0) { javax.swing.plaf.synth.SynthSupport.propertyChange(p0, Region.SCROLL_PANE); }
  public void paintBorder(javax.swing.plaf.synth.SynthContext p0, java.awt.Graphics p1, int p2, int p3, int p4, int p5) {  }
  public void installUI(javax.swing.JComponent c) {
    super.installUI(c);
    javax.swing.plaf.synth.SynthSupport.install(c, Region.SCROLL_PANE);
  }
  public void uninstallUI(javax.swing.JComponent c) {
    javax.swing.plaf.synth.SynthSupport.uninstall(c, Region.SCROLL_PANE);
    super.uninstallUI(c);
  }
}
