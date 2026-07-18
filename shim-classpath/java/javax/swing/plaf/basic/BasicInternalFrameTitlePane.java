/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package javax.swing.plaf.basic;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.UIResource;

public class BasicInternalFrameTitlePane extends JComponent {
  protected JMenuBar menuBar;
  protected JButton iconButton;
  protected JButton maxButton;
  protected JButton closeButton;
  protected JMenu windowMenu;
  protected JInternalFrame frame;
  protected Color selectedTitleColor;
  protected Color selectedTextColor;
  protected Color notSelectedTitleColor;
  protected Color notSelectedTextColor;
  protected Icon maxIcon;
  protected Icon minIcon;
  protected Icon iconIcon;
  protected Icon closeIcon;
  protected PropertyChangeListener propertyChangeListener;
  protected Action closeAction;
  protected Action maximizeAction;
  protected Action iconifyAction;
  protected Action restoreAction;
  protected Action moveAction;
  protected Action sizeAction;
  protected static final String CLOSE_CMD = "Close";
  protected static final String ICONIFY_CMD = "Minimize";
  protected static final String RESTORE_CMD = "Restore";
  protected static final String MAXIMIZE_CMD = "Maximize";
  protected static final String MOVE_CMD = "Move";
  protected static final String SIZE_CMD = "Size";

  public BasicInternalFrameTitlePane(JInternalFrame f) {
    this.frame = f;
    installTitlePane();
  }

  protected void installTitlePane() {
    installDefaults();
    installListeners();
    createActions();
    enableActions();
    createActionMap();
    setLayout(createLayout());
    assembleSystemMenu();
    createButtons();
    addSubComponents();
    updateOpacity();
  }

  private void updateOpacity() {
    setOpaque(false);
  }

  private void createActionMap() {
  }

  protected void addSubComponents() {
    add(menuBar);
    add(iconButton);
    add(maxButton);
    add(closeButton);
  }

  protected void createActions() {
    maximizeAction = new MaximizeAction();
    iconifyAction = new IconifyAction();
    closeAction = new CloseAction();
    restoreAction = new RestoreAction();
    moveAction = new MoveAction();
    sizeAction = new SizeAction();
  }

  protected void installListeners() {
    if (propertyChangeListener == null) {
      propertyChangeListener = createPropertyChangeListener();
    }
    frame.addPropertyChangeListener(propertyChangeListener);
  }

  protected void uninstallListeners() {
    frame.removePropertyChangeListener(propertyChangeListener);
  }

  protected void installDefaults() {
    maxIcon = UIManager.getIcon("InternalFrame.maximizeIcon");
    minIcon = UIManager.getIcon("InternalFrame.minimizeIcon");
    iconIcon = UIManager.getIcon("InternalFrame.iconifyIcon");
    closeIcon = UIManager.getIcon("InternalFrame.closeIcon");
    selectedTitleColor = UIManager.getColor("InternalFrame.activeTitleBackground");
    selectedTextColor = UIManager.getColor("InternalFrame.activeTitleForeground");
    notSelectedTitleColor = UIManager.getColor("InternalFrame.inactiveTitleBackground");
    notSelectedTextColor = UIManager.getColor("InternalFrame.inactiveTitleForeground");
    setFont(UIManager.getFont("InternalFrame.titleFont"));
  }

  protected void uninstallDefaults() {
  }

  protected void createButtons() {
    iconButton = new NoFocusButton("InternalFrame.iconifyButtonAccessibleName");
    iconButton.addActionListener(iconifyAction);
    maxButton = new NoFocusButton("InternalFrame.maximizeButtonAccessibleName");
    maxButton.addActionListener(maximizeAction);
    closeButton = new NoFocusButton("InternalFrame.closeButtonAccessibleName");
    closeButton.addActionListener(closeAction);
    setButtonIcons();
  }

  protected void setButtonIcons() {
    if (frame.isIcon()) {
      if (minIcon != null) {
        iconButton.setIcon(minIcon);
      }
      if (maxIcon != null) {
        maxButton.setIcon(maxIcon);
      }
    } else if (frame.isMaximum()) {
      if (iconIcon != null) {
        iconButton.setIcon(iconIcon);
      }
      if (minIcon != null) {
        maxButton.setIcon(minIcon);
      }
    } else {
      if (iconIcon != null) {
        iconButton.setIcon(iconIcon);
      }
      if (maxIcon != null) {
        maxButton.setIcon(maxIcon);
      }
    }
    if (closeIcon != null) {
      closeButton.setIcon(closeIcon);
    }
  }

  protected void assembleSystemMenu() {
    menuBar = createSystemMenuBar();
    windowMenu = createSystemMenu();
    menuBar.add(windowMenu);
    addSystemMenuItems(windowMenu);
    enableActions();
  }

  protected void addSystemMenuItems(JMenu systemMenu) {
  }

  protected JMenu createSystemMenu() {
    return new JMenu("");
  }

  protected JMenuBar createSystemMenuBar() {
    menuBar = new SystemMenuBar();
    menuBar.setBorderPainted(false);
    return menuBar;
  }

  protected void showSystemMenu() {
  }

  protected void enableActions() {
    restoreAction.setEnabled(frame.isMaximum() || frame.isIcon());
    maximizeAction.setEnabled((frame.isMaximizable() && !frame.isMaximum() && !frame.isIcon())
        || (frame.isMaximizable() && frame.isIcon()));
    iconifyAction.setEnabled(frame.isIconifiable() && !frame.isIcon());
    closeAction.setEnabled(frame.isClosable());
    sizeAction.setEnabled(false);
    moveAction.setEnabled(false);
  }

  protected PropertyChangeListener createPropertyChangeListener() {
    return new PropertyChangeHandler();
  }

  protected LayoutManager createLayout() {
    return new TitlePaneLayout();
  }

  protected void postClosingEvent(JInternalFrame frame) {
    java.beans.PropertyChangeSupport unused = null;
    frame.doDefaultCloseAction();
  }

  protected void paintTitleBackground(Graphics g) {
    g.setColor(frame.isSelected() ? selectedTitleColor : notSelectedTitleColor);
    g.fillRect(0, 0, getWidth(), getHeight());
  }

  public void paintComponent(Graphics g) {
    paintTitleBackground(g);
    if (frame.getTitle() != null) {
      boolean isSelected = frame.isSelected();
      Font f = g.getFont();
      g.setFont(getFont());
      g.setColor(isSelected ? selectedTextColor : notSelectedTextColor);
      FontMetrics fm = getFontMetrics(getFont());
      int baseline = (getHeight() + fm.getAscent() - fm.getLeading() - fm.getDescent()) / 2;
      int xOffset = 0;
      Icon icon = frame.getFrameIcon();
      int leftBound = icon == null ? 0 : icon.getIconWidth() + 5;
      int rightBound = getWidth();
      if (frame.isIconifiable()) {
        rightBound = Math.min(rightBound, iconButton.getX());
      }
      if (frame.isMaximizable()) {
        rightBound = Math.min(rightBound, maxButton.getX());
      }
      if (frame.isClosable()) {
        rightBound = Math.min(rightBound, closeButton.getX());
      }
      int titleW = rightBound - 3 - leftBound - 3;
      String title = getTitle(frame.getTitle(), fm, titleW);
      xOffset = leftBound + 3;
      g.drawString(title, xOffset, baseline);
      g.setFont(f);
    }
  }

  protected String getTitle(String text, FontMetrics fm, int availableWidth) {
    if (text == null) {
      return null;
    }
    if (fm.stringWidth(text) <= availableWidth) {
      return text;
    }
    String clipString = "...";
    int width = fm.stringWidth(clipString);
    int nChars = 0;
    for (int max = text.length(); nChars < max; nChars++) {
      width += fm.charWidth(text.charAt(nChars));
      if (width > availableWidth) {
        break;
      }
    }
    return text.substring(0, nChars) + clipString;
  }

  public class PropertyChangeHandler implements PropertyChangeListener {
    public PropertyChangeHandler() {
    }

    public void propertyChange(PropertyChangeEvent evt) {
      String prop = evt.getPropertyName();
      if ("selected".equals(prop)) {
        repaint();
      } else if ("icon".equals(prop) || "maximum".equals(prop)) {
        setButtonIcons();
        enableActions();
        repaint();
      } else if ("closable".equals(prop) || "maximizable".equals(prop) || "iconable".equals(prop)
          || "title".equals(prop)) {
        enableActions();
        revalidate();
        repaint();
      }
    }
  }

  public class TitlePaneLayout implements LayoutManager {
    public TitlePaneLayout() {
    }

    public void addLayoutComponent(String name, Component c) {
    }

    public void removeLayoutComponent(Component c) {
    }

    public Dimension preferredLayoutSize(Container c) {
      return minimumLayoutSize(c);
    }

    public Dimension minimumLayoutSize(Container c) {
      int width = 30;
      if (frame.isClosable()) {
        width += 21;
      }
      if (frame.isMaximizable()) {
        width += 16 + (frame.isClosable() ? 10 : 4);
      }
      if (frame.isIconifiable()) {
        width += 16 + (frame.isMaximizable() ? 2 : (frame.isClosable() ? 10 : 4));
      }
      FontMetrics fm = frame.getFontMetrics(getFont());
      String frameTitle = frame.getTitle();
      int title_w = frameTitle != null ? fm.stringWidth(frameTitle) : 0;
      int title_length = frameTitle != null ? frameTitle.length() : 0;
      if (title_length > 2) {
        int subtitle_w = fm.stringWidth(frame.getTitle().substring(0, 2) + "...");
        width += Math.min(title_w, subtitle_w);
      } else {
        width += title_w;
      }
      return new Dimension(width, computeHeight());
    }

    public void layoutContainer(Container c) {
      boolean leftToRight = frame.getComponentOrientation().isLeftToRight();
      int w = getWidth();
      int h = getHeight();
      int x;
      int buttonHeight = closeButton.getIcon() == null ? h : closeButton.getIcon().getIconHeight();
      int buttonWidth = closeButton.getIcon() == null ? h : closeButton.getIcon().getIconWidth();
      int y = (h - buttonHeight) / 2;
      x = leftToRight ? w : 0;
      int spacing = 0;
      menuBar.setBounds(leftToRight ? 2 : w - 18, 2, 16, h - 4);
      if (frame.isClosable()) {
        spacing = 2;
        x += leftToRight ? -spacing - buttonWidth : spacing;
        closeButton.setBounds(x, y, buttonWidth, buttonHeight);
        if (!leftToRight) {
          x += buttonWidth;
        }
      }
      if (frame.isMaximizable()) {
        spacing = frame.isClosable() ? 10 : 4;
        x += leftToRight ? -spacing - buttonWidth : spacing;
        maxButton.setBounds(x, y, buttonWidth, buttonHeight);
        if (!leftToRight) {
          x += buttonWidth;
        }
      }
      if (frame.isIconifiable()) {
        spacing = frame.isMaximizable() ? 2 : (frame.isClosable() ? 10 : 4);
        x += leftToRight ? -spacing - buttonWidth : spacing;
        iconButton.setBounds(x, y, buttonWidth, buttonHeight);
      }
    }
  }

  int computeHeight() {
    FontMetrics fm = frame.getFontMetrics(getFont());
    int fontHeight = fm.getHeight() + 7;
    Icon icon = frame.getFrameIcon();
    int iconHeight = 0;
    if (icon != null) {
      iconHeight = Math.min(icon.getIconHeight(), 16);
    }
    iconHeight += 5;
    return Math.max(fontHeight, iconHeight);
  }

  public class MaximizeAction extends AbstractAction {
    public MaximizeAction() {
      super(UIManager.getString("InternalFrameTitlePane.maximizeButtonText"));
    }

    public void actionPerformed(ActionEvent evt) {
      if (frame.isMaximizable()) {
        if (frame.isMaximum() && frame.isIcon()) {
          try {
            frame.setIcon(false);
          } catch (PropertyVetoException e) {
            return;
          }
        } else if (!frame.isMaximum()) {
          try {
            frame.setMaximum(true);
          } catch (PropertyVetoException e) {
            return;
          }
        } else {
          try {
            frame.setMaximum(false);
          } catch (PropertyVetoException e) {
            return;
          }
        }
      }
    }
  }

  public class IconifyAction extends AbstractAction {
    public IconifyAction() {
      super(UIManager.getString("InternalFrameTitlePane.minimizeButtonText"));
    }

    public void actionPerformed(ActionEvent e) {
      if (frame.isIconifiable()) {
        if (!frame.isIcon()) {
          try {
            frame.setIcon(true);
          } catch (PropertyVetoException e1) {
            return;
          }
        } else {
          try {
            frame.setIcon(false);
          } catch (PropertyVetoException e1) {
            return;
          }
        }
      }
    }
  }

  public class CloseAction extends AbstractAction {
    public CloseAction() {
      super(UIManager.getString("InternalFrameTitlePane.closeButtonText"));
    }

    public void actionPerformed(ActionEvent e) {
      if (frame.isClosable()) {
        frame.doDefaultCloseAction();
      }
    }
  }

  public class RestoreAction extends AbstractAction {
    public RestoreAction() {
      super(UIManager.getString("InternalFrameTitlePane.restoreButtonText"));
    }

    public void actionPerformed(ActionEvent evt) {
      if (frame.isMaximizable() && frame.isMaximum() && frame.isIcon()) {
        try {
          frame.setIcon(false);
        } catch (PropertyVetoException e) {
          return;
        }
      } else if (frame.isMaximizable() && frame.isMaximum()) {
        try {
          frame.setMaximum(false);
        } catch (PropertyVetoException e) {
          return;
        }
      } else if (frame.isIconifiable() && frame.isIcon()) {
        try {
          frame.setIcon(false);
        } catch (PropertyVetoException e) {
          return;
        }
      }
    }
  }

  public class MoveAction extends AbstractAction {
    public MoveAction() {
      super(UIManager.getString("InternalFrameTitlePane.moveButtonText"));
    }

    public void actionPerformed(ActionEvent e) {
    }
  }

  public class SizeAction extends AbstractAction {
    public SizeAction() {
      super(UIManager.getString("InternalFrameTitlePane.sizeButtonText"));
    }

    public void actionPerformed(ActionEvent e) {
    }
  }

  public class SystemMenuBar extends JMenuBar {
    public SystemMenuBar() {
    }

    public boolean isFocusTraversable() {
      return false;
    }

    public void requestFocus() {
    }

    public void paint(Graphics g) {
      Icon icon = frame.getFrameIcon();
      if (icon != null) {
        icon.paintIcon(this, g, 0, 0);
      }
    }

    public boolean isOpaque() {
      return true;
    }
  }

  private class NoFocusButton extends JButton {
    NoFocusButton(String uiKey) {
      setFocusPainted(false);
      setMargin(new Insets(0, 0, 0, 0));
      setOpaque(true);
      setFocusable(false);
    }

    public boolean isFocusTraversable() {
      return false;
    }

    public void requestFocus() {
    }
  }
}
