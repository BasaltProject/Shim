/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.plaf.metal;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import javax.swing.JComponent;
import javax.swing.JToolTip;

public class MetalToolTipUI extends javax.swing.plaf.basic.BasicToolTipUI {
  private java.lang.String bp_AcceleratorString;
  public static final int padSpaceBetweenStrings = 12;
  public MetalToolTipUI() {
  }
  public String getAcceleratorString() {
    return acceleratorString;
  }
  protected boolean isAcceleratorHidden() {
    Boolean b = (Boolean) javax.swing.UIManager.get("ToolTip.hideAccelerator");
    return b != null && b.booleanValue();
  }
  public static javax.swing.plaf.ComponentUI createUI(JComponent c) {
    return new MetalToolTipUI();
  }
  public void paint(Graphics g, JComponent c) {
    super.paint(g, c);
  }
  public Dimension getPreferredSize(JComponent c) {
    Dimension d = super.getPreferredSize(c);
    acceleratorString = computeAccelerator(c);
    if (!acceleratorString.equals("")) {
      FontMetrics fm = c.getFontMetrics(smallFont == null ? c.getFont() : smallFont);
      d.width += fm.stringWidth(acceleratorString) + padSpaceBetweenStrings;
    }
    return d;
  }
  public void installUI(JComponent c) {
    super.installUI(c);
    smallFont = javax.swing.UIManager.getFont("ToolTip.font");
  }
  public void uninstallUI(JComponent c) {
    super.uninstallUI(c);
  }
  private String acceleratorString = "";
  private Font smallFont;

  private String computeAccelerator(JComponent c) {
    JToolTip tip = (JToolTip) c;
    JComponent comp = tip.getComponent();
    if (comp instanceof javax.swing.AbstractButton && !isAcceleratorHidden()) {
      int mnemonic = ((javax.swing.AbstractButton) comp).getMnemonic();
      if (mnemonic != 0) {
        return java.awt.event.KeyEvent.getKeyText(mnemonic);
      }
    }
    return "";
  }
}
