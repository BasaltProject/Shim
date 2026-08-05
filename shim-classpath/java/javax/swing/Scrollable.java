/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing;

public interface Scrollable {
  java.awt.Dimension getPreferredScrollableViewportSize();
  int getScrollableUnitIncrement(java.awt.Rectangle p0, int p1, int p2);
  int getScrollableBlockIncrement(java.awt.Rectangle p0, int p1, int p2);
  boolean getScrollableTracksViewportWidth();
  boolean getScrollableTracksViewportHeight();
}
