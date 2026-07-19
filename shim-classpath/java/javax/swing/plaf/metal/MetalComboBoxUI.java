/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.plaf.metal;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.ComboBoxEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;

public class MetalComboBoxUI extends javax.swing.plaf.basic.BasicComboBoxUI {
  public MetalComboBoxUI() {
  }
  protected LayoutManager createLayoutManager() {
    return new MetalComboBoxLayoutManager();
  }
  protected ComboBoxEditor createEditor() {
    return new MetalComboBoxEditor.UIResource();
  }
  protected javax.swing.plaf.basic.ComboPopup createPopup() {
    return new MetalComboPopup(comboBox);
  }
  protected JButton createArrowButton() {
    JButton button = new MetalComboBoxButton(comboBox, new MetalComboBoxIcon(), comboBox.isEditable(), currentValuePane,
        listBox);
    button.setMargin(new Insets(0, 1, 1, 3));
    button.setFocusPainted(false);
    return button;
  }
  public void unconfigureEditor() {
    super.unconfigureEditor();
  }
  public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
  }
  public void paintCurrentValue(Graphics g, Rectangle bounds, boolean hasFocus) {
  }
  public PropertyChangeListener createPropertyChangeListener() {
    return new MetalPropertyChangeListener();
  }
  public void configureEditor() {
    super.configureEditor();
  }
  protected void removeListeners() {
    if (propertyChangeListener != null) {
      comboBox.removePropertyChangeListener(propertyChangeListener);
    }
  }
  public static javax.swing.plaf.ComponentUI createUI(JComponent c) {
    return new MetalComboBoxUI();
  }
  protected void editablePropertyChanged(PropertyChangeEvent e) {
  }
  public void layoutComboBox(Container parent, MetalComboBoxLayoutManager manager) {
    manager.superLayout(parent);
  }
  public void paint(Graphics g, JComponent c) {
  }
  public Dimension getMinimumSize(JComponent c) {
    if (!isMinimumSizeDirty) {
      return new Dimension(cachedMinimumSize);
    }
    Dimension size;
    if (!comboBox.isEditable() && arrowButton != null && arrowButton instanceof MetalComboBoxButton) {
      MetalComboBoxButton button = (MetalComboBoxButton) arrowButton;
      Insets buttonInsets = button.getInsets();
      Insets insets = comboBox.getInsets();
      size = getDisplaySize();
      size.width += insets.left + insets.right;
      size.width += buttonInsets.left + buttonInsets.right;
      size.width += buttonInsets.right + button.getComboIcon().getIconWidth();
      size.height += insets.top + insets.bottom;
      size.height += buttonInsets.top + buttonInsets.bottom;
      size.height = Math.max(24, size.height);
    } else if (comboBox.isEditable() && arrowButton != null && editor != null) {
      size = super.getMinimumSize(c);
      Insets margin = arrowButton.getMargin();
      size.width += margin.left + margin.right;
      size.height += margin.top + margin.bottom;
    } else {
      size = super.getMinimumSize(c);
    }
    cachedMinimumSize = new Dimension(size.width, size.height);
    isMinimumSizeDirty = false;
    return new Dimension(cachedMinimumSize);
  }
  public int getBaseline(JComponent c, int width, int height) {
    return super.getBaseline(c, width, height);
  }

  public class MetalPropertyChangeListener extends javax.swing.plaf.basic.BasicComboBoxUI.PropertyChangeHandler {
    public MetalPropertyChangeListener() {
      super();
    }
    public void propertyChange(PropertyChangeEvent e) {
      super.propertyChange(e);
      String propertyName = e.getPropertyName();
      if ("editable".equals(propertyName)) {
        if (arrowButton instanceof MetalComboBoxButton) {
          ((MetalComboBoxButton) arrowButton).setIconOnly(comboBox.isEditable());
        }
        comboBox.repaint();
      } else if ("background".equals(propertyName)) {
        arrowButton.setBackground(comboBox.getBackground());
        listBox.setBackground(comboBox.getBackground());
      } else if ("foreground".equals(propertyName)) {
        arrowButton.setForeground(comboBox.getForeground());
        listBox.setForeground(comboBox.getForeground());
      }
    }
  }

  public class MetalComboBoxLayoutManager extends javax.swing.plaf.basic.BasicComboBoxUI.ComboBoxLayoutManager {
    public MetalComboBoxLayoutManager() {
      super();
    }
    public void superLayout(Container parent) {
      super.layoutContainer(parent);
    }
    public void layoutContainer(Container parent) {
      layoutComboBox(parent, this);
    }
  }

  public class MetalComboPopup extends javax.swing.plaf.basic.BasicComboPopup {
    public MetalComboPopup(javax.swing.JComboBox<java.lang.Object> p0) {
      super(p0);
    }
    public void delegateFocus(java.awt.event.MouseEvent e) {
      super.delegateFocus(e);
    }
  }
}
