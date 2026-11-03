/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing;

import java.awt.Container;

public abstract class LayoutStyle {
  private static LayoutStyle style;

  public static void setInstance(LayoutStyle style) {
    synchronized (LayoutStyle.class) {
      LayoutStyle.style = style;
    }
  }

  public static LayoutStyle getInstance() {
    LayoutStyle current;
    synchronized (LayoutStyle.class) {
      current = style;
    }
    if (current == null) {
      LookAndFeel laf = UIManager.getLookAndFeel();
      current = laf == null ? null : laf.getLayoutStyle();
      if (current == null) {
        current = shim.swing.MetalLayoutStyle.kInstance;
      }
    }
    return current;
  }

  public LayoutStyle() { }

  public abstract int getPreferredGap(JComponent component1, JComponent component2, ComponentPlacement type,
      int position, Container parent);

  public abstract int getContainerGap(JComponent component, int position, Container parent);

  public enum ComponentPlacement {
    RELATED, UNRELATED, INDENT;
  }
}
