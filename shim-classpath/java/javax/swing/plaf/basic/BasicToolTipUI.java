/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.plaf.basic;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import javax.swing.JComponent;
import javax.swing.JToolTip;
import javax.swing.LookAndFeel;

public class BasicToolTipUI extends javax.swing.plaf.ToolTipUI {
  public BasicToolTipUI() {
  }
  protected void installDefaults(JComponent c) {
    LookAndFeel.installColorsAndFont(c, "ToolTip.background", "ToolTip.foreground", "ToolTip.font");
    LookAndFeel.installProperty(c, "opaque", Boolean.TRUE);
    LookAndFeel.installBorder(c, "ToolTip.border");
  }
  protected void installListeners(JComponent c) {
  }
  protected void uninstallListeners(JComponent c) {
  }
  protected void uninstallDefaults(JComponent c) {
    LookAndFeel.uninstallBorder(c);
  }
  public static javax.swing.plaf.ComponentUI createUI(JComponent c) {
    return new BasicToolTipUI();
  }
  public Dimension getMaximumSize(JComponent c) {
    Dimension d = getPreferredSize(c);
    javax.swing.text.View v = (javax.swing.text.View) c.getClientProperty(BasicHTML.propertyKey);
    if (v != null) {
      d.width += v.getMaximumSpan(javax.swing.text.View.X_AXIS) - v.getPreferredSpan(javax.swing.text.View.X_AXIS);
    }
    return d;
  }
  public void paint(Graphics g, JComponent c) {
    Font font = c.getFont();
    FontMetrics metrics = c.getFontMetrics(font);
    Dimension size = c.getSize();
    g.setColor(c.getForeground());
    String tipText = ((JToolTip) c).getTipText();
    if (tipText == null) {
      tipText = "";
    }
    Insets insets = c.getInsets();
    Rectangle paintTextR = new Rectangle(insets.left + 3, insets.top, size.width - (insets.left + insets.right) - 6,
        size.height - (insets.top + insets.bottom));
    g.setFont(font);
    javax.swing.text.View htmlView = BasicHTML.rendererFor(c, tipText);
    if (htmlView != null) {
      htmlView.paint(g, paintTextR);
    } else {
      g.drawString(tipText, paintTextR.x, paintTextR.y + metrics.getAscent());
    }
  }
  public Dimension getPreferredSize(JComponent c) {
    Font font = c.getFont();
    FontMetrics fm = c.getFontMetrics(font);
    Insets insets = c.getInsets();
    Dimension prefSize = new Dimension(insets.left + insets.right, insets.top + insets.bottom);
    String text = ((JToolTip) c).getTipText();
    if (text == null || text.isEmpty()) {
      text = "";
    } else {
      javax.swing.text.View htmlView = BasicHTML.rendererFor(c, text);
      if (htmlView != null) {
        prefSize.width += (int) htmlView.getPreferredSpan(javax.swing.text.View.X_AXIS) + 6;
        prefSize.height += (int) htmlView.getPreferredSpan(javax.swing.text.View.Y_AXIS);
      } else {
        prefSize.width += fm.stringWidth(text) + 6;
        prefSize.height += fm.getHeight();
      }
    }
    return prefSize;
  }
  public Dimension getMinimumSize(JComponent c) {
    Dimension d = getPreferredSize(c);
    javax.swing.text.View v = (javax.swing.text.View) c.getClientProperty(BasicHTML.propertyKey);
    if (v != null) {
      d.width -= v.getPreferredSpan(javax.swing.text.View.X_AXIS) - v.getMinimumSpan(javax.swing.text.View.X_AXIS);
    }
    return d;
  }
  public void installUI(JComponent c) {
    installDefaults(c);
    installListeners(c);
  }
  public void uninstallUI(JComponent c) {
    uninstallDefaults(c);
    uninstallListeners(c);
  }
}
