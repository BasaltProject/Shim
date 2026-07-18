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
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import javax.swing.AbstractButton;
import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.ButtonUI;
import javax.swing.plaf.ComponentUI;

public class BasicButtonUI extends ButtonUI {
  protected int defaultTextIconGap;
  private int shiftOffset = 0;
  protected int defaultTextShiftOffset;
  private static final BasicButtonUI shared = new BasicButtonUI();

  public BasicButtonUI() {
  }

  public static ComponentUI createUI(JComponent c) {
    return shared;
  }

  protected String getPropertyPrefix() {
    return "Button.";
  }

  public void installUI(JComponent c) {
    installDefaults((AbstractButton) c);
    installListeners((AbstractButton) c);
    installKeyboardActions((AbstractButton) c);
  }

  protected void installDefaults(AbstractButton b) {
    String pp = getPropertyPrefix();
    defaultTextShiftOffset = UIManager.getInt(pp + "textShiftOffset");
    if (b.isContentAreaFilled()) {
      LookAndFeel.installProperty(b, "opaque", Boolean.TRUE);
    } else {
      LookAndFeel.installProperty(b, "opaque", Boolean.FALSE);
    }
    if (b.getMargin() == null || (b.getMargin() instanceof javax.swing.plaf.UIResource)) {
      b.setMargin(UIManager.getInsets(pp + "margin"));
    }
    LookAndFeel.installColorsAndFont(b, pp + "background", pp + "foreground", pp + "font");
    LookAndFeel.installBorder(b, pp + "border");
    Object rollover = UIManager.get(pp + "rollover");
    if (rollover != null) {
      LookAndFeel.installProperty(b, "rolloverEnabled", rollover);
    }
    LookAndFeel.installProperty(b, "iconTextGap", Integer.valueOf(4));
  }

  protected void installListeners(AbstractButton b) {
    BasicButtonListener listener = createButtonListener(b);
    if (listener != null) {
      b.putClientProperty("shim.buttonListener", listener);
      b.addMouseListener(listener);
      b.addMouseMotionListener(listener);
      b.addFocusListener(listener);
      b.addPropertyChangeListener(listener);
      b.addChangeListener(listener);
    }
  }

  protected void installKeyboardActions(AbstractButton b) {
    BasicButtonListener listener = listenerOf(b);
    if (listener != null) {
      listener.installKeyboardActions(b);
    }
  }

  public void uninstallUI(JComponent c) {
    uninstallKeyboardActions((AbstractButton) c);
    uninstallListeners((AbstractButton) c);
    uninstallDefaults((AbstractButton) c);
  }

  protected void uninstallKeyboardActions(AbstractButton b) {
    BasicButtonListener listener = listenerOf(b);
    if (listener != null) {
      listener.uninstallKeyboardActions(b);
    }
  }

  protected void uninstallListeners(AbstractButton b) {
    BasicButtonListener listener = listenerOf(b);
    b.putClientProperty("shim.buttonListener", null);
    if (listener != null) {
      b.removeMouseListener(listener);
      b.removeMouseMotionListener(listener);
      b.removeFocusListener(listener);
      b.removeChangeListener(listener);
      b.removePropertyChangeListener(listener);
    }
  }

  protected void uninstallDefaults(AbstractButton b) {
    LookAndFeel.uninstallBorder(b);
  }

  private BasicButtonListener listenerOf(AbstractButton b) {
    return (BasicButtonListener) b.getClientProperty("shim.buttonListener");
  }

  protected BasicButtonListener createButtonListener(AbstractButton b) {
    return new BasicButtonListener(b);
  }

  public int getDefaultTextIconGap(AbstractButton b) {
    return defaultTextIconGap;
  }

  public void paint(Graphics g, JComponent c) {
    AbstractButton b = (AbstractButton) c;
    ButtonModel model = b.getModel();
    String text = layout(b, b.getFontMetrics(b.getFont()), b.getWidth(), b.getHeight());
    clearTextShiftOffset();
    if (model.isArmed() && model.isPressed()) {
      paintButtonPressed(g, b);
    }
    if (b.getIcon() != null) {
      paintIcon(g, c, iconRect);
    }
    if (text != null && !text.isEmpty()) {
      paintText(g, b, textRect, text);
    }
    if (b.isFocusPainted() && b.hasFocus()) {
      paintFocus(g, b, viewRect, textRect, iconRect);
    }
  }

  private Rectangle viewRect = new Rectangle();
  private Rectangle textRect = new Rectangle();
  private Rectangle iconRect = new Rectangle();

  private String layout(AbstractButton b, FontMetrics fm, int width, int height) {
    Insets i = b.getInsets();
    viewRect.x = i.left;
    viewRect.y = i.top;
    viewRect.width = width - (i.right + viewRect.x);
    viewRect.height = height - (i.bottom + viewRect.y);
    textRect.x = textRect.y = textRect.width = textRect.height = 0;
    iconRect.x = iconRect.y = iconRect.width = iconRect.height = 0;
    return SwingUtilities.layoutCompoundLabel(b, fm, b.getText(), b.getIcon(), b.getVerticalAlignment(),
        b.getHorizontalAlignment(), b.getVerticalTextPosition(), b.getHorizontalTextPosition(), viewRect, iconRect,
        textRect, b.getText() == null ? 0 : b.getIconTextGap());
  }

  protected void paintIcon(Graphics g, JComponent c, Rectangle iconRect) {
    AbstractButton b = (AbstractButton) c;
    ButtonModel model = b.getModel();
    Icon icon = b.getIcon();
    Icon tmpIcon = null;
    if (icon == null) {
      return;
    }
    if (!model.isEnabled()) {
      tmpIcon = model.isSelected() ? b.getDisabledSelectedIcon() : b.getDisabledIcon();
    } else if (model.isPressed() && model.isArmed()) {
      tmpIcon = b.getPressedIcon();
      if (tmpIcon != null) {
        clearTextShiftOffset();
      }
    } else if (b.isRolloverEnabled() && model.isRollover()) {
      tmpIcon = model.isSelected() ? b.getRolloverSelectedIcon() : b.getRolloverIcon();
    } else if (model.isSelected()) {
      tmpIcon = b.getSelectedIcon();
    }
    if (tmpIcon != null) {
      icon = tmpIcon;
    }
    if (model.isPressed() && model.isArmed()) {
      icon.paintIcon(c, g, iconRect.x + getTextShiftOffset(), iconRect.y + getTextShiftOffset());
    } else {
      icon.paintIcon(c, g, iconRect.x, iconRect.y);
    }
  }

  protected void paintText(Graphics g, JComponent c, Rectangle textRect, String text) {
    AbstractButton b = (AbstractButton) c;
    ButtonModel model = b.getModel();
    FontMetrics fm = b.getFontMetrics(g.getFont());
    int mnemonicIndex = b.getDisplayedMnemonicIndex();
    javax.swing.text.View htmlView = javax.swing.plaf.basic.BasicHTML.rendererFor(b, text);
    if (htmlView != null) {
      htmlView.paint(g, textRect);
      return;
    }
    if (model.isEnabled()) {
      g.setColor(b.getForeground());
    } else {
      g.setColor(b.getBackground().brighter());
    }
    BasicGraphicsUtils.drawStringUnderlineCharAt(g, text, mnemonicIndex, textRect.x + getTextShiftOffset(),
        textRect.y + fm.getAscent() + getTextShiftOffset());
    if (!model.isEnabled()) {
      g.setColor(b.getBackground().darker());
      BasicGraphicsUtils.drawStringUnderlineCharAt(g, text, mnemonicIndex, textRect.x + getTextShiftOffset() - 1,
          textRect.y + fm.getAscent() + getTextShiftOffset() - 1);
    }
  }

  protected void paintText(Graphics g, AbstractButton b, Rectangle textRect, String text) {
    paintText(g, (JComponent) b, textRect, text);
  }

  protected void paintFocus(Graphics g, AbstractButton b, Rectangle viewRect, Rectangle textRect, Rectangle iconRect) {
  }

  protected void paintButtonPressed(Graphics g, AbstractButton b) {
  }

  protected void clearTextShiftOffset() {
    this.shiftOffset = 0;
  }

  protected void setTextShiftOffset() {
    this.shiftOffset = defaultTextShiftOffset;
  }

  protected int getTextShiftOffset() {
    return shiftOffset;
  }

  public Dimension getMinimumSize(JComponent c) {
    Dimension d = getPreferredSize(c);
    javax.swing.text.View v = (javax.swing.text.View) c.getClientProperty(BasicHTML.propertyKey);
    if (v != null) {
      d.width -= v.getPreferredSpan(javax.swing.text.View.X_AXIS) - v.getMinimumSpan(javax.swing.text.View.X_AXIS);
    }
    return d;
  }

  public Dimension getPreferredSize(JComponent c) {
    AbstractButton b = (AbstractButton) c;
    return BasicGraphicsUtils.getPreferredButtonSize(b, b.getIconTextGap());
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
    if (width < 0 || height < 0) {
      throw new IllegalArgumentException("Width and height must be >= 0");
    }
    AbstractButton b = (AbstractButton) c;
    String text = b.getText();
    if (text == null || text.isEmpty()) {
      return -1;
    }
    FontMetrics fm = b.getFontMetrics(b.getFont());
    layout(b, fm, width, height);
    return textRect.y + fm.getAscent();
  }

  public Component.BaselineResizeBehavior getBaselineResizeBehavior(JComponent c) {
    super.getBaselineResizeBehavior(c);
    return Component.BaselineResizeBehavior.CENTER_OFFSET;
  }
}
