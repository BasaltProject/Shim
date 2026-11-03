/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package javax.swing.plaf.basic;

import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;

public class BasicCheckBoxUI extends BasicRadioButtonUI {
  private static final BasicCheckBoxUI shared = new BasicCheckBoxUI();

  public BasicCheckBoxUI() {
  }

  public static ComponentUI createUI(JComponent b) {
    return shared;
  }

  public String getPropertyPrefix() {
    return "CheckBox.";
  }
}
