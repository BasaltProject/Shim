/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.plaf;

import java.awt.*;
import javax.accessibility.Accessible;
import javax.swing.*;

public abstract class ComponentUI {
  public ComponentUI() {
  }
  public void update(java.awt.Graphics p0, javax.swing.JComponent p1) {
    if (p1.isOpaque()) {
      p0.setColor(p1.getBackground());
      p0.fillRect(0, 0, p1.getWidth(), p1.getHeight());
    }
    paint(p0, p1);
  }
  public boolean contains(javax.swing.JComponent p0, int p1, int p2) {
    return p0.inside(p1, p2);
  }
  public static javax.swing.plaf.ComponentUI createUI(javax.swing.JComponent p0) {
    throw new Error("ComponentUI.createUI not implemented.");
  }
  public java.awt.Dimension getMaximumSize(javax.swing.JComponent p0) {
    return getPreferredSize(p0);
  }
  public void paint(java.awt.Graphics p0, javax.swing.JComponent p1) {
    
  }
  public int getAccessibleChildrenCount(javax.swing.JComponent p0) {
    return SwingUtilities.getAccessibleChildrenCount(p0);
  }
  public javax.accessibility.Accessible getAccessibleChild(javax.swing.JComponent p0, int p1) {
    return SwingUtilities.getAccessibleChild(p0, p1);
  }
  public java.awt.Dimension getPreferredSize(javax.swing.JComponent p0) {
    return null;
  }
  public java.awt.Dimension getMinimumSize(javax.swing.JComponent p0) {
    return getPreferredSize(p0);
  }
  public int getBaseline(javax.swing.JComponent p0, int p1, int p2) {
    if (p0 == null) {
      throw new NullPointerException("Component must be non-null");
    }
    if (p1 < 0 || p2 < 0) {
      throw new IllegalArgumentException("Width and height must be >= 0");
    }
    return -1;
  }
  public java.awt.Component.BaselineResizeBehavior getBaselineResizeBehavior(javax.swing.JComponent p0) {
    if (p0 == null) {
      throw new NullPointerException("Component must be non-null");
    }
    return Component.BaselineResizeBehavior.OTHER;
  }
  public void installUI(javax.swing.JComponent p0) {
    
  }
  public void uninstallUI(javax.swing.JComponent p0) {
    
  }
}
