/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.plaf.synth;

public class SynthCheckBoxUI extends javax.swing.plaf.synth.SynthRadioButtonUI {
  public SynthCheckBoxUI() {
  }
  protected java.lang.String getPropertyPrefix() { return super.getPropertyPrefix(); }
  public static javax.swing.plaf.ComponentUI createUI(javax.swing.JComponent p0) { return new SynthCheckBoxUI(); }
  public void paintBorder(javax.swing.plaf.synth.SynthContext p0, java.awt.Graphics p1, int p2, int p3, int p4, int p5) { super.paintBorder(p0, p1, p2, p3, p4, p5); }
  public void installUI(javax.swing.JComponent c) {
    super.installUI(c);
    javax.swing.plaf.synth.SynthSupport.install(c, Region.CHECK_BOX);
  }
  public void uninstallUI(javax.swing.JComponent c) {
    javax.swing.plaf.synth.SynthSupport.uninstall(c, Region.CHECK_BOX);
    super.uninstallUI(c);
  }
}
