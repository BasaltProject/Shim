/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing;

public interface BoundedRangeModel {
  int getValue();
  void setValue(int p0);
  int getMaximum();
  int getMinimum();
  void removeChangeListener(javax.swing.event.ChangeListener p0);
  void addChangeListener(javax.swing.event.ChangeListener p0);
  int getExtent();
  void setExtent(int p0);
  void setRangeProperties(int p0, int p1, int p2, int p3, boolean p4);
  void setValueIsAdjusting(boolean p0);
  boolean getValueIsAdjusting();
  void setMinimum(int p0);
  void setMaximum(int p0);
}
