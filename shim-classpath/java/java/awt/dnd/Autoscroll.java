/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.dnd;

public interface Autoscroll {
  java.awt.Insets getAutoscrollInsets();
  void autoscroll(java.awt.Point p0);
}
