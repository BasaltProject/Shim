/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt;

public interface Adjustable {
  int HORIZONTAL = 0;
  int VERTICAL = 1;
  int NO_ORIENTATION = 2;
  int getValue();
  void setValue(int p0);
  int getMaximum();
  int getMinimum();
  int getOrientation();
  void setMinimum(int p0);
  void setMaximum(int p0);
  void setUnitIncrement(int p0);
  int getUnitIncrement();
  void setBlockIncrement(int p0);
  int getBlockIncrement();
  void setVisibleAmount(int p0);
  int getVisibleAmount();
  void addAdjustmentListener(java.awt.event.AdjustmentListener p0);
  void removeAdjustmentListener(java.awt.event.AdjustmentListener p0);
}
