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
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.plaf.ComponentUI;

public class BasicToggleButtonUI extends BasicButtonUI {
  private static final BasicToggleButtonUI shared = new BasicToggleButtonUI();

  public BasicToggleButtonUI() {
  }

  public static ComponentUI createUI(JComponent b) {
    return shared;
  }

  protected String getPropertyPrefix() {
    return "ToggleButton.";
  }

  public void paint(Graphics g, JComponent c) {
    AbstractButton b = (AbstractButton) c;
    ButtonModel model = b.getModel();
    Dimension size = b.getSize();
    FontMetrics fm = g.getFontMetrics();
    Insets i = c.getInsets();
    Rectangle viewRect = new Rectangle(size);
    viewRect.x += i.left;
    viewRect.y += i.top;
    viewRect.width -= (i.right + viewRect.x);
    viewRect.height -= (i.bottom + viewRect.y);
    Rectangle iconRect = new Rectangle();
    Rectangle textRect = new Rectangle();
    Font f = c.getFont();
    g.setFont(f);
    String text = SwingUtilities.layoutCompoundLabel(c, fm, b.getText(), b.getIcon(), b.getVerticalAlignment(),
        b.getHorizontalAlignment(), b.getVerticalTextPosition(), b.getHorizontalTextPosition(), viewRect, iconRect,
        textRect, b.getText() == null ? 0 : b.getIconTextGap());
    g.setColor(b.getBackground());
    if (model.isArmed() && model.isPressed() || model.isSelected()) {
      paintButtonPressed(g, b);
    }
    if (b.getIcon() != null) {
      paintIcon(g, b, iconRect);
    }
    if (text != null && !text.isEmpty()) {
      paintText(g, b, textRect, text);
    }
    if (b.isFocusPainted() && b.hasFocus()) {
      paintFocus(g, b, viewRect, textRect, iconRect);
    }
  }

  protected void paintIcon(Graphics g, AbstractButton b, Rectangle iconRect) {
    super.paintIcon(g, (JComponent) b, iconRect);
  }

  protected int getTextShiftOffset() {
    return 0;
  }
}
