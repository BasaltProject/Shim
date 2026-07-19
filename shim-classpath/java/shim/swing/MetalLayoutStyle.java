/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.swing;

import java.awt.Container;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;

public final class MetalLayoutStyle extends LayoutStyle {
  public static final MetalLayoutStyle kInstance = new MetalLayoutStyle();

  private MetalLayoutStyle() { }

  private static boolean isToggle(JComponent c) {
    String id = c.getUIClassID();
    return "CheckBoxUI".equals(id) || "RadioButtonUI".equals(id);
  }

  private static void checkPosition(int position) {
    if (position != SwingConstants.NORTH && position != SwingConstants.SOUTH && position != SwingConstants.WEST
        && position != SwingConstants.EAST) {
      throw new IllegalArgumentException("Invalid position");
    }
  }

  public int getPreferredGap(JComponent c1, JComponent c2, ComponentPlacement type, int position, Container parent) {
    if (c1 == null || c2 == null || type == null) {
      throw new NullPointerException();
    }
    checkPosition(position);
    boolean horizontal = position == SwingConstants.EAST || position == SwingConstants.WEST;
    int base;
    switch (type) {
      case INDENT:
        if (horizontal) {
          return 12;
        }
        base = 6;
        break;
      case UNRELATED:
        base = 12;
        break;
      default:
        base = 6;
        break;
    }
    if (horizontal && (c1 instanceof JLabel) != (c2 instanceof JLabel)) {
      base += 6;
    }
    if (isToggle(c1)) {
      base -= 4;
    }
    if (isToggle(c2)) {
      base -= 4;
    }
    return Math.max(base, 0);
  }

  public int getContainerGap(JComponent c, int position, Container parent) {
    checkPosition(position);
    int gap = 12;
    if (c != null && isToggle(c)) {
      gap -= 4;
    }
    return gap;
  }
}
