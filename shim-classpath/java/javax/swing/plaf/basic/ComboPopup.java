/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.plaf.basic;

public interface ComboPopup {
  void uninstallingUI();
  java.awt.event.MouseListener getMouseListener();
  java.awt.event.MouseMotionListener getMouseMotionListener();
  java.awt.event.KeyListener getKeyListener();
  javax.swing.JList<java.lang.Object> getList();
  boolean isVisible();
  void show();
  void hide();
}
