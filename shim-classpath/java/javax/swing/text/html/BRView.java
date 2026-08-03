/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package javax.swing.text.html;

import javax.swing.text.Element;

class BRView extends InlineView {
  BRView(Element elem) {
    super(elem);
  }

  public int getBreakWeight(int axis, float pos, float len) {
    if (axis == X_AXIS) {
      return ForcedBreakWeight;
    }
    return super.getBreakWeight(axis, pos, len);
  }
}
