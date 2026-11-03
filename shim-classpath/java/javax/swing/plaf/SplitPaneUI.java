/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.plaf;

public abstract class SplitPaneUI extends javax.swing.plaf.ComponentUI {
  protected SplitPaneUI() {
  }
  public abstract void resetToPreferredSizes(javax.swing.JSplitPane p0);
  public abstract void setDividerLocation(javax.swing.JSplitPane p0, int p1);
  public abstract int getMinimumDividerLocation(javax.swing.JSplitPane p0);
  public abstract int getMaximumDividerLocation(javax.swing.JSplitPane p0);
  public abstract void finishedPaintingChildren(javax.swing.JSplitPane p0, java.awt.Graphics p1);
  public abstract int getDividerLocation(javax.swing.JSplitPane p0);
}
