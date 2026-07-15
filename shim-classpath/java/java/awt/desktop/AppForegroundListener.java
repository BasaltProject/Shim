/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.desktop;

public interface AppForegroundListener extends java.awt.desktop.SystemEventListener {
  void appRaisedToForeground(java.awt.desktop.AppForegroundEvent p0);
  void appMovedToBackground(java.awt.desktop.AppForegroundEvent p0);
}
