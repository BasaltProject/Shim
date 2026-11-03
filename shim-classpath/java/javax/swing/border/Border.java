/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.border;

public interface Border {
  boolean isBorderOpaque();
  void paintBorder(java.awt.Component p0, java.awt.Graphics p1, int p2, int p3, int p4, int p5);
  java.awt.Insets getBorderInsets(java.awt.Component p0);
}
