/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.event;

public interface MenuDragMouseListener extends java.util.EventListener {
  void menuDragMouseEntered(javax.swing.event.MenuDragMouseEvent p0);
  void menuDragMouseExited(javax.swing.event.MenuDragMouseEvent p0);
  void menuDragMouseDragged(javax.swing.event.MenuDragMouseEvent p0);
  void menuDragMouseReleased(javax.swing.event.MenuDragMouseEvent p0);
}
