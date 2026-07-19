/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.plaf.metal;

import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import javax.swing.JComponent;
import javax.swing.tree.TreePath;

public class MetalTreeUI extends javax.swing.plaf.basic.BasicTreeUI {
  public MetalTreeUI() {
  }
  protected void paintVerticalPartOfLeg(Graphics g, Rectangle clipBounds, Insets insets, TreePath path) {
    super.paintVerticalPartOfLeg(g, clipBounds, insets, path);
  }
  protected void paintHorizontalPartOfLeg(Graphics g, Rectangle clipBounds, Insets insets, Rectangle bounds, TreePath path, int row, boolean isExpanded, boolean hasBeenExpanded, boolean isLeaf) {
    super.paintHorizontalPartOfLeg(g, clipBounds, insets, bounds, path, row, isExpanded, hasBeenExpanded, isLeaf);
  }
  protected int getHorizontalLegBuffer() {
    return 3;
  }
  protected boolean isLocationInExpandControl(int row, int rowLevel, int mouseX, int mouseY) {
    return false;
  }
  protected void decodeLineStyle(Object lineStyleFlag) {
  }
  protected void paintHorizontalSeparators(Graphics g, JComponent c) {
  }
  public static javax.swing.plaf.ComponentUI createUI(JComponent x) {
    return new MetalTreeUI();
  }
  public void paint(Graphics g, JComponent c) {
    super.paint(g, c);
  }
  public void installUI(JComponent c) {
    super.installUI(c);
  }
  public void uninstallUI(JComponent c) {
    super.uninstallUI(c);
  }
}
