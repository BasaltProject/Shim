/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.table;

import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;
import java.io.Serializable;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

public class DefaultTableCellRenderer extends JLabel implements TableCellRenderer, Serializable {
  private static final Border SAFE_NO_FOCUS_BORDER = new EmptyBorder(1, 1, 1, 1);
  private static final Border DEFAULT_NO_FOCUS_BORDER = new EmptyBorder(1, 1, 1, 1);

  protected static Border noFocusBorder = DEFAULT_NO_FOCUS_BORDER;

  private Color unselectedForeground;
  private Color unselectedBackground;

  public DefaultTableCellRenderer() {
    super();
    setOpaque(true);
    setBorder(getNoFocusBorder());
    setName("Table.cellRenderer");
  }

  private Border getNoFocusBorder() {
    Border border = UIManager.getBorder("Table.cellNoFocusBorder");
    if (border != null) {
      return border;
    }
    return SAFE_NO_FOCUS_BORDER;
  }

  public void setForeground(Color c) {
    super.setForeground(c);
    unselectedForeground = c;
  }

  public void setBackground(Color c) {
    super.setBackground(c);
    unselectedBackground = c;
  }

  public void updateUI() {
    super.updateUI();
    setForeground(null);
    setBackground(null);
  }

  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
      int row, int column) {
    if (table == null) {
      return this;
    }
    Color fg = null;
    Color bg = null;
    JTable.DropLocation dropLocation = table.getDropLocation();
    if (dropLocation != null && !dropLocation.isInsertRow() && !dropLocation.isInsertColumn()
        && dropLocation.getRow() == row && dropLocation.getColumn() == column) {
      fg = UIManager.getColor("Table.dropCellForeground");
      bg = UIManager.getColor("Table.dropCellBackground");
      isSelected = true;
    }
    if (isSelected) {
      super.setForeground(fg == null ? table.getSelectionForeground() : fg);
      super.setBackground(bg == null ? table.getSelectionBackground() : bg);
    } else {
      Color background = unselectedBackground != null ? unselectedBackground : table.getBackground();
      if (background == null || background instanceof javax.swing.plaf.UIResource) {
        Color alternateColor = UIManager.getColor("Table.alternateRowColor");
        if (alternateColor != null && row % 2 != 0) {
          background = alternateColor;
        }
      }
      super.setForeground(unselectedForeground != null ? unselectedForeground : table.getForeground());
      super.setBackground(background);
    }
    setFont(table.getFont());
    if (hasFocus) {
      Border border = null;
      if (isSelected) {
        border = UIManager.getBorder("Table.focusSelectedCellHighlightBorder");
      }
      if (border == null) {
        border = UIManager.getBorder("Table.focusCellHighlightBorder");
      }
      setBorder(border);
      if (!isSelected && table.isCellEditable(row, column)) {
        Color col = UIManager.getColor("Table.focusCellForeground");
        if (col != null) {
          super.setForeground(col);
        }
        col = UIManager.getColor("Table.focusCellBackground");
        if (col != null) {
          super.setBackground(col);
        }
      }
    } else {
      setBorder(getNoFocusBorder());
    }
    setValue(value);
    return this;
  }

  public boolean isOpaque() {
    Color back = getBackground();
    Component p = getParent();
    if (p != null) {
      p = p.getParent();
    }
    boolean colorMatch = (back != null) && (p != null) && back.equals(p.getBackground()) && p.isOpaque();
    return !colorMatch && super.isOpaque();
  }

  public void invalidate() { }

  public void validate() { }

  public void revalidate() { }

  public void repaint(long tm, int x, int y, int width, int height) { }

  public void repaint(Rectangle r) { }

  public void repaint() { }

  protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
    if (propertyName == "text" || propertyName == "labelFor" || propertyName == "displayedMnemonic"
        || ((propertyName == "font" || propertyName == "foreground") && oldValue != newValue)) {
      super.firePropertyChange(propertyName, oldValue, newValue);
    }
  }

  public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) { }

  protected void setValue(Object value) { setText((value == null) ? "" : value.toString()); }

  public static class UIResource extends DefaultTableCellRenderer implements javax.swing.plaf.UIResource {
    public UIResource() { }
  }
}
