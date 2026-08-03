/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package javax.swing.text.html;

import java.awt.Graphics;
import java.awt.Shape;
import javax.swing.text.Element;

class NoFramesView extends BlockView {
  NoFramesView(Element elem, int axis) {
    super(elem, axis);
  }

  public void paint(Graphics g, Shape allocation) {
    java.awt.Container host = getContainer();
    if (host != null && isVisible()) {
      super.paint(g, allocation);
    }
  }

  public boolean isVisible() {
    java.awt.Container host = getContainer();
    return host instanceof javax.swing.JEditorPane && !((javax.swing.JEditorPane) host).isEditable() ? false : true;
  }

  public float getPreferredSpan(int axis) { return isVisible() ? super.getPreferredSpan(axis) : 0; }

  public float getMinimumSpan(int axis) { return isVisible() ? super.getMinimumSpan(axis) : 0; }

  public float getMaximumSpan(int axis) { return isVisible() ? super.getMaximumSpan(axis) : 0; }
}
