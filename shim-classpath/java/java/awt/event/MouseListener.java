/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.event;

public interface MouseListener extends java.util.EventListener {
  void mousePressed(java.awt.event.MouseEvent p0);
  void mouseReleased(java.awt.event.MouseEvent p0);
  void mouseClicked(java.awt.event.MouseEvent p0);
  void mouseExited(java.awt.event.MouseEvent p0);
  void mouseEntered(java.awt.event.MouseEvent p0);
}
