/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.event;

public interface MenuKeyListener extends java.util.EventListener {
  void menuKeyPressed(javax.swing.event.MenuKeyEvent p0);
  void menuKeyReleased(javax.swing.event.MenuKeyEvent p0);
  void menuKeyTyped(javax.swing.event.MenuKeyEvent p0);
}
