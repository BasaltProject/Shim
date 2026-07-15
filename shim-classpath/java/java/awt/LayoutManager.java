/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt;

public interface LayoutManager {
  void removeLayoutComponent(java.awt.Component p0);
  void addLayoutComponent(java.lang.String p0, java.awt.Component p1);
  void layoutContainer(java.awt.Container p0);
  java.awt.Dimension preferredLayoutSize(java.awt.Container p0);
  java.awt.Dimension minimumLayoutSize(java.awt.Container p0);
}
