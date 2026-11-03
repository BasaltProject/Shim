/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.event;

import java.awt.Component;
import java.awt.event.KeyEvent;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;

public class MenuKeyEvent extends KeyEvent {
  private final MenuElement[] path;
  private final MenuSelectionManager manager;

  public MenuKeyEvent(Component source, int id, long when, int modifiers, int keyCode, char keyChar,
      MenuElement[] p, MenuSelectionManager m) {
    super(source, id, when, modifiers, keyCode, keyChar);
    path = p;
    manager = m;
  }

  public MenuElement[] getPath() { return path; }

  public MenuSelectionManager getMenuSelectionManager() { return manager; }
}
