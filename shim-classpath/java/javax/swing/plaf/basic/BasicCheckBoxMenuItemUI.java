/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */


package javax.swing.plaf.basic;

import java.awt.event.MouseEvent;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;

public class BasicCheckBoxMenuItemUI extends BasicMenuItemUI {
  public BasicCheckBoxMenuItemUI() {
  }

  public static javax.swing.plaf.ComponentUI createUI(JComponent c) {
    return new BasicCheckBoxMenuItemUI();
  }

  protected String getPropertyPrefix() {
    return "CheckBoxMenuItem";
  }

  public void processMouseEvent(JMenuItem item, MouseEvent e, MenuElement[] path, MenuSelectionManager manager) {
    java.awt.Point p = e.getPoint();
    if (p.x >= 0 && p.x < item.getWidth() && p.y >= 0 && p.y < item.getHeight()) {
      if (e.getID() == MouseEvent.MOUSE_RELEASED) {
        manager.clearSelectedPath();
        item.doClick(0);
      } else {
        manager.setSelectedPath(path);
      }
    } else if (item.getModel().isArmed()) {
      MenuElement[] newPath = new MenuElement[path.length - 1];
      for (int i = 0, c = path.length - 1; i < c; i++) {
        newPath[i] = path[i];
      }
      manager.setSelectedPath(newPath);
    }
  }
}
