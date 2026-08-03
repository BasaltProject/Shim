/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package javax.swing.text.html;

import java.awt.Container;
import java.awt.Rectangle;
import javax.swing.JViewport;
import javax.swing.SizeRequirements;
import javax.swing.text.Element;
import javax.swing.text.View;

class BodyBlockView extends BlockView {
  BodyBlockView(Element elem) {
    super(elem, View.Y_AXIS);
  }

  protected SizeRequirements calculateMajorAxisRequirements(int axis, SizeRequirements r) {
    r = super.calculateMajorAxisRequirements(axis, r);
    r.maximum = Integer.MAX_VALUE;
    return r;
  }

  protected void layoutMinorAxis(int targetSpan, int axis, int[] offsets, int[] spans) {
    Container container = getContainer();
    Container parentContainer;
    if (container instanceof javax.swing.JEditorPane && (parentContainer = container.getParent()) instanceof JViewport) {
      JViewport viewPort = (JViewport) parentContainer;
      int viewportWidth = viewPort.getExtentSize().width;
      if (viewportWidth > 0) {
        int min = (int) getMinimumSpan(axis);
        targetSpan = Math.max(Math.min(targetSpan, viewportWidth - getLeftInset() - getRightInset()), min);
      }
    }
    super.layoutMinorAxis(targetSpan, axis, offsets, spans);
  }
}
