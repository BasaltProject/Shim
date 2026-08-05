/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing;

import java.awt.Component;
import java.awt.Container;
import java.io.Serializable;
import java.util.Comparator;

public class LayoutFocusTraversalPolicy extends SortingFocusTraversalPolicy implements Serializable {

  public LayoutFocusTraversalPolicy() { super(new LayoutComparator()); }

  LayoutFocusTraversalPolicy(Comparator<? super Component> c) { super(c); }

  private static final class LayoutComparator implements Comparator<Component>, Serializable {
    public int compare(Component a, Component b) {
      if (a == b) {
        return 0;
      }
      int dy = a.getY() - b.getY();
      if (dy != 0) {
        return dy;
      }
      return a.getX() - b.getX();
    }
  }

}
