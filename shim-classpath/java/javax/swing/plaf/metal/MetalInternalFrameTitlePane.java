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
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.LayoutManager;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Icon;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicInternalFrameTitlePane;
import shim.swing.MetalPaint;

public class MetalInternalFrameTitlePane extends BasicInternalFrameTitlePane {
  protected boolean isPalette = false;
  protected Icon paletteCloseIcon;
  protected int paletteTitleHeight;

  public MetalInternalFrameTitlePane(JInternalFrame f) {
    super(f);
  }

  public void addNotify() {
    super.addNotify();
    setPalette(isPalette);
  }

  protected void installDefaults() {
    super.installDefaults();
    setFont(UIManager.getFont("InternalFrame.titleFont"));
    paletteTitleHeight = UIManager.getInt("InternalFrame.paletteTitleHeight");
    paletteCloseIcon = MetalIconFactory.getInternalFrameCloseIcon(16);
    maxIcon = MetalIconFactory.getInternalFrameMaximizeIcon(16);
    minIcon = MetalIconFactory.getInternalFrameAltMaximizeIcon(16);
    iconIcon = MetalIconFactory.getInternalFrameMinimizeIcon(16);
    closeIcon = MetalIconFactory.getInternalFrameCloseIcon(16);
  }

  protected void uninstallDefaults() {
    super.uninstallDefaults();
  }

  protected void createButtons() {
    super.createButtons();
    setButtonIcons();
  }

  protected void assembleSystemMenu() {
    menuBar = createSystemMenuBar();
  }

  protected void addSystemMenuItems(JMenu systemMenu) {
  }

  protected void showSystemMenu() {
  }

  protected void addSubComponents() {
    add(iconButton);
    add(maxButton);
    add(closeButton);
  }

  protected PropertyChangeListener createPropertyChangeListener() {
    return new PropertyChangeHandler();
  }

  protected LayoutManager createLayout() {
    return new MetalTitlePaneLayout();
  }

  public void paintPalette(Graphics g) {
    g.setColor(UIManager.getColor("InternalFrame.activeTitleBackground"));
    g.fillRect(0, 0, getWidth(), getHeight());
  }

  public void setPalette(boolean b) {
    isPalette = b;
    revalidate();
  }

  public void paintComponent(Graphics g) {
    if (isPalette) {
      paintPalette(g);
      return;
    }
    boolean leftToRight = frame.getComponentOrientation().isLeftToRight();
    boolean isSelected = frame.isSelected();
    int width = getWidth();
    int height = getHeight();
    Color background;
    if (isSelected) {
      background = selectedTitleColor;
      if (!MetalPaint.drawGradient(this, g, "InternalFrame.activeTitleGradient", 0, 0, width, height, true)) {
        g.setColor(background);
        g.fillRect(0, 0, width, height);
      }
    } else {
      g.setColor(notSelectedTitleColor);
      g.fillRect(0, 0, width, height);
    }
    int titleLength;
    int xOffset = leftToRight ? 5 : width - 5;
    String frameTitle = frame.getTitle();
    Icon icon = frame.getFrameIcon();
    if (icon != null) {
      if (!leftToRight) {
        xOffset -= icon.getIconWidth();
      }
      int iconY = ((height / 2) - (icon.getIconHeight() / 2));
      icon.paintIcon(frame, g, xOffset, iconY);
      xOffset += leftToRight ? icon.getIconWidth() + 5 : -5;
    }
    if (frameTitle != null) {
      java.awt.Font f = getFont();
      g.setFont(f);
      java.awt.FontMetrics fm = getFontMetrics(f);
      int fHeight = fm.getHeight();
      g.setColor(isSelected ? selectedTextColor : notSelectedTextColor);
      int yOffset = ((height - fm.getHeight()) / 2) + fm.getAscent();
      int buttonsW = 0;
      if (frame.isClosable()) {
        buttonsW += closeButton.getWidth() + 4;
      }
      if (frame.isMaximizable()) {
        buttonsW += maxButton.getWidth() + 4;
      }
      if (frame.isIconifiable()) {
        buttonsW += iconButton.getWidth() + 4;
      }
      if (leftToRight) {
        titleLength = width - buttonsW - xOffset - 3;
        frameTitle = getTitle(frameTitle, fm, titleLength);
      } else {
        titleLength = xOffset - buttonsW - 3;
        frameTitle = getTitle(frameTitle, fm, titleLength);
        xOffset -= fm.stringWidth(frameTitle);
      }
      g.drawString(frameTitle, xOffset, yOffset);
    }
  }

  class MetalTitlePaneLayout extends TitlePaneLayout {
    public void addLayoutComponent(String name, Component c) {
    }

    public void removeLayoutComponent(Component c) {
    }

    public Dimension preferredLayoutSize(Container c) {
      return minimumLayoutSize(c);
    }

    public Dimension minimumLayoutSize(Container c) {
      Dimension d = super.minimumLayoutSize(c);
      if (isPalette) {
        return new Dimension(d.width, paletteTitleHeight);
      }
      return d;
    }

    public void layoutContainer(Container c) {
      boolean leftToRight = frame.getComponentOrientation().isLeftToRight();
      int w = getWidth();
      int x = leftToRight ? w : 0;
      int y = 2;
      int spacing;
      int buttonHeight = closeButton.getIcon().getIconHeight();
      int buttonWidth = closeButton.getIcon().getIconWidth();
      if (frame.isClosable()) {
        spacing = 4;
        x += leftToRight ? -spacing - buttonWidth : spacing;
        closeButton.setBounds(x, y, buttonWidth, buttonHeight);
        if (!leftToRight) {
          x += buttonWidth;
        }
      }
      if (frame.isMaximizable() && !isPalette) {
        spacing = frame.isClosable() ? 10 : 4;
        x += leftToRight ? -spacing - buttonWidth : spacing;
        maxButton.setBounds(x, y, buttonWidth, buttonHeight);
        if (!leftToRight) {
          x += buttonWidth;
        }
      }
      if (frame.isIconifiable() && !isPalette) {
        spacing = frame.isMaximizable() ? 2 : (frame.isClosable() ? 10 : 4);
        x += leftToRight ? -spacing - buttonWidth : spacing;
        iconButton.setBounds(x, y, buttonWidth, buttonHeight);
        if (!leftToRight) {
          x += buttonWidth;
        }
      }
    }
  }

  class PropertyChangeHandler extends BasicInternalFrameTitlePane.PropertyChangeHandler {
    public void propertyChange(PropertyChangeEvent evt) {
      String prop = evt.getPropertyName();
      if ("selected".equals(prop)) {
        Boolean b = (Boolean) evt.getNewValue();
        iconButton.putClientProperty("paintActive", b);
        closeButton.putClientProperty("paintActive", b);
        maxButton.putClientProperty("paintActive", b);
      } else if ("JInternalFrame.isPalette".equals(prop)) {
        if (evt.getNewValue() != null) {
          setPalette(((Boolean) evt.getNewValue()).booleanValue());
        } else {
          setPalette(false);
        }
      }
      super.propertyChange(evt);
    }
  }
}
