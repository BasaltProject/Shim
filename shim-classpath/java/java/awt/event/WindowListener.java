/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.event;

public interface WindowListener extends java.util.EventListener {
  void windowOpened(java.awt.event.WindowEvent p0);
  void windowClosing(java.awt.event.WindowEvent p0);
  void windowClosed(java.awt.event.WindowEvent p0);
  void windowIconified(java.awt.event.WindowEvent p0);
  void windowDeiconified(java.awt.event.WindowEvent p0);
  void windowActivated(java.awt.event.WindowEvent p0);
  void windowDeactivated(java.awt.event.WindowEvent p0);
}
