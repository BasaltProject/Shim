/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.event;

public interface ComponentListener extends java.util.EventListener {
  void componentResized(java.awt.event.ComponentEvent p0);
  void componentMoved(java.awt.event.ComponentEvent p0);
  void componentShown(java.awt.event.ComponentEvent p0);
  void componentHidden(java.awt.event.ComponentEvent p0);
}
