/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package javax.swing.plaf.basic;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.LookAndFeel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.LabelUI;

public class BasicLabelUI extends LabelUI implements PropertyChangeListener {
  protected static BasicLabelUI labelUI = new BasicLabelUI();
  private Rectangle paintIconR = new Rectangle();
  private Rectangle paintTextR = new Rectangle();
  private Rectangle paintViewR = new Rectangle();

  public BasicLabelUI() {
  }

  public static ComponentUI createUI(JComponent c) {
    return labelUI;
  }

  protected void paintEnabledText(JLabel l, Graphics g, String s, int textX, int textY) {
    int mnemIndex = l.getDisplayedMnemonicIndex();
    g.setColor(l.getForeground());
    BasicGraphicsUtils.drawStringUnderlineCharAt(g, s, mnemIndex, textX, textY);
  }

  protected void paintDisabledText(JLabel l, Graphics g, String s, int textX, int textY) {
    int accChar = l.getDisplayedMnemonicIndex();
    java.awt.Color background = l.getBackground();
    g.setColor(background.brighter());
    BasicGraphicsUtils.drawStringUnderlineCharAt(g, s, accChar, textX + 1, textY + 1);
    g.setColor(background.darker());
    BasicGraphicsUtils.drawStringUnderlineCharAt(g, s, accChar, textX, textY);
  }

  protected String layoutCL(JLabel label, FontMetrics fontMetrics, String text, Icon icon, Rectangle viewR,
      Rectangle iconR, Rectangle textR) {
    return SwingUtilities.layoutCompoundLabel(label, fontMetrics, text, icon, label.getVerticalAlignment(),
        label.getHorizontalAlignment(), label.getVerticalTextPosition(), label.getHorizontalTextPosition(), viewR,
        iconR, textR, label.getIconTextGap());
  }

  public void paint(Graphics g, JComponent c) {
    JLabel label = (JLabel) c;
    String text = label.getText();
    Icon icon = label.isEnabled() ? label.getIcon() : label.getDisabledIcon();
    if (icon == null && text == null) {
      return;
    }
    FontMetrics fm = label.getFontMetrics(g.getFont());
    Insets insets = c.getInsets();
    paintViewR.x = insets.left;
    paintViewR.y = insets.top;
    paintViewR.width = c.getWidth() - (insets.left + insets.right);
    paintViewR.height = c.getHeight() - (insets.top + insets.bottom);
    paintIconR.x = paintIconR.y = paintIconR.width = paintIconR.height = 0;
    paintTextR.x = paintTextR.y = paintTextR.width = paintTextR.height = 0;
    String clippedText = layoutCL(label, fm, text, icon, paintViewR, paintIconR, paintTextR);
    if (icon != null) {
      icon.paintIcon(c, g, paintIconR.x, paintIconR.y);
    }
    javax.swing.text.View htmlView = BasicHTML.rendererFor(label, text);
    if (htmlView != null) {
      htmlView.paint(g, paintTextR);
    } else if (text != null) {
      int textX = paintTextR.x;
      int textY = paintTextR.y + fm.getAscent();
      if (label.isEnabled()) {
        paintEnabledText(label, g, clippedText, textX, textY);
      } else {
        paintDisabledText(label, g, clippedText, textX, textY);
      }
    }
  }

  public Dimension getPreferredSize(JComponent c) {
    JLabel label = (JLabel) c;
    String text = label.getText();
    Icon icon = label.isEnabled() ? label.getIcon() : label.getDisabledIcon();
    Insets insets = label.getInsets();
    java.awt.Font font = label.getFont();
    int dx = insets.left + insets.right;
    int dy = insets.top + insets.bottom;
    if (icon == null && (text == null || font == null)) {
      return new Dimension(dx, dy);
    } else if (text == null || (icon != null && font == null)) {
      return new Dimension(icon.getIconWidth() + dx, icon.getIconHeight() + dy);
    }
    FontMetrics fm = label.getFontMetrics(font);
    Rectangle iconR = new Rectangle();
    Rectangle textR = new Rectangle();
    Rectangle viewR = new Rectangle();
    iconR.x = iconR.y = iconR.width = iconR.height = 0;
    textR.x = textR.y = textR.width = textR.height = 0;
    viewR.x = dx;
    viewR.y = dy;
    viewR.width = viewR.height = Short.MAX_VALUE;
    layoutCL(label, fm, text, icon, viewR, iconR, textR);
    int x1 = Math.min(iconR.x, textR.x);
    int x2 = Math.max(iconR.x + iconR.width, textR.x + textR.width);
    int y1 = Math.min(iconR.y, textR.y);
    int y2 = Math.max(iconR.y + iconR.height, textR.y + textR.height);
    return new Dimension(x2 - x1 + dx, y2 - y1 + dy);
  }

  public Dimension getMinimumSize(JComponent c) {
    Dimension d = getPreferredSize(c);
    javax.swing.text.View v = (javax.swing.text.View) c.getClientProperty(BasicHTML.propertyKey);
    if (v != null) {
      d.width -= v.getPreferredSpan(javax.swing.text.View.X_AXIS) - v.getMinimumSpan(javax.swing.text.View.X_AXIS);
    }
    return d;
  }

  public Dimension getMaximumSize(JComponent c) {
    Dimension d = getPreferredSize(c);
    javax.swing.text.View v = (javax.swing.text.View) c.getClientProperty(BasicHTML.propertyKey);
    if (v != null) {
      d.width += v.getMaximumSpan(javax.swing.text.View.X_AXIS) - v.getPreferredSpan(javax.swing.text.View.X_AXIS);
    }
    return d;
  }

  public int getBaseline(JComponent c, int width, int height) {
    super.getBaseline(c, width, height);
    JLabel label = (JLabel) c;
    String text = label.getText();
    if (text == null || text.isEmpty() || label.getFont() == null) {
      return -1;
    }
    FontMetrics fm = label.getFontMetrics(label.getFont());
    Icon icon = label.isEnabled() ? label.getIcon() : label.getDisabledIcon();
    Rectangle viewR = new Rectangle();
    Rectangle iconR = new Rectangle();
    Rectangle textR = new Rectangle();
    Insets insets = label.getInsets();
    viewR.x = insets.left;
    viewR.y = insets.top;
    viewR.width = width - (insets.left + insets.right);
    viewR.height = height - (insets.top + insets.bottom);
    layoutCL(label, fm, text, icon, viewR, iconR, textR);
    return textR.y + fm.getAscent();
  }

  public Component.BaselineResizeBehavior getBaselineResizeBehavior(JComponent c) {
    super.getBaselineResizeBehavior(c);
    JLabel label = (JLabel) c;
    switch (label.getVerticalAlignment()) {
      case SwingConstants.TOP:
        return Component.BaselineResizeBehavior.CONSTANT_ASCENT;
      case SwingConstants.BOTTOM:
        return Component.BaselineResizeBehavior.CONSTANT_DESCENT;
      case SwingConstants.CENTER:
        return Component.BaselineResizeBehavior.CENTER_OFFSET;
      default:
        return Component.BaselineResizeBehavior.OTHER;
    }
  }

  public void installUI(JComponent c) {
    installDefaults((JLabel) c);
    installComponents((JLabel) c);
    installListeners((JLabel) c);
    installKeyboardActions((JLabel) c);
  }

  public void uninstallUI(JComponent c) {
    uninstallDefaults((JLabel) c);
    uninstallComponents((JLabel) c);
    uninstallListeners((JLabel) c);
    uninstallKeyboardActions((JLabel) c);
  }

  protected void installComponents(JLabel c) {
  }

  protected void uninstallComponents(JLabel c) {
  }

  protected void installDefaults(JLabel c) {
    LookAndFeel.installColorsAndFont(c, "Label.background", "Label.foreground", "Label.font");
    LookAndFeel.installProperty(c, "opaque", Boolean.FALSE);
  }

  protected void installListeners(JLabel c) {
    c.addPropertyChangeListener(this);
  }

  protected void installKeyboardActions(JLabel l) {
  }

  protected void uninstallDefaults(JLabel c) {
  }

  protected void uninstallListeners(JLabel c) {
    c.removePropertyChangeListener(this);
  }

  protected void uninstallKeyboardActions(JLabel c) {
  }

  public void propertyChange(PropertyChangeEvent e) {
    String name = e.getPropertyName();
    if (name == "text" || "font".equals(name) || "foreground".equals(name)) {
      JLabel lbl = (JLabel) e.getSource();
      lbl.revalidate();
      lbl.repaint();
    }
  }
}
