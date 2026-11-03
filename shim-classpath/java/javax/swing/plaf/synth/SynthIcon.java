/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package javax.swing.plaf.synth;

import java.awt.Component;
import java.awt.Graphics;
import javax.swing.Icon;

public interface SynthIcon extends Icon {
  void paintIcon(SynthContext context, Graphics g, int x, int y, int w, int h);

  int getIconWidth(SynthContext context);

  int getIconHeight(SynthContext context);

  default void paintIcon(Component c, Graphics g, int x, int y) {
    paintIcon((SynthContext) null, g, x, y, getIconWidth(), getIconHeight());
  }

  default int getIconWidth() {
    return getIconWidth((SynthContext) null);
  }

  default int getIconHeight() {
    return getIconHeight((SynthContext) null);
  }
}
