/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.plaf;

public abstract class ListUI extends javax.swing.plaf.ComponentUI {
  protected ListUI() {
  }
  public abstract int locationToIndex(javax.swing.JList<?> p0, java.awt.Point p1);
  public abstract java.awt.Rectangle getCellBounds(javax.swing.JList<?> p0, int p1, int p2);
  public abstract java.awt.Point indexToLocation(javax.swing.JList<?> p0, int p1);
}
