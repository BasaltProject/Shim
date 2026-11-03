/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.plaf;

public abstract class TabbedPaneUI extends javax.swing.plaf.ComponentUI {
  protected TabbedPaneUI() {
  }
  public abstract int getTabRunCount(javax.swing.JTabbedPane p0);
  public abstract java.awt.Rectangle getTabBounds(javax.swing.JTabbedPane p0, int p1);
  public abstract int tabForCoordinate(javax.swing.JTabbedPane p0, int p1, int p2);
}
