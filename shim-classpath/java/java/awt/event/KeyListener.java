/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.event;

public interface KeyListener extends java.util.EventListener {
  void keyTyped(java.awt.event.KeyEvent p0);
  void keyPressed(java.awt.event.KeyEvent p0);
  void keyReleased(java.awt.event.KeyEvent p0);
}
