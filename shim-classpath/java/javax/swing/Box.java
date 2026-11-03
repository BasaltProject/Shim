/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing;

import java.awt.AWTError;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.LayoutManager;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;

public class Box extends JComponent implements Accessible {
  public Box(int axis) {
    super();
    super.setLayout(new BoxLayout(this, axis));
  }

  public static Box createHorizontalBox() { return new Box(BoxLayout.X_AXIS); }

  public static Box createVerticalBox() { return new Box(BoxLayout.Y_AXIS); }

  public static java.awt.Component createRigidArea(Dimension d) {
    return new Filler(new Dimension(d), new Dimension(d), new Dimension(d));
  }

  public static java.awt.Component createHorizontalStrut(int width) {
    return new Filler(new Dimension(width, 0), new Dimension(width, 0), new Dimension(width, Short.MAX_VALUE));
  }

  public static java.awt.Component createVerticalStrut(int height) {
    return new Filler(new Dimension(0, height), new Dimension(0, height), new Dimension(Short.MAX_VALUE, height));
  }

  public static java.awt.Component createGlue() {
    return new Filler(new Dimension(0, 0), new Dimension(0, 0), new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));
  }

  public static java.awt.Component createHorizontalGlue() {
    return new Filler(new Dimension(0, 0), new Dimension(0, 0), new Dimension(Short.MAX_VALUE, 0));
  }

  public static java.awt.Component createVerticalGlue() {
    return new Filler(new Dimension(0, 0), new Dimension(0, 0), new Dimension(0, Short.MAX_VALUE));
  }

  public void setLayout(LayoutManager l) { throw new AWTError("Illegal request"); }

  protected void paintComponent(Graphics g) {
    if (ui != null) {
      super.paintComponent(g);
    } else if (isOpaque()) {
      g.setColor(getBackground());
      g.fillRect(0, 0, getWidth(), getHeight());
    }
  }

  public AccessibleContext getAccessibleContext() {
    if (accessibleContext == null) {
      accessibleContext = new AccessibleBox();
    }
    return accessibleContext;
  }

  protected class AccessibleBox extends AccessibleJComponent {
    protected AccessibleBox() { }

    public AccessibleRole getAccessibleRole() { return AccessibleRole.FILLER; }
  }

  public static class Filler extends JComponent implements Accessible {
    public Filler(Dimension min, Dimension pref, Dimension max) {
      setMinimumSize(min);
      setPreferredSize(pref);
      setMaximumSize(max);
    }

    public void changeShape(Dimension min, Dimension pref, Dimension max) {
      setMinimumSize(min);
      setPreferredSize(pref);
      setMaximumSize(max);
      revalidate();
    }

    protected void paintComponent(Graphics g) {
      if (ui != null) {
        super.paintComponent(g);
      } else if (isOpaque()) {
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());
      }
    }

    public AccessibleContext getAccessibleContext() {
      if (accessibleContext == null) {
        accessibleContext = new AccessibleBoxFiller();
      }
      return accessibleContext;
    }

    protected class AccessibleBoxFiller extends AccessibleAWTComponent {
      protected AccessibleBoxFiller() { }

      public AccessibleRole getAccessibleRole() { return AccessibleRole.FILLER; }
    }
  }
}
