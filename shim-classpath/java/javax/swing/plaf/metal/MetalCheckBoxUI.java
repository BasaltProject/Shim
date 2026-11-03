/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package javax.swing.plaf.metal;

import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;

public class MetalCheckBoxUI extends MetalRadioButtonUI {
  private static final MetalCheckBoxUI shared = new MetalCheckBoxUI();
  private boolean defaults_initialized = false;

  public MetalCheckBoxUI() {
  }

  public static ComponentUI createUI(JComponent b) {
    return shared;
  }

  public String getPropertyPrefix() {
    return "CheckBox.";
  }

  public void installDefaults(AbstractButton b) {
    super.installDefaults(b);
    defaults_initialized = true;
  }

  protected void uninstallDefaults(AbstractButton b) {
    super.uninstallDefaults(b);
    defaults_initialized = false;
  }
}
