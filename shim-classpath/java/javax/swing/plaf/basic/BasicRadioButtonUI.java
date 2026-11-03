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
import javax.swing.AbstractButton;
import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;

public class BasicRadioButtonUI extends BasicToggleButtonUI {
  private static final BasicRadioButtonUI shared = new BasicRadioButtonUI();
  protected Icon icon;
  private boolean defaults_initialized = false;
  private Dimension size = new Dimension();
  private Rectangle viewRect = new Rectangle();
  private Rectangle iconRect = new Rectangle();
  private Rectangle textRect = new Rectangle();
  private Rectangle prefViewRect = new Rectangle();
  private Rectangle prefIconRect = new Rectangle();
  private Rectangle prefTextRect = new Rectangle();

  public BasicRadioButtonUI() {
  }

  public static ComponentUI createUI(JComponent b) {
    return shared;
  }

  protected String getPropertyPrefix() {
    return "RadioButton.";
  }

  protected void installDefaults(AbstractButton b) {
    super.installDefaults(b);
    if (!defaults_initialized) {
      icon = UIManager.getIcon(getPropertyPrefix() + "icon");
      defaults_initialized = true;
    }
  }

  protected void uninstallDefaults(AbstractButton b) {
    super.uninstallDefaults(b);
    defaults_initialized = false;
  }

  public Icon getDefaultIcon() {
    return icon;
  }

  public synchronized void paint(Graphics g, JComponent c) {
    AbstractButton b = (AbstractButton) c;
    ButtonModel model = b.getModel();
    Font f = c.getFont();
    g.setFont(f);
    FontMetrics fm = c.getFontMetrics(f);
    Insets i = c.getInsets();
    size = b.getSize(size);
    viewRect.x = i.left;
    viewRect.y = i.top;
    viewRect.width = size.width - (i.right + viewRect.x);
    viewRect.height = size.height - (i.bottom + viewRect.y);
    iconRect.x = iconRect.y = iconRect.width = iconRect.height = 0;
    textRect.x = textRect.y = textRect.width = textRect.height = 0;
    Icon altIcon = b.getIcon();
    String text = SwingUtilities.layoutCompoundLabel(c, fm, b.getText(), altIcon != null ? altIcon : getDefaultIcon(),
        b.getVerticalAlignment(), b.getHorizontalAlignment(), b.getVerticalTextPosition(),
        b.getHorizontalTextPosition(), viewRect, iconRect, textRect, b.getText() == null ? 0 : b.getIconTextGap());
    if (c.isOpaque()) {
      g.setColor(b.getBackground());
      g.fillRect(0, 0, size.width, size.height);
    }
    if (altIcon != null) {
      if (!model.isEnabled()) {
        altIcon = model.isSelected() ? b.getDisabledSelectedIcon() : b.getDisabledIcon();
      } else if (model.isPressed() && model.isArmed()) {
        altIcon = b.getPressedIcon();
        if (altIcon == null) {
          altIcon = b.getSelectedIcon();
        }
      } else if (model.isSelected()) {
        if (b.isRolloverEnabled() && model.isRollover()) {
          altIcon = b.getRolloverSelectedIcon();
          if (altIcon == null) {
            altIcon = b.getSelectedIcon();
          }
        } else {
          altIcon = b.getSelectedIcon();
        }
      } else if (b.isRolloverEnabled() && model.isRollover()) {
        altIcon = b.getRolloverIcon();
      }
      if (altIcon == null) {
        altIcon = b.getIcon();
      }
      altIcon.paintIcon(c, g, iconRect.x, iconRect.y);
    } else {
      getDefaultIcon().paintIcon(c, g, iconRect.x, iconRect.y);
    }
    if (text != null) {
      paintText(g, b, textRect, text);
      if (b.hasFocus() && b.isFocusPainted() && textRect.width > 0 && textRect.height > 0) {
        paintFocus(g, textRect, size);
      }
    }
  }

  protected void paintFocus(Graphics g, Rectangle textRect, Dimension size) {
  }

  public Dimension getPreferredSize(JComponent c) {
    if (c.getComponentCount() > 0) {
      return null;
    }
    AbstractButton b = (AbstractButton) c;
    String text = b.getText();
    Icon buttonIcon = b.getIcon();
    if (buttonIcon == null) {
      buttonIcon = getDefaultIcon();
    }
    Font font = b.getFont();
    FontMetrics fm = b.getFontMetrics(font);
    prefViewRect.x = prefViewRect.y = 0;
    prefViewRect.width = Short.MAX_VALUE;
    prefViewRect.height = Short.MAX_VALUE;
    prefIconRect.x = prefIconRect.y = prefIconRect.width = prefIconRect.height = 0;
    prefTextRect.x = prefTextRect.y = prefTextRect.width = prefTextRect.height = 0;
    SwingUtilities.layoutCompoundLabel(c, fm, text, buttonIcon, b.getVerticalAlignment(), b.getHorizontalAlignment(),
        b.getVerticalTextPosition(), b.getHorizontalTextPosition(), prefViewRect, prefIconRect, prefTextRect,
        text == null ? 0 : b.getIconTextGap());
    Rectangle r = prefIconRect.union(prefTextRect);
    Insets insets = b.getInsets();
    r.width += insets.left + insets.right;
    r.height += insets.top + insets.bottom;
    return r.getSize();
  }
}
