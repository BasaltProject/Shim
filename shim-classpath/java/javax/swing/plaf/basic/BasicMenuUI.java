/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */


package javax.swing.plaf.basic;

import java.awt.Dimension;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MenuListener;
import javax.swing.event.MouseInputListener;

public class BasicMenuUI extends BasicMenuItemUI {
  protected ChangeListener changeListener;
  protected MenuListener menuListener;
  private int lastMnemonic;

  public BasicMenuUI() {
  }

  public static javax.swing.plaf.ComponentUI createUI(JComponent x) {
    return new BasicMenuUI();
  }

  protected String getPropertyPrefix() {
    return "Menu";
  }

  protected void installDefaults() {
    super.installDefaults();
    ((JMenu) menuItem).setDelay(200);
    if (arrowIcon == null) {
      arrowIcon = javax.swing.UIManager.getIcon("Menu.arrowIcon");
    }
  }

  protected void uninstallDefaults() {
    super.uninstallDefaults();
  }

  protected void installListeners() {
    super.installListeners();
    if (changeListener == null) {
      changeListener = createChangeListener(menuItem);
    }
    if (changeListener != null) {
      menuItem.addChangeListener(changeListener);
    }
    if (menuListener == null) {
      menuListener = createMenuListener(menuItem);
    }
    if (menuListener != null) {
      ((JMenu) menuItem).addMenuListener(menuListener);
    }
  }

  protected void uninstallListeners() {
    super.uninstallListeners();
    if (changeListener != null) {
      menuItem.removeChangeListener(changeListener);
    }
    if (menuListener != null) {
      ((JMenu) menuItem).removeMenuListener(menuListener);
    }
    changeListener = null;
    menuListener = null;
  }

  protected void installKeyboardActions() {
  }

  protected void uninstallKeyboardActions() {
  }

  protected MouseInputListener createMouseInputListener(JComponent c) {
    return new MouseInputHandler() {
      public void mousePressed(MouseEvent e) {
        JMenu menu = (JMenu) menuItem;
        if (!menu.isEnabled()) {
          return;
        }
        MenuSelectionManager manager = MenuSelectionManager.defaultManager();
        if (menu.isTopLevelMenu()) {
          if (menu.isSelected() && menu.getPopupMenu().isShowing()) {
            manager.clearSelectedPath();
          } else {
            java.awt.Container cnt = menu.getParent();
            if (cnt != null && cnt instanceof javax.swing.JMenuBar) {
              MenuElement[] me = new MenuElement[2];
              me[0] = (javax.swing.JMenuBar) cnt;
              me[1] = menu;
              manager.setSelectedPath(me);
            }
          }
        }
        MenuElement[] selectedPath = manager.getSelectedPath();
        if (selectedPath.length > 0 && selectedPath[selectedPath.length - 1] != menu.getPopupMenu()) {
          if (menu.isTopLevelMenu() || menu.getDelay() == 0) {
            appendPath(selectedPath, menu.getPopupMenu());
          }
        }
      }

      public void mouseReleased(MouseEvent e) {
      }

      public void mouseEntered(MouseEvent e) {
        JMenu menu = (JMenu) menuItem;
        if (!menu.isEnabled()) {
          return;
        }
        MenuSelectionManager manager = MenuSelectionManager.defaultManager();
        MenuElement[] selectedPath = manager.getSelectedPath();
        if (!menu.isTopLevelMenu() || (selectedPath.length > 0 && selectedPath[0] == menu.getParent())) {
          manager.setSelectedPath(getPath());
        }
      }
    };
  }

  private void appendPath(MenuElement[] path, MenuElement elem) {
    MenuElement[] newPath = new MenuElement[path.length + 1];
    System.arraycopy(path, 0, newPath, 0, path.length);
    newPath[path.length] = elem;
    MenuSelectionManager.defaultManager().setSelectedPath(newPath);
  }

  protected javax.swing.event.MenuDragMouseListener createMenuDragMouseListener(JComponent c) {
    return null;
  }

  protected javax.swing.event.MenuKeyListener createMenuKeyListener(JComponent c) {
    return null;
  }

  protected java.beans.PropertyChangeListener createPropertyChangeListener(JComponent c) {
    return super.createPropertyChangeListener(c);
  }

  protected MenuListener createMenuListener(JComponent c) {
    return null;
  }

  protected ChangeListener createChangeListener(JComponent c) {
    return null;
  }

  protected void setupPostTimer(JMenu menu) {
  }

  public Dimension getMinimumSize(JComponent c) {
    if (((JMenu) c).isTopLevelMenu()) {
      return c.getPreferredSize();
    }
    return null;
  }

  public Dimension getMaximumSize(JComponent c) {
    if (((JMenu) c).isTopLevelMenu()) {
      Dimension d = c.getPreferredSize();
      return new Dimension(d.width, Short.MAX_VALUE);
    }
    return null;
  }

  public class ChangeHandler implements ChangeListener {
    public JMenu menu;
    public BasicMenuUI ui;
    public boolean isSelected = false;
    public java.awt.Component wasFocused;

    public ChangeHandler(JMenu m, BasicMenuUI ui) {
      menu = m;
      this.ui = ui;
    }

    public void stateChanged(ChangeEvent e) {
    }
  }
}
