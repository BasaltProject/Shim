/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.plaf;

public abstract class OptionPaneUI extends javax.swing.plaf.ComponentUI {
  protected OptionPaneUI() {
  }
  public abstract boolean containsCustomComponents(javax.swing.JOptionPane p0);
  public abstract void selectInitialValue(javax.swing.JOptionPane p0);
}
