/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.plaf.metal;

import java.awt.Graphics;
import javax.swing.JComponent;

public class MetalProgressBarUI extends javax.swing.plaf.basic.BasicProgressBarUI {
  public MetalProgressBarUI() {
  }
  public void paintIndeterminate(Graphics g, JComponent c) {
    super.paintIndeterminate(g, c);
  }
  public void paintDeterminate(Graphics g, JComponent c) {
    super.paintDeterminate(g, c);
  }
  public static javax.swing.plaf.ComponentUI createUI(JComponent c) {
    return new MetalProgressBarUI();
  }
}
