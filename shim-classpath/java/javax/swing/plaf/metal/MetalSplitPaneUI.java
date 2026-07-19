/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */


package javax.swing.plaf.metal;

import javax.swing.JComponent;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;

public class MetalSplitPaneUI extends BasicSplitPaneUI {
  public MetalSplitPaneUI() {
  }

  public static javax.swing.plaf.ComponentUI createUI(JComponent x) {
    return new MetalSplitPaneUI();
  }

  public BasicSplitPaneDivider createDefaultDivider() {
    return new MetalSplitPaneDivider(this);
  }
}
