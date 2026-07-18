/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.plaf;

public abstract class ComboBoxUI extends javax.swing.plaf.ComponentUI {
  protected ComboBoxUI() {
  }
  public abstract void setPopupVisible(javax.swing.JComboBox<?> p0, boolean p1);
  public abstract boolean isPopupVisible(javax.swing.JComboBox<?> p0);
  public abstract boolean isFocusTraversable(javax.swing.JComboBox<?> p0);
}
