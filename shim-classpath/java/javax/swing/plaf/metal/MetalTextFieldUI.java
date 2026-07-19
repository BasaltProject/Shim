/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package javax.swing.plaf.metal;

import java.beans.PropertyChangeEvent;
import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicTextFieldUI;

public class MetalTextFieldUI extends BasicTextFieldUI {
  public MetalTextFieldUI() { }

  public static ComponentUI createUI(JComponent c) { return new MetalTextFieldUI(); }

  public void propertyChange(PropertyChangeEvent evt) {
    super.propertyChange(evt);
  }
}
