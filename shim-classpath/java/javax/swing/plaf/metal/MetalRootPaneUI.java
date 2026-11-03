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
import javax.swing.plaf.basic.BasicRootPaneUI;

public class MetalRootPaneUI extends BasicRootPaneUI {
  private static final MetalRootPaneUI shared = new MetalRootPaneUI();

  public MetalRootPaneUI() {
  }

  public static ComponentUI createUI(JComponent c) {
    return shared;
  }

  public void installUI(JComponent c) {
    super.installUI(c);
  }

  public void uninstallUI(JComponent c) {
    super.uninstallUI(c);
  }

  public void propertyChange(PropertyChangeEvent e) {
    super.propertyChange(e);
  }
}
