/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package javax.swing.text.html;

import javax.swing.text.Element;

class LineView extends ParagraphView {
  int tabBase;

  LineView(Element elem) {
    super(elem);
  }

  public float nextTabStop(float x, int tabOffset) {
    java.awt.Container c = getContainer();
    java.awt.Font f = ((HTMLDocument) getDocument()).getFont(getAttributes());
    int tab = 8 * (c == null ? 8 : c.getFontMetrics(f).charWidth('m'));
    if (tab <= 0) {
      return x;
    }
    int ntabs = (((int) x) - tabBase) / tab;
    return tabBase + ((ntabs + 1) * tab);
  }

  public float getMinimumSpan(int axis) { return getPreferredSpan(axis); }

  public int getResizeWeight(int axis) {
    switch (axis) {
      case X_AXIS:
        return 1;
      case Y_AXIS:
        return 0;
      default:
        return 0;
    }
  }

}
