/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.desktop;

public interface SystemSleepListener extends java.awt.desktop.SystemEventListener {
  void systemAboutToSleep(java.awt.desktop.SystemSleepEvent p0);
  void systemAwoke(java.awt.desktop.SystemSleepEvent p0);
}
