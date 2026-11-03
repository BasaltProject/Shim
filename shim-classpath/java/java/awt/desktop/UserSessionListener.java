/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.desktop;

public interface UserSessionListener extends java.awt.desktop.SystemEventListener {
  void userSessionDeactivated(java.awt.desktop.UserSessionEvent p0);
  void userSessionActivated(java.awt.desktop.UserSessionEvent p0);
}
