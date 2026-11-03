/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.desktop;

public interface ScreenSleepListener extends java.awt.desktop.SystemEventListener {
  void screenAboutToSleep(java.awt.desktop.ScreenSleepEvent p0);
  void screenAwoke(java.awt.desktop.ScreenSleepEvent p0);
}
