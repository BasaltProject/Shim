/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Rectangle;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;

public class CellRendererPane extends Container implements Accessible {
  protected AccessibleContext accessibleContext = null;

  public CellRendererPane() {
    super();
    setLayout(null);
    setVisible(false);
  }

  public void invalidate() { }

  public void paint(Graphics g) { }

  public void update(Graphics g) { }

  protected void addImpl(Component x, Object constraints, int index) {
    if (x.getParent() == this) {
      return;
    }
    super.addImpl(x, constraints, index);
  }

  public void paintComponent(Graphics g, Component c, Container p, int x, int y, int w, int h, boolean shouldValidate) {
    if (c == null) {
      if (p != null) {
        Color oldColor = g.getColor();
        g.setColor(p.getBackground());
        g.fillRect(x, y, w, h);
        g.setColor(oldColor);
      }
      return;
    }
    if (c.getParent() != this) {
      this.add(c);
    }
    c.setBounds(x, y, w, h);
    if (shouldValidate) {
      c.validate();
    }
    Graphics cg = g.create(x, y, w, h);
    try {
      c.paint(cg);
    } finally {
      cg.dispose();
    }
    c.setBounds(-w, -h, 0, 0);
  }

  public void paintComponent(Graphics g, Component c, Container p, int x, int y, int w, int h) {
    paintComponent(g, c, p, x, y, w, h, false);
  }

  public void paintComponent(Graphics g, Component c, Container p, Rectangle r) {
    paintComponent(g, c, p, r.x, r.y, r.width, r.height);
  }

  public AccessibleContext getAccessibleContext() {
    if (accessibleContext == null) {
      accessibleContext = new AccessibleCellRendererPane();
    }
    return accessibleContext;
  }

  protected class AccessibleCellRendererPane extends AccessibleAWTContainer {
    public AccessibleRole getAccessibleRole() { return AccessibleRole.PANEL; }
  }
}
