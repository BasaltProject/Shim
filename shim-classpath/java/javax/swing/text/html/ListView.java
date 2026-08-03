/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package javax.swing.text.html;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;
import javax.swing.text.Element;
import javax.swing.text.View;

public class ListView extends BlockView {
  private StyleSheet.ListPainter listPainter;

  public ListView(Element elem) {
    super(elem, View.Y_AXIS);
  }

  public float getAlignment(int axis) {
    switch (axis) {
      case View.X_AXIS:
        return 0.5f;
      case View.Y_AXIS:
        return 0.5f;
      default:
        throw new IllegalArgumentException("Invalid axis: " + axis);
    }
  }

  public void paint(Graphics g, Shape allocation) {
    super.paint(g, allocation);
  }

  protected void paintChild(Graphics g, Rectangle alloc, int index) {
    if (listPainter == null) {
      listPainter = getStyleSheet().getListPainter(getAttributes());
    }
    listPainter.paint(g, alloc.x, alloc.y, alloc.width, alloc.height, this, index);
    super.paintChild(g, alloc, index);
  }

  protected void setPropertiesFromAttributes() {
    super.setPropertiesFromAttributes();
    listPainter = getStyleSheet().getListPainter(getAttributes());
  }
}
