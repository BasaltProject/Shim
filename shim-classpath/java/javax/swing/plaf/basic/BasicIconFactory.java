/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package javax.swing.plaf.basic;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import javax.swing.AbstractButton;
import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.UIManager;
import javax.swing.plaf.UIResource;

public class BasicIconFactory implements java.io.Serializable {
  private static Icon checkBoxIcon;
  private static Icon radioButtonIcon;
  private static Icon checkBoxMenuItemIcon;
  private static Icon radioButtonMenuItemIcon;
  private static Icon menuItemCheckIcon;
  private static Icon menuItemArrowIcon;
  private static Icon menuArrowIcon;

  public BasicIconFactory() { }

  public static Icon getMenuItemCheckIcon() {
    if (menuItemCheckIcon == null) {
      menuItemCheckIcon = new SizedIcon(9, 9, 0);
    }
    return menuItemCheckIcon;
  }

  public static Icon getMenuItemArrowIcon() {
    if (menuItemArrowIcon == null) {
      menuItemArrowIcon = new SizedIcon(4, 8, 1);
    }
    return menuItemArrowIcon;
  }

  public static Icon getMenuArrowIcon() {
    if (menuArrowIcon == null) {
      menuArrowIcon = new SizedIcon(4, 8, 1);
    }
    return menuArrowIcon;
  }

  public static Icon getCheckBoxIcon() {
    if (checkBoxIcon == null) {
      checkBoxIcon = new SizedIcon(13, 13, 2);
    }
    return checkBoxIcon;
  }

  public static Icon getRadioButtonIcon() {
    if (radioButtonIcon == null) {
      radioButtonIcon = new SizedIcon(13, 13, 3);
    }
    return radioButtonIcon;
  }

  public static Icon getCheckBoxMenuItemIcon() {
    if (checkBoxMenuItemIcon == null) {
      checkBoxMenuItemIcon = new SizedIcon(9, 9, 0);
    }
    return checkBoxMenuItemIcon;
  }

  public static Icon getRadioButtonMenuItemIcon() {
    if (radioButtonMenuItemIcon == null) {
      radioButtonMenuItemIcon = new SizedIcon(6, 6, 4);
    }
    return radioButtonMenuItemIcon;
  }

  public static Icon createEmptyFrameIcon() {
    return new SizedIcon(14, 16, 5);
  }

  private static final class SizedIcon implements Icon, UIResource, java.io.Serializable {
    private final int width;
    private final int height;
    private final int kind;

    SizedIcon(int width, int height, int kind) {
      this.width = width;
      this.height = height;
      this.kind = kind;
    }

    public int getIconWidth() {
      return width;
    }

    public int getIconHeight() {
      return height;
    }

    public void paintIcon(Component c, Graphics g, int x, int y) {
      if (kind == 5) {
        return;
      }
      boolean selected = false;
      if (c instanceof AbstractButton) {
        ButtonModel model = ((AbstractButton) c).getModel();
        selected = model.isSelected();
        if (kind == 1) {
          selected = true;
        }
      }
      Color old = g.getColor();
      Color fg = c == null ? Color.black : c.getForeground();
      if (kind == 1) {
        g.setColor(fg);
        g.translate(x, y);
        int[] xs = {0, 0, 4};
        int[] ys = {0, 8, 4};
        g.fillPolygon(xs, ys, 3);
        g.translate(-x, -y);
      } else if (kind == 2 || kind == 3) {
        Color shadow = UIManager.getColor("controlShadow");
        g.setColor(shadow == null ? Color.gray : shadow);
        if (kind == 2) {
          g.drawRect(x, y, width - 1, height - 1);
        } else {
          g.drawOval(x, y, width - 1, height - 1);
        }
        if (selected) {
          g.setColor(fg);
          if (kind == 2) {
            g.drawLine(x + 3, y + 6, x + 5, y + 8);
            g.drawLine(x + 5, y + 8, x + 9, y + 3);
          } else {
            g.fillOval(x + 3, y + 3, 7, 7);
          }
        }
      } else if (selected) {
        g.setColor(fg);
        if (kind == 0) {
          g.drawLine(x + 1, y + 4, x + 3, y + 6);
          g.drawLine(x + 3, y + 6, x + 7, y + 1);
        } else {
          g.fillOval(x, y, width, height);
        }
      }
      g.setColor(old);
    }
  }
}
