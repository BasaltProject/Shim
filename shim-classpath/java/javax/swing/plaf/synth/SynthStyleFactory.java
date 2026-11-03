/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.plaf.synth;

public abstract class SynthStyleFactory {
  public SynthStyleFactory() {
  }
  public abstract javax.swing.plaf.synth.SynthStyle getStyle(javax.swing.JComponent p0, javax.swing.plaf.synth.Region p1);
}
