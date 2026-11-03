/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing;

import java.awt.*;
import javax.swing.border.Border;

public class DefaultListCellRenderer extends javax.swing.JLabel implements javax.swing.ListCellRenderer<java.lang.Object>, java.io.Serializable {
  private boolean bp_Opaque;
  protected static javax.swing.border.Border noFocusBorder;
  public DefaultListCellRenderer() {
  }
  public void validate() {
    
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
  public void invalidate() {
    
  }
  public java.awt.Component getListCellRendererComponent(javax.swing.JList<?> p0, java.lang.Object p1, int p2, boolean p3, boolean p4) {
    setComponentOrientation(p0.getComponentOrientation());
    Color bg = null;
    Color fg = null;
    JList.DropLocation dropLocation = p0.getDropLocation();
    if (dropLocation != null && !dropLocation.isInsert() && dropLocation.getIndex() == p2) {
      bg = UIManager.getColor("List.dropCellBackground");
      fg = UIManager.getColor("List.dropCellForeground");
      p3 = true;
    }
    if (p3) {
      setBackground(bg == null ? p0.getSelectionBackground() : bg);
      setForeground(fg == null ? p0.getSelectionForeground() : fg);
    } else {
      setBackground(p0.getBackground());
      setForeground(p0.getForeground());
    }
    if (p1 instanceof Icon) {
      setIcon((Icon) p1);
      setText("");
    } else {
      setIcon(null);
      setText((p1 == null) ? "" : p1.toString());
    }
    setEnabled(p0.isEnabled());
    setFont(p0.getFont());
    Border border = null;
    if (p4) {
      if (p3) {
        border = UIManager.getBorder("List.focusSelectedCellHighlightBorder");
      }
      if (border == null) {
        border = UIManager.getBorder("List.focusCellHighlightBorder");
      }
    } else {
      border = noFocus();
    }
    setBorder(border);
    return this;
  }
  public void firePropertyChange(java.lang.String p0, int p1, int p2) {
    
  }
  public void firePropertyChange(java.lang.String p0, long p1, long p2) {
    
  }
  public void firePropertyChange(java.lang.String p0, short p1, short p2) {
    
  }
  protected void firePropertyChange(java.lang.String p0, java.lang.Object p1, java.lang.Object p2) {
    if (p0 == "text" || p0 == "labelFor" || p0 == "displayedMnemonic"
        || ((p0 == "font" || p0 == "foreground") && p1 != p2)) {
      super.firePropertyChange(p0, p1, p2);
    }
  }
  public void firePropertyChange(java.lang.String p0, boolean p1, boolean p2) {
    
  }
  public void firePropertyChange(java.lang.String p0, double p1, double p2) {
    
  }
  public void firePropertyChange(java.lang.String p0, float p1, float p2) {
    
  }
  public void firePropertyChange(java.lang.String p0, char p1, char p2) {
    
  }
  public void firePropertyChange(java.lang.String p0, byte p1, byte p2) {
    
  }
  public void repaint(long p0, int p1, int p2, int p3, int p4) {
    
  }
  public void repaint() {
    
  }
  public void repaint(java.awt.Rectangle p0) {
    
  }
  public void revalidate() {
    
  }

  public static class UIResource extends javax.swing.DefaultListCellRenderer implements javax.swing.plaf.UIResource {
    public UIResource() {
    }
  }

  private static final Border SAFE_NO_FOCUS_BORDER = new javax.swing.border.EmptyBorder(1, 1, 1, 1);

  private static Border noFocus() {
    Border b = UIManager.getBorder("List.cellNoFocusBorder");
    return b != null ? b : SAFE_NO_FOCUS_BORDER;
  }

}
