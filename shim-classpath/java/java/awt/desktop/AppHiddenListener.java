/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.desktop;

public interface AppHiddenListener extends java.awt.desktop.SystemEventListener {
  void appHidden(java.awt.desktop.AppHiddenEvent p0);
  void appUnhidden(java.awt.desktop.AppHiddenEvent p0);
}
