/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.plaf.synth;

public class SynthTextPaneUI extends javax.swing.plaf.synth.SynthEditorPaneUI {
  public SynthTextPaneUI() {
  }
  protected java.lang.String getPropertyPrefix() { return super.getPropertyPrefix(); }
  public static javax.swing.plaf.ComponentUI createUI(javax.swing.JComponent p0) { return new SynthTextPaneUI(); }
  protected void propertyChange(java.beans.PropertyChangeEvent p0) { javax.swing.plaf.synth.SynthSupport.propertyChange(p0, Region.TEXT_PANE); }
  public void paintBorder(javax.swing.plaf.synth.SynthContext p0, java.awt.Graphics p1, int p2, int p3, int p4, int p5) { super.paintBorder(p0, p1, p2, p3, p4, p5); }
  public void installUI(javax.swing.JComponent p0) { super.installUI(p0); javax.swing.plaf.synth.SynthSupport.install(p0, Region.TEXT_PANE); }
}
