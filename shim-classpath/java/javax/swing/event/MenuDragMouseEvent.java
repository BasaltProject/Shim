/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.event;

import java.awt.Component;
import java.awt.event.MouseEvent;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;

public class MenuDragMouseEvent extends MouseEvent {
  private final MenuElement[] path;
  private final MenuSelectionManager manager;

  @Deprecated
  public MenuDragMouseEvent(Component source, int id, long when, int modifiers, int x, int y, int clickCount,
      boolean popupTrigger, MenuElement[] p, MenuSelectionManager m) {
    super(source, id, when, modifiers, x, y, clickCount, popupTrigger);
    path = p;
    manager = m;
  }

  public MenuDragMouseEvent(Component source, int id, long when, int modifiers, int x, int y, int xAbs, int yAbs,
      int clickCount, boolean popupTrigger, MenuElement[] p, MenuSelectionManager m) {
    super(source, id, when, modifiers, x, y, xAbs, yAbs, clickCount, popupTrigger, NOBUTTON);
    path = p;
    manager = m;
  }

  public MenuElement[] getPath() { return path; }

  public MenuSelectionManager getMenuSelectionManager() { return manager; }
}
