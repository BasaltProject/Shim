/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.event;

public interface MenuListener extends java.util.EventListener {
  void menuSelected(javax.swing.event.MenuEvent p0);
  void menuDeselected(javax.swing.event.MenuEvent p0);
  void menuCanceled(javax.swing.event.MenuEvent p0);
}
