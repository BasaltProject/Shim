/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package javax.swing.text;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;
import javax.swing.Icon;

public class IconView extends View {
  private Icon c;

  public IconView(Element elem) {
    super(elem);
    AttributeSet attr = elem.getAttributes();
    c = StyleConstants.getIcon(attr);
  }

  public void paint(Graphics g, Shape a) {
    Rectangle alloc = a.getBounds();
    if (c != null) {
      c.paintIcon(getContainer(), g, alloc.x, alloc.y);
    }
  }

  public float getPreferredSpan(int axis) {
    switch (axis) {
      case View.X_AXIS:
        return c != null ? c.getIconWidth() : 0;
      case View.Y_AXIS:
        return c != null ? c.getIconHeight() : 0;
      default:
        throw new IllegalArgumentException("Invalid axis: " + axis);
    }
  }

  public float getAlignment(int axis) {
    switch (axis) {
      case View.Y_AXIS:
        return 1;
      default:
        return super.getAlignment(axis);
    }
  }

  public Shape modelToView(int pos, Shape a, Position.Bias b) throws BadLocationException {
    int p0 = getStartOffset();
    int p1 = getEndOffset();
    if ((pos >= p0) && (pos <= p1)) {
      Rectangle r = a.getBounds();
      if (pos == p1) {
        r.x += r.width;
      }
      r.width = 0;
      return r;
    }
    throw new BadLocationException(pos + " not in range " + p0 + "," + p1, pos);
  }

  public int viewToModel(float x, float y, Shape a, Position.Bias[] bias) {
    Rectangle alloc = (Rectangle) a;
    if (x < alloc.x + (alloc.width / 2)) {
      bias[0] = Position.Bias.Forward;
      return getStartOffset();
    }
    bias[0] = Position.Bias.Backward;
    return getEndOffset();
  }
}
