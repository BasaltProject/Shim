/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing;

public interface Icon {
  void paintIcon(java.awt.Component p0, java.awt.Graphics p1, int p2, int p3);
  int getIconWidth();
  int getIconHeight();
}
