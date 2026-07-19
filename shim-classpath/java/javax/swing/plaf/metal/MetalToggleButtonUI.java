/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package javax.swing.plaf.metal;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import javax.swing.AbstractButton;
import javax.swing.ButtonModel;
import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.BasicGraphicsUtils;
import javax.swing.plaf.basic.BasicToggleButtonUI;
import shim.swing.MetalPaint;

public class MetalToggleButtonUI extends BasicToggleButtonUI {
  protected Color focusColor;
  protected Color selectColor;
  protected Color disabledTextColor;
  private static final MetalToggleButtonUI shared = new MetalToggleButtonUI();

  public MetalToggleButtonUI() {
  }

  public static ComponentUI createUI(JComponent b) {
    return shared;
  }

  public void installDefaults(AbstractButton b) {
    super.installDefaults(b);
    String pp = getPropertyPrefix();
    focusColor = UIManager.getColor(pp + "focus");
    selectColor = UIManager.getColor(pp + "select");
    disabledTextColor = UIManager.getColor(pp + "disabledText");
  }

  protected void uninstallDefaults(AbstractButton b) {
    super.uninstallDefaults(b);
  }

  protected Color getSelectColor() {
    selectColor = UIManager.getColor(getPropertyPrefix() + "select");
    return selectColor;
  }

  protected Color getDisabledTextColor() {
    disabledTextColor = UIManager.getColor(getPropertyPrefix() + "disabledText");
    return disabledTextColor;
  }

  protected Color getFocusColor() {
    focusColor = UIManager.getColor(getPropertyPrefix() + "focus");
    return focusColor;
  }

  public void update(Graphics g, JComponent c) {
    AbstractButton button = (AbstractButton) c;
    if (c.isOpaque() && (c.getBackground() instanceof UIResource) && button.isContentAreaFilled() && c.isEnabled()) {
      ButtonModel model = button.getModel();
      if (!MetalPaint.isToolBarButton(c)) {
        if (!model.isArmed() && !model.isPressed() && !model.isSelected()
            && MetalPaint.drawGradient(c, g, "ToggleButton.gradient", 0, 0, c.getWidth(), c.getHeight(), true)) {
          paint(g, c);
          return;
        }
      } else if (model.isRollover() && !model.isArmed() && !model.isPressed() && !model.isSelected()
          && MetalPaint.drawGradient(c, g, "ToggleButton.gradient", 0, 0, c.getWidth(), c.getHeight(), true)) {
        paint(g, c);
        return;
      }
    }
    super.update(g, c);
  }

  protected void paintButtonPressed(Graphics g, AbstractButton b) {
    if (b.isContentAreaFilled()) {
      g.setColor(getSelectColor());
      g.fillRect(0, 0, b.getWidth(), b.getHeight());
    }
  }

  protected void paintText(Graphics g, JComponent c, Rectangle textRect, String text) {
    AbstractButton b = (AbstractButton) c;
    ButtonModel model = b.getModel();
    FontMetrics fm = b.getFontMetrics(g.getFont());
    int mnemonicIndex = b.getDisplayedMnemonicIndex();
    if (model.isEnabled()) {
      g.setColor(b.getForeground());
    } else if (model.isSelected()) {
      g.setColor(c.getBackground());
    } else {
      g.setColor(getDisabledTextColor());
    }
    BasicGraphicsUtils.drawStringUnderlineCharAt(g, text, mnemonicIndex, textRect.x + getTextShiftOffset(),
        textRect.y + fm.getAscent() + getTextShiftOffset());
  }

  protected void paintFocus(Graphics g, AbstractButton b, Rectangle viewRect, Rectangle textRect, Rectangle iconRect) {
    Rectangle focusRect = new Rectangle();
    String text = b.getText();
    boolean isIcon = b.getIcon() != null;
    if (text != null && !text.isEmpty()) {
      if (!isIcon) {
        focusRect.setBounds(textRect);
      } else {
        focusRect.setBounds(iconRect.union(textRect));
      }
    } else if (isIcon) {
      focusRect.setBounds(iconRect);
    }
    g.setColor(getFocusColor());
    g.drawRect(focusRect.x - 1, focusRect.y - 1, focusRect.width + 1, focusRect.height);
  }

  protected void paintIcon(Graphics g, AbstractButton b, Rectangle iconRect) {
    super.paintIcon(g, b, iconRect);
  }
}
