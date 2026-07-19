/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.plaf.basic;

import java.awt.Container;
import java.awt.Dimension;
import javax.swing.BoxLayout;
import javax.swing.JPopupMenu;
import javax.swing.plaf.UIResource;

public class DefaultMenuLayout extends BoxLayout implements UIResource {
  public DefaultMenuLayout(Container target, int axis) { super(target, axis); }

  public Dimension preferredLayoutSize(Container target) {
    if (target instanceof JPopupMenu) {
      JPopupMenu pop = (JPopupMenu) target;
      pop.putClientProperty("JPopupMenu.maxIconWidth", null);
      if (pop.getComponentCount() == 0) {
        return new Dimension(0, 0);
      }
    }
    super.invalidateLayout(target);
    return super.preferredLayoutSize(target);
  }
}
