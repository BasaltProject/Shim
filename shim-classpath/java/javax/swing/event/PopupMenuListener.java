/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.event;

public interface PopupMenuListener extends java.util.EventListener {
  void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent p0);
  void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent p0);
  void popupMenuCanceled(javax.swing.event.PopupMenuEvent p0);
}
