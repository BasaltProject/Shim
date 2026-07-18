/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.plaf;

import java.awt.Component;
import java.awt.Graphics;
import java.io.Serializable;
import javax.swing.Icon;

public class IconUIResource implements Icon, UIResource, Serializable {
  private final Icon delegate;

  public IconUIResource(Icon delegate) {
    if (delegate == null) {
      throw new IllegalArgumentException("null delegate icon argument");
    }
    this.delegate = delegate;
  }

  public void paintIcon(Component c, Graphics g, int x, int y) { delegate.paintIcon(c, g, x, y); }

  public int getIconWidth() { return delegate.getIconWidth(); }

  public int getIconHeight() { return delegate.getIconHeight(); }
}
