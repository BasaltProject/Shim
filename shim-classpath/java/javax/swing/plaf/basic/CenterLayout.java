/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package javax.swing.plaf.basic;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.io.Serializable;

class CenterLayout implements LayoutManager, Serializable {
  public void addLayoutComponent(String name, Component comp) { }

  public void removeLayoutComponent(Component comp) { }

  public Dimension preferredLayoutSize(Container target) {
    Insets in = target.getInsets();
    Dimension d = target.getComponentCount() > 0 ? target.getComponent(0).getPreferredSize() : new Dimension();
    return new Dimension(d.width + in.left + in.right, d.height + in.top + in.bottom);
  }

  public Dimension minimumLayoutSize(Container target) {
    return preferredLayoutSize(target);
  }

  public void layoutContainer(Container target) {
    if (target.getComponentCount() == 0) {
      return;
    }
    Component c = target.getComponent(0);
    Dimension d = c.getPreferredSize();
    Insets in = target.getInsets();
    int x = in.left + (target.getWidth() - in.left - in.right - d.width) / 2;
    int y = in.top + (target.getHeight() - in.top - in.bottom - d.height) / 2;
    c.setBounds(Math.max(x, in.left), Math.max(y, in.top), d.width, d.height);
  }
}
