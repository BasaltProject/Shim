/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt;

public interface LayoutManager2 extends java.awt.LayoutManager {
  void addLayoutComponent(java.awt.Component p0, java.lang.Object p1);
  void invalidateLayout(java.awt.Container p0);
  java.awt.Dimension maximumLayoutSize(java.awt.Container p0);
  float getLayoutAlignmentX(java.awt.Container p0);
  float getLayoutAlignmentY(java.awt.Container p0);
}
