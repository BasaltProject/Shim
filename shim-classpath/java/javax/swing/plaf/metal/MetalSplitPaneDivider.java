/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */


package javax.swing.plaf.metal;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import javax.swing.JButton;
import javax.swing.JSplitPane;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;

class MetalSplitPaneDivider extends BasicSplitPaneDivider {
  public MetalSplitPaneDivider(BasicSplitPaneUI ui) {
    super(ui);
    setLayout(new MetalDividerLayout());
  }

  public void paint(Graphics g) {
    g.setColor(MetalLookAndFeel.getControl());
    Dimension size = getSize();
    g.fillRect(0, 0, size.width, size.height);
    super.paint(g);
  }

  protected JButton createLeftOneTouchButton() {
    return super.createLeftOneTouchButton();
  }

  protected JButton createRightOneTouchButton() {
    return super.createRightOneTouchButton();
  }

  public class MetalDividerLayout implements java.awt.LayoutManager {
    private final DividerLayout delegate = new DividerLayout() {};

    MetalDividerLayout() {
    }

    public void layoutContainer(Container c) {
      delegate.layoutContainer(c);
    }

    public Dimension minimumLayoutSize(Container c) {
      return delegate.minimumLayoutSize(c);
    }

    public Dimension preferredLayoutSize(Container c) {
      return delegate.preferredLayoutSize(c);
    }

    public void removeLayoutComponent(Component c) {
    }

    public void addLayoutComponent(String string, Component c) {
    }
  }
}
