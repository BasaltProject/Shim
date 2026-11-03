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
import javax.swing.JLabel;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicGraphicsUtils;
import javax.swing.plaf.basic.BasicLabelUI;

public class MetalLabelUI extends BasicLabelUI {
  protected static MetalLabelUI metalLabelUI = new MetalLabelUI();

  public MetalLabelUI() {
  }

  public static ComponentUI createUI(JComponent c) {
    return metalLabelUI;
  }

  protected void paintDisabledText(JLabel l, Graphics g, String s, int textX, int textY) {
    int mnemIndex = l.getDisplayedMnemonicIndex();
    g.setColor(UIManager.getColor("Label.disabledForeground"));
    BasicGraphicsUtils.drawStringUnderlineCharAt(g, s, mnemIndex, textX, textY);
  }
}
