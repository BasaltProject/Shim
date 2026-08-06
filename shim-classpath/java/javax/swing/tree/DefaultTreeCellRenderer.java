/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.tree;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.UIResource;

public class DefaultTreeCellRenderer extends JLabel implements TreeCellRenderer {
  private JTree tree;
  private boolean isDropCell;
  private boolean drawsFocusBorderAroundIcon;
  private boolean drawDashedFocusIndicator;
  private Color treeBGColor;
  private Color focusBGColor;

  protected boolean selected;
  protected boolean hasFocus;
  protected transient Icon closedIcon;
  protected transient Icon leafIcon;
  protected transient Icon openIcon;
  protected Color textSelectionColor;
  protected Color textNonSelectionColor;
  protected Color backgroundSelectionColor;
  protected Color backgroundNonSelectionColor;
  protected Color borderSelectionColor;

  public DefaultTreeCellRenderer() {
    inited = true;
  }

  private boolean inited;

  public void updateUI() {
    super.updateUI();
    if (!inited || (getLeafIcon() instanceof UIResource)) {
      setLeafIcon(UIManager.getIcon("Tree.leafIcon"));
    }
    if (!inited || (getClosedIcon() instanceof UIResource)) {
      setClosedIcon(UIManager.getIcon("Tree.closedIcon"));
    }
    if (!inited || (getOpenIcon() instanceof UIResource)) {
      setOpenIcon(UIManager.getIcon("Tree.openIcon"));
    }
    if (!inited || (getTextSelectionColor() instanceof UIResource)) {
      setTextSelectionColor(UIManager.getColor("Tree.selectionForeground"));
    }
    if (!inited || (getTextNonSelectionColor() instanceof UIResource)) {
      setTextNonSelectionColor(UIManager.getColor("Tree.textForeground"));
    }
    if (!inited || (getBackgroundSelectionColor() instanceof UIResource)) {
      setBackgroundSelectionColor(UIManager.getColor("Tree.selectionBackground"));
    }
    if (!inited || (getBackgroundNonSelectionColor() instanceof UIResource)) {
      setBackgroundNonSelectionColor(UIManager.getColor("Tree.textBackground"));
    }
    if (!inited || (getBorderSelectionColor() instanceof UIResource)) {
      setBorderSelectionColor(UIManager.getColor("Tree.selectionBorderColor"));
    }
    Object value = UIManager.get("Tree.drawsFocusBorderAroundIcon");
    drawsFocusBorderAroundIcon = (value != null && ((Boolean) value));
    value = UIManager.get("Tree.drawDashedFocusIndicator");
    drawDashedFocusIndicator = (value != null && ((Boolean) value));
    setName("Tree.cellRenderer");
  }

  public Icon getDefaultOpenIcon() { return UIManager.getIcon("Tree.openIcon"); }

  public Icon getDefaultClosedIcon() { return UIManager.getIcon("Tree.closedIcon"); }

  public Icon getDefaultLeafIcon() { return UIManager.getIcon("Tree.leafIcon"); }

  public void setOpenIcon(Icon newIcon) { openIcon = newIcon; }

  public Icon getOpenIcon() { return openIcon; }

  public void setClosedIcon(Icon newIcon) { closedIcon = newIcon; }

  public Icon getClosedIcon() { return closedIcon; }

  public void setLeafIcon(Icon newIcon) { leafIcon = newIcon; }

  public Icon getLeafIcon() { return leafIcon; }

  public void setTextSelectionColor(Color newColor) { textSelectionColor = newColor; }

  public Color getTextSelectionColor() { return textSelectionColor; }

  public void setTextNonSelectionColor(Color newColor) { textNonSelectionColor = newColor; }

  public Color getTextNonSelectionColor() { return textNonSelectionColor; }

  public void setBackgroundSelectionColor(Color newColor) { backgroundSelectionColor = newColor; }

  public Color getBackgroundSelectionColor() { return backgroundSelectionColor; }

  public void setBackgroundNonSelectionColor(Color newColor) { backgroundNonSelectionColor = newColor; }

  public Color getBackgroundNonSelectionColor() { return backgroundNonSelectionColor; }

  public void setBorderSelectionColor(Color newColor) { borderSelectionColor = newColor; }

  public Color getBorderSelectionColor() { return borderSelectionColor; }

  public void setFont(Font font) {
    if (font instanceof FontUIResource) {
      font = null;
    }
    super.setFont(font);
  }

  public Font getFont() {
    Font font = super.getFont();
    if (font == null && tree != null) {
      font = tree.getFont();
    }
    return font;
  }

  public void setBackground(Color color) {
    if (color instanceof ColorUIResource) {
      color = null;
    }
    super.setBackground(color);
  }

  public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
      boolean leaf, int row, boolean hasFocus) {
    String stringValue = tree.convertValueToText(value, sel, expanded, leaf, row, hasFocus);
    this.tree = tree;
    this.hasFocus = hasFocus;
    setText(stringValue);
    Color fg = null;
    isDropCell = false;
    JTree.DropLocation dropLocation = tree.getDropLocation();
    if (dropLocation != null && dropLocation.getChildIndex() == -1
        && tree.getRowForPath(dropLocation.getPath()) == row) {
      Color col = UIManager.getColor("Tree.dropCellForeground");
      if (col != null) {
        fg = col;
      } else {
        fg = getTextSelectionColor();
      }
      isDropCell = true;
    } else if (sel) {
      fg = getTextSelectionColor();
    } else {
      fg = getTextNonSelectionColor();
    }
    setForeground(fg);
    Icon icon = null;
    if (leaf) {
      icon = getLeafIcon();
    } else if (expanded) {
      icon = getOpenIcon();
    } else {
      icon = getClosedIcon();
    }
    if (!tree.isEnabled()) {
      setEnabled(false);
      LookAndFeel laf = UIManager.getLookAndFeel();
      Icon disabledIcon = laf.getDisabledIcon(tree, icon);
      if (disabledIcon != null) {
        icon = disabledIcon;
      }
      setDisabledIcon(icon);
    } else {
      setEnabled(true);
      setIcon(icon);
    }
    setComponentOrientation(tree.getComponentOrientation());
    selected = sel;
    return this;
  }

  public void paint(Graphics g) { super.paint(g); }

  public Dimension getPreferredSize() {
    Dimension retDimension = super.getPreferredSize();
    if (retDimension != null) {
      retDimension = new Dimension(retDimension.width + 3, retDimension.height);
    }
    return retDimension;
  }

  public void validate() { }

  public void invalidate() { }

  public void revalidate() { }

  public void repaint(long tm, int x, int y, int width, int height) { }

  public void repaint(Rectangle r) { }

  public void repaint() { }

  protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
    if (propertyName == "text" || ((propertyName == "font" || propertyName == "foreground") && oldValue != newValue)) {
      super.firePropertyChange(propertyName, oldValue, newValue);
    }
  }

  public void firePropertyChange(String propertyName, byte oldValue, byte newValue) { }

  public void firePropertyChange(String propertyName, char oldValue, char newValue) { }

  public void firePropertyChange(String propertyName, short oldValue, short newValue) { }

  public void firePropertyChange(String propertyName, int oldValue, int newValue) { }

  public void firePropertyChange(String propertyName, long oldValue, long newValue) { }

  public void firePropertyChange(String propertyName, float oldValue, float newValue) { }

  public void firePropertyChange(String propertyName, double oldValue, double newValue) { }

  public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) { }
}
