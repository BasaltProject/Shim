/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.plaf.basic;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.JComponent;
import javax.swing.JSeparator;
import javax.swing.LookAndFeel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

public class BasicSeparatorUI extends javax.swing.plaf.SeparatorUI {
  protected java.awt.Color shadow;
  protected java.awt.Color highlight;
  public BasicSeparatorUI() {
  }
  protected void installDefaults(JSeparator s) {
    LookAndFeel.installColors(s, "Separator.background", "Separator.foreground");
    LookAndFeel.installProperty(s, "opaque", Boolean.FALSE);
    shadow = UIManager.getColor("Separator.shadow");
    highlight = UIManager.getColor("Separator.highlight");
  }
  protected void installListeners(JSeparator s) {
  }
  protected void uninstallListeners(JSeparator s) {
  }
  protected void uninstallDefaults(JSeparator s) {
  }
  public static javax.swing.plaf.ComponentUI createUI(JComponent c) {
    return new BasicSeparatorUI();
  }
  public Dimension getMaximumSize(JComponent c) {
    if (((JSeparator) c).getOrientation() == JSeparator.VERTICAL) {
      return new Dimension(2, Short.MAX_VALUE);
    }
    return new Dimension(Short.MAX_VALUE, 2);
  }
  public void paint(Graphics g, JComponent c) {
    java.awt.Dimension s = c.getSize();
    if (((JSeparator) c).getOrientation() == JSeparator.VERTICAL) {
      g.setColor(c.getForeground());
      g.drawLine(0, 0, 0, s.height);
      g.setColor(c.getBackground());
      g.drawLine(1, 0, 1, s.height);
    } else {
      g.setColor(c.getForeground());
      g.drawLine(0, 0, s.width, 0);
      g.setColor(c.getBackground());
      g.drawLine(0, 1, s.width, 1);
    }
  }
  public Dimension getPreferredSize(JComponent c) {
    if (((JSeparator) c).getOrientation() == JSeparator.VERTICAL) {
      return new Dimension(2, 0);
    }
    return new Dimension(0, 2);
  }
  public Dimension getMinimumSize(JComponent c) {
    return null;
  }
  public void installUI(JComponent c) {
    installDefaults((JSeparator) c);
    installListeners((JSeparator) c);
  }
  public void uninstallUI(JComponent c) {
    uninstallDefaults((JSeparator) c);
    uninstallListeners((JSeparator) c);
  }
}
