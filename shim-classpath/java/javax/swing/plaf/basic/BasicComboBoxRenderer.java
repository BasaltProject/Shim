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
import java.io.Serializable;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

public class BasicComboBoxRenderer extends JLabel implements ListCellRenderer<Object>, Serializable {
  protected static Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);
  private static final Border safeNoFocusBorder = new EmptyBorder(1, 1, 1, 1);

  public BasicComboBoxRenderer() {
    super();
    setOpaque(true);
    setBorder(getNoFocusBorder());
  }

  private static Border getNoFocusBorder() {
    Border b = UIManager.getBorder("List.cellNoFocusBorder");
    if (b == null) {
      return noFocusBorder == null ? safeNoFocusBorder : noFocusBorder;
    }
    return b;
  }

  public Dimension getPreferredSize() {
    Dimension size;
    if ((this.getText() == null) || (this.getText().equals(""))) {
      this.setText(" ");
      size = super.getPreferredSize();
      this.setText("");
    } else {
      size = super.getPreferredSize();
    }
    return size;
  }

  public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
      boolean cellHasFocus) {
    if (isSelected) {
      setBackground(list.getSelectionBackground());
      setForeground(list.getSelectionForeground());
    } else {
      setBackground(list.getBackground());
      setForeground(list.getForeground());
    }
    setFont(list.getFont());
    if (value instanceof Icon) {
      setIcon((Icon) value);
    } else {
      setText(value == null ? "" : value.toString());
    }
    return this;
  }

  public static class UIResource extends BasicComboBoxRenderer implements javax.swing.plaf.UIResource {
    public UIResource() {
    }
  }
}
