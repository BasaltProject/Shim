/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */


package javax.swing.plaf.metal;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import javax.swing.CellRendererPane;
import javax.swing.DefaultButtonModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;

public class MetalComboBoxButton extends JButton {
  protected JComboBox<Object> comboBox;
  protected JList<Object> listBox;
  protected CellRendererPane rendererPane;
  protected Icon comboIcon;
  protected boolean iconOnly = false;

  public final JComboBox<Object> getComboBox() {
    return comboBox;
  }

  public final void setComboBox(JComboBox<Object> cb) {
    JComboBox<Object> old = comboBox;
    comboBox = cb;
    firePropertyChange("comboBox", old, cb);
  }

  public final Icon getComboIcon() {
    return comboIcon;
  }

  public final void setComboIcon(Icon i) {
    Icon old = comboIcon;
    comboIcon = i;
    firePropertyChange("comboIcon", old, i);
  }

  public final boolean isIconOnly() {
    return iconOnly;
  }

  public final void setIconOnly(boolean isIconOnly) {
    boolean old = iconOnly;
    iconOnly = isIconOnly;
    firePropertyChange("iconOnly", old, isIconOnly);
  }

  MetalComboBoxButton() {
    super("");
    DefaultButtonModel model = new DefaultButtonModel() {
      public void setArmed(boolean armed) {
        super.setArmed(isPressed() ? true : armed);
      }
    };
    setModel(model);
  }

  public MetalComboBoxButton(JComboBox<Object> cb, Icon i, CellRendererPane pane, JList<Object> list) {
    this();
    comboBox = cb;
    comboIcon = i;
    rendererPane = pane;
    listBox = list;
    setEnabled(comboBox.isEnabled());
  }

  public MetalComboBoxButton(JComboBox<Object> cb, Icon i, boolean onlyIcon, CellRendererPane pane,
      JList<Object> list) {
    this(cb, i, pane, list);
    iconOnly = onlyIcon;
  }

  public boolean isFocusTraversable() {
    return false;
  }

  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);
    if (enabled) {
      setBackground(comboBox.getBackground());
      setForeground(comboBox.getForeground());
    } else {
      setBackground(UIManager.getColor("ComboBox.disabledBackground"));
      setForeground(UIManager.getColor("ComboBox.disabledForeground"));
    }
  }

  public Dimension getMinimumSize() {
    if (!isMinimumSizeSet()) {
      Dimension size = new Dimension();
      Insets insets = getInsets();
      size.width = comboIcon.getIconWidth() + insets.left + insets.right;
      size.height = comboIcon.getIconHeight() + insets.top + insets.bottom;
      return size;
    }
    return super.getMinimumSize();
  }

  public Dimension getMaximumSize() {
    return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
  }

  @SuppressWarnings("unchecked")
  public void paintComponent(Graphics g) {
    boolean leftToRight = comboBox.getComponentOrientation().isLeftToRight();
    super.paintComponent(g);
    Insets insets = getInsets();
    int width = getWidth() - (insets.left + insets.right);
    int height = getHeight() - (insets.top + insets.bottom);
    if (height <= 0 || width <= 0) {
      return;
    }
    int left = insets.left;
    int top = insets.top;
    int right = left + (width - 1);
    int bottom = top + (height - 1);
    int iconWidth = 0;
    int iconLeft = leftToRight ? right : left;
    if (comboIcon != null) {
      iconWidth = comboIcon.getIconWidth();
      int iconHeight = comboIcon.getIconHeight();
      int iconTop;
      if (iconOnly) {
        iconLeft = getWidth() / 2 - iconWidth / 2;
        iconTop = getHeight() / 2 - iconHeight / 2;
      } else {
        if (leftToRight) {
          iconLeft = (left + (width - 1)) - iconWidth;
        } else {
          iconLeft = left;
        }
        iconTop = (top + ((bottom - top) / 2)) - (iconHeight / 2);
      }
      comboIcon.paintIcon(this, g, iconLeft, iconTop);
    }
    if (!iconOnly && comboBox != null) {
      ListCellRenderer<Object> renderer = comboBox.getRenderer();
      boolean renderPressed = getModel().isPressed();
      Component c = renderer.getListCellRendererComponent(listBox, comboBox.getSelectedItem(), -1, renderPressed,
          false);
      c.setFont(rendererPane.getFont());
      if (model.isArmed() && model.isPressed()) {
        if (isOpaque()) {
          c.setBackground(UIManager.getColor("Button.select"));
        }
        c.setForeground(comboBox.getForeground());
      } else if (!comboBox.isEnabled()) {
        if (isOpaque()) {
          c.setBackground(UIManager.getColor("ComboBox.disabledBackground"));
        }
        c.setForeground(UIManager.getColor("ComboBox.disabledForeground"));
      } else {
        c.setForeground(comboBox.getForeground());
        c.setBackground(comboBox.getBackground());
      }
      int cWidth = width - (insets.right + iconWidth);
      boolean shouldValidate = c instanceof javax.swing.JPanel;
      if (leftToRight) {
        rendererPane.paintComponent(g, c, this, left, top, cWidth, height, shouldValidate);
      } else {
        rendererPane.paintComponent(g, c, this, left + iconWidth, top, cWidth, height, shouldValidate);
      }
    }
  }
}
