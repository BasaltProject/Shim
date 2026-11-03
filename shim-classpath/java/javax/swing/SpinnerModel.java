/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing;

public interface SpinnerModel {
  java.lang.Object getValue();
  void setValue(java.lang.Object p0);
  void removeChangeListener(javax.swing.event.ChangeListener p0);
  void addChangeListener(javax.swing.event.ChangeListener p0);
  java.lang.Object getNextValue();
  java.lang.Object getPreviousValue();
}
