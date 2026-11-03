/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package javax.swing.plaf.metal;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicRadioButtonUI;

public class MetalRadioButtonUI extends BasicRadioButtonUI {
  protected Color focusColor;
  protected Color selectColor;
  protected Color disabledTextColor;
  private static final MetalRadioButtonUI shared = new MetalRadioButtonUI();
  private boolean defaults_initialized = false;

  public MetalRadioButtonUI() {
  }

  public static ComponentUI createUI(JComponent c) {
    return shared;
  }

  public void installDefaults(AbstractButton b) {
    super.installDefaults(b);
    if (!defaults_initialized) {
      focusColor = UIManager.getColor(getPropertyPrefix() + "focus");
      selectColor = UIManager.getColor(getPropertyPrefix() + "select");
      disabledTextColor = UIManager.getColor(getPropertyPrefix() + "disabledText");
      defaults_initialized = true;
    }
  }

  protected void uninstallDefaults(AbstractButton b) {
    super.uninstallDefaults(b);
    defaults_initialized = false;
  }

  protected Color getSelectColor() {
    return selectColor;
  }

  protected Color getDisabledTextColor() {
    return disabledTextColor;
  }

  protected Color getFocusColor() {
    return focusColor;
  }

  public synchronized void paint(Graphics g, JComponent c) {
    super.paint(g, c);
  }

  protected void paintFocus(Graphics g, Rectangle t, Dimension d) {
    g.setColor(getFocusColor());
    g.drawRect(t.x - 1, t.y, t.width + 1, t.height - 1);
  }
}
